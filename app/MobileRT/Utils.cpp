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

        const auto lastRed {avg & 0xFF};
        const auto lastGreen {(avg >> 8) & 0xFF};
        const auto lastBlue {(avg >> 16) & 0xFF};

        const auto samplerRed {static_cast<::std::int32_t> (sample[0] * 255)};
        const auto samplerGreen {static_cast<::std::int32_t> (sample[1] * 255)};
        const auto samplerBlue {static_cast<::std::int32_t> (sample[2] * 255)};

        const auto currentRed {((numSample - 1) * lastRed + samplerRed) / numSample};
        const auto currentGreen {((numSample - 1) * lastGreen + samplerGreen) / numSample};
        const auto currentBlue {((numSample - 1) * lastBlue + samplerBlue) / numSample};

        const auto retR {::std::min(currentRed, 255)};
        const auto retG {::std::min(currentGreen, 255)};
        const auto retB {::std::min(currentBlue, 255)};

        const auto res {static_cast<::std::int32_t>(
            (0xFF000000) |
            static_cast<::std::uint32_t> (retB) << 16 |
            static_cast<::std::uint32_t> (retG) << 8 |
            static_cast<::std::uint32_t> (retR)
        )};

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

}// namespace MobileRT
