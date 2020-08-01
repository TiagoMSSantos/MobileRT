#include "Components/Samplers/StaticMersenneTwister.hpp"
#include <array>

using ::Components::StaticMersenneTwister;

namespace {
    ::std::array<float, ::MobileRT::ArraySize> randomSequence {};
}//namespace

StaticMersenneTwister::StaticMersenneTwister() {
    ::MobileRT::fillArrayWithMersenneTwister(&randomSequence);
}

float StaticMersenneTwister::getSample(const ::std::uint32_t /*sample*/) {
    return Sampler::getSampleFromArray(randomSequence);
}
