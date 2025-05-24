#ifndef MOBILERT_UTILS_UTILS_HPP
#define MOBILERT_UTILS_UTILS_HPP

#include "Utils_dependent.hpp"

#include <algorithm>
#include <array>
#include <boost/assert.hpp>
#include <chrono>
#include <cmath>
#include <glm/ext.hpp>
#include <glm/glm.hpp>

#define GLM_ENABLE_EXPERIMENTAL
#include <glm/gtx/string_cast.hpp>

#include <pcg_random.hpp>
#include <random>
#include <string>
#include <sstream>
#include <thread>
#include <vector>

#if !defined(_WIN32) && !defined(__APPLE__)
    // Not available in Windows nor MacOS.
    #include <boost/stacktrace.hpp>

    // Only used in Linux & Android.
    #include <csignal>
#endif

namespace MobileRT {

// Only catch signals for Linux systems, since boost stacktrace doesn't work on Windows nor MacOS.
#if !defined(_WIN32) && !defined(__APPLE__)
    [[noreturn]] void signalHandler(int signum);
#endif

#ifndef NDEBUG
    /**
     * If in debug mode, then the `LOG_DEBUG` macro will print the given message to the console.
     */
    #define LOG_DEBUG(...) \
        ::Dependent::printDebug( \
            ::MobileRT::convertToString(::MobileRT::getFileName(__FILE__), ":", __LINE__, ": ", __VA_ARGS__) \
        )
#else
    /**
     * If in release mode, then the `LOG_DEBUG` macro should do nothing, to
     * eventually make the compiler optimize and remove it from the
     * generated binary code.
     */
    #define LOG_DEBUG(...) do { } while (false)
#endif

#define LOG_INFO(...) \
    ::Dependent::printInfo( \
        ::MobileRT::convertToString(::MobileRT::getFileName(__FILE__), ":", __LINE__, ": ", __VA_ARGS__) \
    )

#define LOG_WARN(...) \
    ::Dependent::printWarn( \
        ::MobileRT::convertToString(::MobileRT::getFileName(__FILE__), ":", __LINE__, ": ", __VA_ARGS__) \
    )

#define LOG_ERROR(...) \
    ::Dependent::printError( \
        ::MobileRT::convertToString(::MobileRT::getFileName(__FILE__), ":", __LINE__, ": ", __VA_ARGS__) \
    )


#ifndef LOG_DEBUG
#define LOG_DEBUG(...)
#endif

#ifndef LOG_INFO
#define LOG_INFO(...)
#endif

#ifndef LOG_WARN
#define LOG_WARN(...)
#endif

#ifndef LOG_ERROR
#define LOG_ERROR(...)
#endif

    template<typename T, ::std::size_t S>
    void fillArrayWithHaltonSeq(::std::array<T, S> *values);

    template<typename T, ::std::size_t S>
    void fillArrayWithMersenneTwister(::std::array<T, S> *values);

    template<typename T, ::std::size_t S>
    void fillArrayWithPCG(::std::array<T, S> *values);

    inline ::std::string getFileName(const char *filepath);


    ::std::int32_t roundDownToMultipleOf(::std::int32_t value, ::std::int32_t multiple);

    float haltonSequence(::std::uint32_t index, ::std::uint32_t base);

    ::std::int32_t incrementalAvg(const ::glm::vec3 &sample,
                                  ::std::int32_t avg,
                                  ::std::int32_t numSample);

    template<::std::int32_t S, typename T>
    inline ::std::array<T, S> toArray(const char *values);

    ::glm::vec2 toVec2(const char *values);

    ::glm::vec3 toVec3(const char *values);

    ::glm::vec3 toVec3(const float *values);

    bool equal(float a, float b);

    bool equal(const ::glm::vec3 &vec1, const ::glm::vec3 &vec2);

    template<::std::int32_t S, typename T>
    bool isValid(const ::glm::vec<S, T> &value);

    template<::std::int32_t S, typename T>
    bool hasPositiveValue(const ::glm::vec<S, T> &value);

    bool isValid(float value);

    ::glm::vec2 normalize(const ::glm::vec2 &textureCoordinates);

    ::glm::vec3 normalize(const ::glm::vec3 &color);

    float fresnel(const ::glm::vec3 &I, const ::glm::vec3 &N, float ior);

    void checkSystemError(const char *message);

    ::std::string getErrorMessage(const char *message);

    void logFreeMemory();

    void logStackTrace();

   /**
    * Helper method which adds a parameter into the ostringstream.
    *
    * @tparam S The size of the vec.
    * @tparam T The type of the vec.
    * @param oss The ostringstream to add the parameters.
    * @param parameter The parameter to add in the ostringstream.
    */
    template<::std::int32_t S, typename T>
    void addToStringStream(::std::ostringstream *oss, const ::glm::vec<S, T>& parameter) {
        *oss << ::glm::to_string(parameter);
    }

