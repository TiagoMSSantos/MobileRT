#include "Components/Samplers/MersenneTwister.hpp"

using ::Components::MersenneTwister;

float MersenneTwister::getSample(const ::std::uint32_t /*sample*/) noexcept {
    thread_local static ::std::uniform_real_distribution<float> uniform_dist {0.0f, 1.0f};
    thread_local static ::std::random_device randomDevice {"/dev/urandom"};
    thread_local static ::std::mt19937 generator {randomDevice()};
    const float res{uniform_dist(generator)};
    return res;
}
