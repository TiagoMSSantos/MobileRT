#include "Components/Samplers/StaticMersenneTwister.hpp"
#include <array>

using ::Components::StaticMersenneTwister;
namespace {
    const ::std::uint32_t MASK{0xFFFFF};
    const ::std::uint32_t SIZE{MASK + 1};
    ::std::array<float, SIZE> VALUES;

    bool FillThings() {
        static ::std::uniform_real_distribution<float> uniform_dist {0.0f, 1.0f};
        static ::std::random_device randomDevice {"/dev/urandom"};
        static ::std::mt19937 generator {randomDevice()};
        ::std::generate(VALUES.begin(), VALUES.end(), []() {return uniform_dist(generator);});
        return true;
    }
}//namespace

StaticMersenneTwister::StaticMersenneTwister() noexcept {
    static bool unused{FillThings()};
    static_cast<void> (unused);
}

float StaticMersenneTwister::getSample(const ::std::uint32_t /*sample*/) noexcept {
    const ::std::uint32_t current {this->sample_.fetch_add(1, ::std::memory_order_relaxed)};
    const auto it {VALUES.begin() + (current & MASK)};
    return *it;
}
