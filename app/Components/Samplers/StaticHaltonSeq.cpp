#include "Components/Samplers/StaticHaltonSeq.hpp"
#include <array>

using ::Components::StaticHaltonSeq;

namespace {
    ::std::array<float, ::MobileRT::ArraySize> randomSequence {};
}//namespace

StaticHaltonSeq::StaticHaltonSeq() {
    ::MobileRT::fillArrayWithHaltonSeq(&randomSequence);
}

StaticHaltonSeq::StaticHaltonSeq(const ::std::uint32_t width, const ::std::uint32_t height,
                                 const ::std::uint32_t samples) :
    Sampler {width, height, samples} {
    ::MobileRT::fillArrayWithHaltonSeq(&randomSequence);
}

float StaticHaltonSeq::getSample(const ::std::uint32_t /*sample*/) {
    return Sampler::getSampleFromArray(randomSequence);
}
