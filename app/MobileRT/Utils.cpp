#include "MobileRT/Utils.hpp"

namespace MobileRT {

    ::std::int32_t roundDownToMultipleOf(const ::std::int32_t value, const ::std::int32_t multiple) noexcept {
        const auto rest {value % multiple};
        const auto res {rest > 1 ? value - rest : value};
        return res;
    }

    // https://en.wikipedia.org/wiki/Halton_sequence
    float haltonSequence(::std::uint32_t index, const ::std::uint32_t base) noexcept {
        auto fraction {1.0F};
        auto nextValue {0.0F};
        while (index > 0) {
            fraction /= base;
            nextValue += fraction * (index % base);
            index = static_cast<::std::uint32_t> (::std::floor(index / base));
        }
        return nextValue;
    }

    // newAvg = ((size - 1) * oldAvg + newNum) / size;
    ::std::int32_t incrementalAvg(
            const ::glm::vec3 &sample, const ::std::int32_t avg, const ::std::int32_t numSample) noexcept {
        const auto avgUnsigned {static_cast<::std::uint32_t> (avg)};
        const auto numSampleUnsigned {static_cast<::std::uint32_t> (numSample)};

        const auto lastRed {avgUnsigned & 0xFFU};
        const auto lastGreen {(avgUnsigned >> 8U) & 0xFFU};
        const auto lastBlue {(avgUnsigned >> 16U) & 0xFFU};

        const auto samplerRed {static_cast<::std::uint32_t> (sample[0] * 255U)};
        const auto samplerGreen {static_cast<::std::uint32_t> (sample[1] * 255U)};
        const auto samplerBlue {static_cast<::std::uint32_t> (sample[2] * 255U)};

        const auto currentRed {((numSampleUnsigned - 1U) * lastRed + samplerRed) / numSampleUnsigned};
        const auto currentGreen {((numSampleUnsigned - 1U) * lastGreen + samplerGreen) / numSampleUnsigned};
        const auto currentBlue {((numSampleUnsigned - 1U) * lastBlue + samplerBlue) / numSampleUnsigned};

        const auto retR {::std::min(currentRed, 255U)};
        const auto retG {::std::min(currentGreen, 255U)};
        const auto retB {::std::min(currentBlue, 255U)};

        const auto res {static_cast<::std::int32_t> (0xFF000000 | retB << 16U | retG << 8U | retR)};

        return res;
    }

    ::glm::vec3 toVec3(const char *const values) noexcept {
        ::std::stringstream data {values};
        auto x {0.0F};
        auto y {0.0F};
        auto z {0.0F};
        data >> x;
        data >> y;
        data >> z;

        return ::glm::vec3 {x, y, z};
    }

    ::glm::vec2 toVec2(const char *const values) noexcept {
        ::std::stringstream data {values};
        auto x {0.0F};
        auto y {0.0F};
        data >> x;
        data >> y;

        return ::glm::vec2 {x, y};
    }

    bool equal(const float a, const float b) noexcept {
        const auto absValue {::std::fabs(a - b)};
        const auto res {absValue < Epsilon};
        return res;
    }

    bool equal(const ::glm::vec3 &a, const ::glm::vec3 &b) noexcept {
        const auto sameX {equal(a[0], b[0])};
        const auto sameY {equal(a[1], b[1])};
        const auto sameZ {equal(a[2], b[2])};
        const auto same {sameX && sameY && sameZ};
        return same;
    }

}// namespace MobileRT
