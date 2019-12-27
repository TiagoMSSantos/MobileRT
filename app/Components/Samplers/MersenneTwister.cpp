#include "Components/Samplers/MersenneTwister.hpp"

using ::Components::MersenneTwister;

float MersenneTwister::getSample(const ::std::uint32_t /*sample*/) noexcept {
    thread_local static ::std::uniform_real_distribution<float> uniformDist {0.0F, 1.0F};
    thread_local static ::std::random_device randomDevice {};
    thread_local static ::std::mt19937 generator {randomDevice()};
    const auto res {uniformDist(generator)};
    return res;
}
