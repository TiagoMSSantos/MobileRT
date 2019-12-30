#include "Components/Samplers/StaticHaltonSeq.hpp"
#include <array>

using ::Components::StaticHaltonSeq;

namespace {
    const ::std::uint32_t mask {0xFFFFF};
    const ::std::uint32_t size {mask + 1};
    ::std::array<float, size> values {};

    bool fillThings() {
        for (auto it {values.begin()}; it < values.end(); std::advance(it, 1)) {
            const auto index {static_cast<::std::uint32_t> (::std::distance(values.begin(), it))};
            *it = ::MobileRT::haltonSequence(index, 2);
        }
        static ::std::random_device randomDevice {};
        static ::std::mt19937 generator {randomDevice()};
        ::std::shuffle(values.begin(), values.end(), generator);
        return true;
    }
}//namespace

StaticHaltonSeq::StaticHaltonSeq() {
    static auto unused {fillThings()};
    static_cast<void> (unused);
}

StaticHaltonSeq::StaticHaltonSeq(const ::std::uint32_t width, const ::std::uint32_t height,
                                 const ::std::uint32_t samples) :
    Sampler {width, height, samples} {
    static auto unused{fillThings()};
    static_cast<void> (unused);
}

float StaticHaltonSeq::getSample(const ::std::uint32_t /*sample*/) {
    const auto current {this->sample_.fetch_add(1, ::std::memory_order_relaxed)};
    const auto it {values.begin() + (current & mask)};
    return *it;
}