   /**
    * Helper method which adds a parameter into the ostringstream.
    *
    * @tparam Type The type of the argument.
    * @param oss The ostringstream to add the parameters.
    * @param parameter The parameter to add in the ostringstream.
    */
    template <typename Type>
    void addToStringStream(::std::ostringstream *oss, const Type& parameter) {
        *oss << parameter;
    }

    /**
     * Helper method which add a parameter into the ostringstream.
     *
     * @tparam First The type of the first argument.
     * @tparam Args The type of the rest of the arguments.
     * @param oss The ostringstream to add the parameters.
     * @param parameter The first parameter of the list to add.
     * @param args The rest of the arguments.
     */
    template <typename First, typename... Args>
    void addToStringStream(::std::ostringstream *oss, const First& parameter, Args &&... args) {
        addToStringStream(oss, parameter);
        addToStringStream(oss, args...);
    }

    /**
     * Helper method which converts all the parameters to a single string.
     *
     * @tparam Args The type of the arguments.
     * @param args The arguments to convert to string.
     * @return A string containing all the parameters.
     */
    template <typename... Args>
    ::std::string convertToString(Args &&... args) {
        ::std::ostringstream oss {""};
        addToStringStream(&oss, args...);
        oss << '\n';
        const ::std::string &line {oss.str()};
        return line;
    }

    /**
     * Helper method which gets the name of the file of a file path.
     *
     * @param filepath The path to a file.
     * @return The name of the file.
     */
    ::std::string getFileName(const char *const filepath) {
        const ::std::string &filePath {filepath};
        ::std::string::size_type filePos {filePath.rfind('/')};
        if (filePos != ::std::string::npos) {
            ++filePos;
        } else {
            filePos = 0;
        }
        const ::std::string &res {filePath.substr(filePos)};
        return res;
    }

     /**
      * A helper method which prepares an array with random numbers generated.
      * <br/>
      * This method uses the Halton sequence to fill the array and then shuffles the sequence.
      *
      * @tparam T The type of the elements in the array.
      * @tparam S The size of the array.
      * @param values The pointer to an array where the random numbers should be put.
      */
    template<typename T, ::std::size_t S>
    void fillArrayWithHaltonSeq(::std::array<T, S> *const values) {
        for (typename ::std::array<T, S>::iterator itValues {values->begin()}; itValues < values->end(); ::std::advance(itValues, 1)) {
            const ::std::uint32_t index {static_cast<::std::uint32_t> (::std::distance(values->begin(), itValues))};
            *itValues = ::MobileRT::haltonSequence(index, 2);
        }
        thread_local static ::std::random_device randomDevice {};
        thread_local static ::std::mt19937 generator {randomDevice()};
        ::std::shuffle(values->begin(), values->end(), generator);
    }

    /**
      * A helper method which prepares an array with random numbers generated.
      * <br/>
      * This method uses the Mersenne Twister generator to fill the array.
      *
      * @tparam T The type of the elements in the array.
      * @tparam S The size of the array.
      * @param values The pointer to an array where the random numbers should be put.
      */
    template<typename T, ::std::size_t S>
    void fillArrayWithMersenneTwister(::std::array<T, S> *const values) {
        thread_local static ::std::uniform_real_distribution<float> uniformDist {0.0F, 1.0F};
        thread_local static ::std::random_device randomDevice {};
        thread_local static ::std::mt19937 generator {randomDevice()};
        ::std::generate(values->begin(), values->end(), []() {return uniformDist(generator);});
    }

    /**
      * A helper method which prepares an array with random numbers generated.
      * <br/>
      * This method uses the PCG generator to fill the array.
      *
      * @tparam T The type of the elements in the array.
      * @tparam S The size of the array.
      * @param values The pointer to an array where the random numbers should be put.
      */
    template<typename T, ::std::size_t S>
    void fillArrayWithPCG(::std::array<T, S> *const values) {
        thread_local static ::pcg_extras::seed_seq_from<::std::random_device> seedSource {};
        thread_local static ::pcg32 generator(seedSource);
        thread_local static ::std::uniform_real_distribution<float> uniformDist {0.0F, 1.0F};
        ::std::generate(values->begin(), values->end(), []() {return uniformDist(generator);});
    }

    /**
     * Determines whether a ::glm::vec is valid or not.
     *
     * @tparam S The size of ::glm::vec.
     * @tparam T The type of ::glm::vec.
     * @param value A vec floating point values.
     * @return Whether the vec is valid or not.
     */
    template<::std::int32_t S, typename T>
    bool isValid(const ::glm::vec<S, T> &value) {
        const bool isNaN {::glm::all(::glm::isnan(value))};
        const bool isInf {::glm::all(::glm::isinf(value))};
        const bool res {!isNaN && !isInf};
        return res;
    }

