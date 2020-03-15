#include "Components/Samplers/StaticHaltonSeq.hpp"
#include <array>

using ::Components::StaticHaltonSeq;

namespace {
    ::std::array<float, ::MobileRT::ARRAY_SIZE> values {};
}//namespace

StaticHaltonSeq::StaticHaltonSeq() {
    ::MobileRT::fillArrayWithHaltonSeq(&values);
}

StaticHaltonSeq::StaticHaltonSeq(const ::std::uint32_t width, const ::std::uint32_t height,
                                 const ::std::uint32_t samples) :
    Sampler {width, height, samples} {
    ::MobileRT::fillArrayWithHaltonSeq(&values);
}

float StaticHaltonSeq::getSample(const ::std::uint32_t /*sample*/) {
    return Sampler::getSampleFromArray(values);
}
