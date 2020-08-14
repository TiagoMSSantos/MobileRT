#ifndef MOBILERT_UTILS_HPP
#define MOBILERT_UTILS_HPP

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
#include <sstream>
#include <thread>
#include <vector>

namespace MobileRT {
#define LOG(...) ::MobileRT::log(::MobileRT::getFileName(__FILE__), ":", __LINE__, ": ", __VA_ARGS__)

#ifndef LOG
#define LOG(...)
#endif

    template<typename ...Args>
    void log(Args &&... args);

    template<typename T, ::std::size_t S>
    void fillArrayWithHaltonSeq(::std::array<T, S> *values);

    template<typename T, ::std::size_t S>
    void fillArrayWithMersenneTwister(::std::array<T, S> *values);

    template<typename T, ::std::size_t S>
    void fillArrayWithPCG(::std::array<T, S> *values);

    inline ::std::string getFileName(const char *filepath);

    /**
     * The epsilon is to counter floating-point accuracy problems.
     *
     * The fact that floating-point numbers cannot precisely represent all real
     * numbers, and that floating-point operations cannot precisely represent
     * true arithmetic operations, leads to many surprising situations.
     * This is related to the finite precision with which computers generally
     * represent numbers.
     *
     * The value of the epsilon depends on the floating-point precision being
     * used.
     * It should be small enough that only very grazing intersections are culled,
     * but large enough that it avoids the speckling problem. It can be
     * determined by experiment, or by just eyeballing it.
     */
    const float Epsilon {1.0e-06F};

    /**
     * A higher epsilon to counter floating-point accuracy problems.
     * This is currently being used only for the ray sphere intersection.
     */
    const float EpsilonLarge {1.0e-05F};

    /**
     * The maximum distance that a ray can travel.
     */
    const float RayLengthMax {1.0e+30F};

    /**
     * The number of minimum bounces that a ray must do when being traced.
     * Useful for Path Tracing algorithm.
     */
    const ::std::int32_t RayDepthMin {1};

    /**
     * The number of maximum bounces that a ray must do when being traced.
     * Useful for many Ray Tracing algorithms like Whitted and Path Tracing.
     */
    const ::std::int32_t RayDepthMax {6};

    /**
     * The number of tiles (blocks) that divide an image plane.
     */
    const ::std::int32_t NumberOfTiles {256};

    /**
     * The number of axes in the scene.
     * Typically is just 3: X (length), Y (height) and Z (width).
     */
    const ::std::int32_t NumberOfAxes {3};

    /**
     * The size of a stack.
     * This is currently being used for the stacks in BVH.
     */
    const ::std::int32_t StackSize {512};

    /**
     * A mask that is used to get an index in an array more efficiently.
     * For example: index = counter++ & ArrayMask
     *
     * Where the counter is just incremented and may reach overflow.
     */
    const ::std::uint32_t ArrayMask {0xFFFFF};

    /**
     * The size of an array.
     * The size is just the mask + 1, so the mask can be used when getting the
     * index in the array.
     * This is currently being used for the arrays of static samplers which
     * contain the random values.
     */
    const ::std::uint32_t ArraySize {ArrayMask + 1};


    ::std::int32_t roundDownToMultipleOf(::std::int32_t value, ::std::int32_t multiple);

    float haltonSequence(::std::uint32_t index, ::std::uint32_t base);

    ::std::int32_t incrementalAvg(const ::glm::vec3 &sample, ::std::int32_t avg, ::std::int32_t numSample);

    template<::std::int32_t S, typename T>
    inline ::std::array<T, S> toArray(const char *values);

    ::glm::vec2 toVec2(const char *values);

    ::glm::vec3 toVec3(const char *values);

    ::glm::vec3 toVec3(const float *values);

    bool equal(float a, float b);

    bool equal(const ::glm::vec3 &a, const ::glm::vec3 &b);

    template<::std::int32_t S, typename T>
    bool isValid(const ::glm::vec<S, T> &value);

    template<::std::int32_t S, typename T>
    bool hasPositiveValue(const ::glm::vec<S, T> &value);

    bool isValid(float value);

    ::glm::vec2 normalize(const ::glm::vec2 &textureCoordinates);

    ::glm::vec3 normalize(const ::glm::vec3 &color);

    float fresnel(const ::glm::vec3 &I, const ::glm::vec3 &N, float ior);

