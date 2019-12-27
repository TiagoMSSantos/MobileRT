#include "MobileRT/Utils.hpp"

namespace MobileRT {

    ::std::int32_t roundDownToMultipleOf(const ::std::int32_t value, const ::std::int32_t multiple) noexcept {
        const ::std::int32_t rest {value % multiple};
        const ::std::int32_t res {rest > 1 ? value - rest : value};
        return res;
    }

    // https://en.wikipedia.org/wiki/Halton_sequence
    float haltonSequence(::std::uint32_t index, const ::std::uint32_t base) noexcept {
        float fraction {1.0F};
        float nextValue {};
        while (index > 0) {
            fraction /= base;
            nextValue += fraction * (index % base);
            index = static_cast<::std::uint32_t> (::std::floor(index / base));
        }
        return nextValue;
    }

    // newAvg = ((size - 1) * oldAvg + newNum) / size;
    ::std::uint32_t incrementalAvg(
            const ::glm::vec3 &sample, const ::std::uint32_t avg, const ::std::uint32_t numSample) noexcept {

        const ::std::uint32_t lastRed {avg & 0xFFU};
        const ::std::uint32_t lastGreen {(avg >> 8U) & 0xFFU};
        const ::std::uint32_t lastBlue {(avg >> 16U) & 0xFFU};

        const ::std::uint32_t samplerRed {static_cast<::std::uint32_t> (sample[0] * 255)};
        const ::std::uint32_t samplerGreen {static_cast<::std::uint32_t> (sample[1] * 255)};
        const ::std::uint32_t samplerBlue {static_cast<::std::uint32_t> (sample[2] * 255)};

        const ::std::uint32_t currentRed {((numSample - 1) * lastRed + samplerRed) / numSample};
        const ::std::uint32_t currentGreen {((numSample - 1) * lastGreen + samplerGreen) / numSample};
        const ::std::uint32_t currentBlue {((numSample - 1) * lastBlue + samplerBlue) / numSample};

        const ::std::uint32_t retR {::std::min(currentRed, 255U)};
        const ::std::uint32_t retG {::std::min(currentGreen, 255U)};
        const ::std::uint32_t retB {::std::min(currentBlue, 255U)};

        const ::std::uint32_t res {0xFF000000U | (retB << 16U) | (retG << 8U) | retR};

        return res;
    }

    ::glm::vec3 toVec3(const char *const values) noexcept {
        ::std::stringstream data {values};
        float x {};
        float y {};
        float z {};
        data >> x;
        data >> y;
        data >> z;

        return ::glm::vec3 {x, y, z};
    }

    ::glm::vec2 toVec2(const char *const values) noexcept {
        ::std::stringstream data {values};
        float x {};
        float y {};
        data >> x;
        data >> y;

        return ::glm::vec2 {x, y};
    }

}// namespace MobileRT
