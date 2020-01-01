#ifndef MOBILERT_UTILS_HPP
#define MOBILERT_UTILS_HPP

#include "Utils_dependent.hpp"

#include <algorithm>
#include <chrono>
#include <cmath>
#include <glm/glm.hpp>
#include <sstream>
#include <thread>
#include <vector>

namespace MobileRT {
#define LOG(...) {::MobileRT::log(::MobileRT::getFileName(__FILE__), ":", __LINE__, ": ", __VA_ARGS__);}

#ifndef LOG
#define LOG(...)
#endif

    template<typename ...Args>
    void log(Args &&... args);

    inline ::std::string getFileName(const char *filepath);

    const float RayLengthMax {1.0e+30f};
    const ::std::int32_t RayDepthMin {4};
    const ::std::int32_t RayDepthMax {6};
    const ::std::int32_t NumberOfBlocks {256};
    const float Epsilon {1.0e-06f};

    ::std::int32_t roundDownToMultipleOf(::std::int32_t value, ::std::int32_t multiple);

    float haltonSequence(::std::uint32_t index, ::std::uint32_t base);

    ::std::int32_t incrementalAvg(const ::glm::vec3 &sample, ::std::int32_t avg, ::std::int32_t numSample);

    ::glm::vec3 toVec3(const char *values);

    ::glm::vec2 toVec2(const char *values);

    bool equal(float a, float b);

    bool equal(const ::glm::vec3 &a, const ::glm::vec3 &b);

    ::glm::vec2 normalize(const ::glm::vec2 &textureCoordinates);

    template<typename ...Args>
    void log(Args &&... args) {
        ::std::ostringstream oss {""};
        static_cast<void> (::std::initializer_list<::std::int32_t> {(oss << args, 0)...});
        oss << '\n';
        const auto &line {oss.str()};
        ::Dependent::printString(line);
    }

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
}//namespace MobileRT

#if __cplusplus <= 201103L
namespace std {
    template<typename T, typename... Args>
    ::std::unique_ptr<T> make_unique(Args &&... args) {
        return ::std::unique_ptr<T>(new T(::std::forward<Args>(args)...));
    }
}//namespace std
#endif

#endif //MOBILERT_UTILS_HPP
