#include "Components/Samplers/StaticHaltonSeq.hpp"
#include <array>

using ::Components::StaticHaltonSeq;

namespace {
    const ::std::uint32_t MASK{0xFFFFF};
    const ::std::uint32_t SIZE{MASK + 1};
    ::std::array<float, SIZE> VALUES{};

    bool FillThings() {
        for (auto it {VALUES.begin()}; it < VALUES.end(); std::advance(it, 1)) {
            const ::std::uint32_t index {static_cast<uint32_t>(::std::distance(VALUES.begin(), it))};
            *it = ::MobileRT::haltonSequence(index, 2);
        }
        static ::std::random_device randomDevice {"/dev/urandom"};
        static ::std::mt19937 generator {randomDevice()};
        ::std::shuffle(VALUES.begin(), VALUES.end(), generator);
        return true;
    }
}//namespace

StaticHaltonSeq::StaticHaltonSeq() noexcept {
    static bool unused{FillThings()};
    static_cast<void> (unused);
}

StaticHaltonSeq::StaticHaltonSeq(const ::std::uint32_t width, const ::std::uint32_t height,
                                 const ::std::uint32_t samples) noexcept :
        Sampler(width, height, samples) {
    static bool unused{FillThings()};
    static_cast<void> (unused);
}

float StaticHaltonSeq::getSample(const ::std::uint32_t /*sample*/) noexcept {
    const ::std::uint32_t current {this->sample_.fetch_add(1, ::std::memory_order_relaxed)};
    const auto it {VALUES.begin() + (current & MASK)};
    return *it;
}