   /**
    * Helper method which adds a parameter into the ostringstream.
    *
    * @tparam S The size of the vec.
    * @tparam T The type of the vec.
    * @param oss The ostringstream to add the parameters.
    * @param parameter The parameter to add in the ostringstream.
    */
    template<::std::int32_t S, typename T>
    void addToStringStream(::std::ostringstream *oss, ::glm::vec<S, T> parameter) {
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
    void addToStringStream(::std::ostringstream *oss, Type parameter) {
        *oss << parameter;
    }

    /**
     * Helper method which adds a parameter into the ostringstream.
     *
     * @tparam S The size of the vec.
     * @tparam T The type of the vec.
     * @tparam Args The type of the rest of the arguments.
     * @param oss The ostringstream to add the parameters.
     * @param parameter The first parameter of the list to add.
     * @param args The rest of the arguments.
     */
    template<::std::int32_t S, typename T, typename... Args>
    void addToStringStream(::std::ostringstream *oss, ::glm::vec<S, T> parameter, Args &&... args) {
        *oss << ::glm::to_string(parameter);
        addToStringStream(oss, args...);
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
    void addToStringStream(::std::ostringstream *oss, First parameter, Args &&... args) {
        *oss << parameter;
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
        const auto &line {oss.str()};
        return line;
    }

    /**
     * Helper method which prints all the parameters in the console output.
     *
     * @tparam Args The type of the arguments.
     * @param args The arguments to print.
     */
    template<typename ...Args>
    void log(Args &&... args) {
        const auto &line {convertToString(args...)};
        ::Dependent::printString(line);
    }

    /**
     * Helper method which gets the name of the file of a file path.
     *
     * @param filepath The path to a file.
     * @return The name of the file.
     */
    ::std::string getFileName(const char *const filepath) {
        const ::std::string &filePath {filepath};
        auto filePos {filePath.rfind('/')};
        if (filePos != ::std::string::npos) {
            ++filePos;
        } else {
            filePos = 0;
        }
        const auto &res {filePath.substr(filePos)};
        return res;
    }

     /**
      * A helper method which prepares an array with random numbers generated.
      * <p>
      * This method uses the Halton sequence to fill the array and then shuffles the sequence.
      *
      * @tparam T The type of the elements in the array.
      * @tparam S The size of the array.
      * @param values The pointer to an array where the random numbers should be put.
      */
    template<typename T, ::std::size_t S>
    void fillArrayWithHaltonSeq(::std::array<T, S> *const values) {
        for (auto it {values->begin()}; it < values->end(); ::std::advance(it, 1)) {
            const auto index {static_cast<::std::uint32_t> (::std::distance(values->begin(), it))};
            *it = ::MobileRT::haltonSequence(index, 2);
        }
        static ::std::random_device randomDevice {};
        static ::std::mt19937 generator {randomDevice()};
        ::std::shuffle(values->begin(), values->end(), generator);
    }

    /**
      * A helper method which prepares an array with random numbers generated.
      * <p>
      * This method uses the Mersenne Twister generator to fill the array.
      *
      * @tparam T The type of the elements in the array.
      * @tparam S The size of the array.
      * @param values The pointer to an array where the random numbers should be put.
      */
    template<typename T, ::std::size_t S>
    void fillArrayWithMersenneTwister(::std::array<T, S> *const values) {
        static ::std::uniform_real_distribution<float> uniformDist {0.0F, 1.0F};
        static ::std::random_device randomDevice {};
        static ::std::mt19937 generator {randomDevice()};
        ::std::generate(values->begin(), values->end(), []() {return uniformDist(generator);});
    }

    /**
      * A helper method which prepares an array with random numbers generated.
      * <p>
      * This method uses the PCG generator to fill the array.
      *
      * @tparam T The type of the elements in the array.
      * @tparam S The size of the array.
      * @param values The pointer to an array where the random numbers should be put.
      */
    template<typename T, ::std::size_t S>
    void fillArrayWithPCG(::std::array<T, S> *const values) {
        static ::pcg_extras::seed_seq_from<::std::random_device> seedSource {};
        static ::pcg32 generator(seedSource);
        static ::std::uniform_real_distribution<float> uniformDist {0.0F, 1.0F};
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
        const auto isNaN {::glm::all(::glm::isnan(value))};
        const auto isInf {::glm::all(::glm::isinf(value))};
        const auto res {!isNaN && !isInf};
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
        for (auto i {0u}; i < S; ++i) {
            data >> parsedValues[i];
        }
        return parsedValues;
    }


    #ifndef NDEBUG
        /**
         * If in debug mode, then the `ASSERT` macro will call the the assert
         * macro from C++ Boost framework `BOOST_ASSERT_MSG` and then terminate
         * the application with a printed error.
         */
        #define ASSERT(condition, ...) \
        do { \
            if (!(condition)) { \
                LOG("Assertion '", #condition, "': ",  __VA_ARGS__); \
                BOOST_ASSERT_MSG(condition, ::MobileRT::convertToString(__VA_ARGS__).c_str()); \
            } \
        } while (false)
    #else
        /**
         * If in release mode, then the `ASSERT` macro should do nothing, to
         * eventually make the compiler optimize and remove it from the
         * generated binary code.
         */
        #define ASSERT(condition, ...) do { } while (false)
    #endif


    namespace std {
        #if __cplusplus <= 201103L
            /**
             * The make_unique method to be used when the version of the C++ language is older than C++14.
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
             * The make_unique method to be used when the version of the C++ language is C++14 or newer..
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
    }//namespace std


}//namespace MobileRT

#endif //MOBILERT_UTILS_HPP
