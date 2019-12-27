#include "Components/Samplers/StaticMersenneTwister.hpp"
#include <array>

using ::Components::StaticMersenneTwister;

namespace {
    const ::std::uint32_t mask {0xFFFFF};
    const ::std::uint32_t size {mask + 1};
    ::std::array<float, size> values {};

    bool fillThings() {
        static ::std::uniform_real_distribution<float> uniformDist {0.0F, 1.0F};
        static ::std::random_device randomDevice {};
        static ::std::mt19937 generator {randomDevice()};
        ::std::generate(values.begin(), values.end(), []() {return uniformDist(generator);});
        return true;
    }
}//namespace

StaticMersenneTwister::StaticMersenneTwister() noexcept {
    static auto unused {fillThings()};
    static_cast<void> (unused);
}

float StaticMersenneTwister::getSample(const ::std::uint32_t /*sample*/) noexcept {
    const auto current {this->sample_.fetch_add(1, ::std::memory_order_relaxed)};
    const auto it {values.begin() + (current & mask)};
    return *it;
}