    /**
     * Determines whether a ::glm::vec has positive values or not.
     *
     * @tparam S The size of ::glm::vec.
     * @tparam T The type of ::glm::vec.
     * @param value A vec floating point values.
     * @return Whether the vec has positive values or not.
     */
    template<::std::int32_t S, typename T>
    bool hasPositiveValue(const ::glm::vec<S, T> &value) {
        return ::glm::any(::glm::greaterThan(value, ::glm::vec<S, T> {0}));
    }

    /**
     * Converts a sequence of chars to an array.
     *
     * @tparam S The number of values to parse.
     * @tparam T The type of the values to parse.
     * @param values The values to parse.
     * @return An array with the values parsed.
     */
    template<::std::int32_t S, typename T>
    inline ::std::array<T, S> toArray(const char *const values) {
        ::std::stringstream data {values};
        ::std::array<float, S> parsedValues {};
        for (::std::uint32_t i {0u}; i < S; ++i) {
            data >> parsedValues[i];
        }
        return parsedValues;
    }

    /**
     * The `ASSERT` macro will call the the assert macro from C++ Boost framework
     * `BOOST_ASSERT_MSG` and then terminate the application with the error logged.
     */
    #define ASSERT(condition, ...) \
        do { \
            if (!(condition)) { \
                LOG_ERROR("Assertion '", #condition, "': ",  __VA_ARGS__); \
                if (errno != 0) { \
                    LOG_ERROR("ErrorMessage: ", ::MobileRT::getErrorMessage("Assertion failed")); \
                    ::MobileRT::logStackTrace(); \
                    ::MobileRT::logFreeMemory(); \
                } \
                BOOST_ASSERT_MSG(condition, ::MobileRT::convertToString(__VA_ARGS__).c_str()); \
            } \
        } while (false)

    #ifndef NDEBUG
        /**
         * If in debug mode, then the `ASSERT_DEBUG` macro will call the `ASSERT` macro.
         */
        #define ASSERT_DEBUG(condition, ...) ASSERT(condition, ...)
    #else
        /**
         * If in release mode, then the `ASSERT_DEBUG` macro should do nothing,
         * to eventually make the compiler optimize and remove it from the
         * generated binary code.
         */
        #define ASSERT_DEBUG(condition, ...) do { } while (false)
    #endif


    namespace std {
        /**
         * C++ versions:
         * 199711L <=> C++98 or C++03
         * 201103L <=> C++11
         * 201402L <=> C++14
         * 201703L <=> C++17
         * 202002L <=> C++20
         * 202302L <=> C++23
         * 202612L <=> C++26
         */
        #if __cplusplus < 201402L
            /**
             * The make_unique method to be used when the version of the C++ language is older than
             * C++14.
             *
             * @tparam T    The type of the object to construct.
             * @tparam Args The type of the arguments.
             * @param args The arguments to build the object.
             * @return A unique_ptr of an object of type T.
             */
            template<typename T, typename... Args>
            ::std::unique_ptr<T> make_unique(Args &&... args) {
                return ::std::unique_ptr<T>(new T(::std::forward<Args>(args)...));
            }
        #else
            /**
             * The make_unique method to be used when the version of the C++ language is C++14 or
             * newer.
             *
             * @tparam T    The type of the object to construct.
             * @tparam Args The type of the arguments.
             * @param args The arguments to build the object.
             * @return A unique_ptr of an object of type T.
             */
            template<typename T, typename... Args>
            ::std::unique_ptr<T> make_unique(Args &&... args) {
                return ::std::make_unique<T>(::std::forward<Args>(args)...);
            }
        #endif

        /*
         * Its necessary to force a C++ version that is not set in the CMakeLists
         * because with older NDKs, it supports C++17 but this std::to_string is
         * not implemented there.
         */
        #if __cplusplus < 201703L
            /**
             * Helper method that converts any type to std::string for older
             * compilers that are not C++11 compliant.
             *
             * @tparam T  The type of the object to be converted to std::string.
             * @param str The object to be converted to std::string.
             * @return The object in std::string format.
             */
            template <typename T> ::std::string to_string(const T& str) {
                ::std::ostringstream stm {""};
                stm << str ;
                return stm.str() ;
            }
        #else
            /**
             * Helper method that converts any type to std::string.
             *
             * @tparam T  The type of the object to be converted to std::string.
             * @param str The object to be converted to std::string.
             * @return The object in std::string format.
             */
            template <typename T> ::std::string to_string(const T& str) {
                return ::std::to_string(str);
            }
        #endif
    }//namespace std


}//namespace MobileRT

#endif //MOBILERT_UTILS_UTILS_HPP
