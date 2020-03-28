#ifndef MOBILERT_UTILS_HPP
#define MOBILERT_UTILS_HPP

#include "Utils_dependent.hpp"

#include <algorithm>
#include <array>
#include <chrono>
#include <cmath>
#include <glm/glm.hpp>
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

    inline ::std::string getFileName(const char *filepath);

    const float Epsilon {1.0e-06F};
    const float EpsilonLarge {1.0e-05F};
    const float RayLengthMax {1.0e+30F};
    const ::std::int32_t RayDepthMin {1};
    const ::std::int32_t RayDepthMax {6};
    const ::std::int32_t NumberOfTiles {256};
    const ::std::int32_t NumberOfAxes {3};
    const ::std::int32_t StackSize {512};
    const ::std::uint32_t ArrayMask {0xFFFFF};
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
     * Helper method which prints all the parameters in the console output.
     *
     * @tparam Args The type of the arguments.
     * @param args The arguments to print.
     */
    template<typename ...Args>
    void log(Args &&... args) {
        ::std::ostringstream oss {""};
        static_cast<void> (::std::initializer_list<::std::int32_t> {(oss << args, 0)...});
        oss << '\n';
        const auto &line {oss.str()};
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
        ::std::array<float, S> parsedValues {0.0F};
        for (auto i {0u}; i < S; ++i) {
            data >> parsedValues[i];
        }
        return parsedValues;
    }


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
                return ::std::make_unique(args);
            }
        #endif
    }//namespace std


}//namespace MobileRT

#endif //MOBILERT_UTILS_HPP
