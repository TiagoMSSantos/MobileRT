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
    void log(Args &&... args) noexcept;

    inline ::std::string getFileName(const char *filepath) noexcept;

    template<typename T>
    ::std::vector<T *> convertVector(::std::vector<T> &source) noexcept;

    const float RayLengthMax {1.0e+30f};
    const ::std::int32_t RayDepthMin {4};
    const ::std::int32_t RayDepthMax {8};
    const ::std::int32_t NumberOfBlocks {256};
    const float Epsilon {1.0e-06f};

    ::std::int32_t roundDownToMultipleOf(::std::int32_t value, ::std::int32_t multiple) noexcept;

    float haltonSequence(::std::uint32_t index, ::std::uint32_t base) noexcept;

    ::std::uint32_t incrementalAvg(
        const ::glm::vec3 &sample, ::std::uint32_t avg, ::std::uint32_t numSample) noexcept;


    ::glm::vec3 toVec3(const char *values) noexcept;

    ::glm::vec2 toVec2(const char *values) noexcept;

    template<typename ...Args>
    void log(Args &&... args) noexcept {
        ::std::ostringstream oss{""};
        static_cast<void> (::std::initializer_list<::std::int32_t> {(oss << args, 0)...});
        oss << '\n';
        const ::std::string &line {oss.str()};
        ::Dependent::printString(line);
    }

    ::std::string getFileName(const char *const filepath) noexcept {
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

    template<typename T>
    ::std::vector<T *> convertVector(::std::vector<T> &source) noexcept {
        ::std::vector<T *> target(source.size());
        ::std::transform(source.begin(), source.end(), target.begin(),
                         [](T &t) noexcept -> T * { return &t; });
        return target;
    }
}//namespace MobileRT

#endif //MOBILERT_UTILS_HPP
