#include "MobileRT/Sampler.hpp"

using ::MobileRT::Sampler;

Sampler::Sampler(const ::std::uint32_t width, const ::std::uint32_t height,
                 const ::std::uint32_t samples) :
    domainSize_ {(width / (width / static_cast<::std::uint32_t>(::std::sqrt(NumberOfTiles)))) *
                (height / (width / static_cast<::std::uint32_t>(::std::sqrt(NumberOfTiles))))},
    samples_ {samples} {
}

Sampler::~Sampler() {
    LOG("SAMPLER DESTROYED!!!");
}

void Sampler::resetSampling() {
    this->sample_ = 0;
}

void Sampler::stopSampling() {
    this->samples_ = 0;
}

float Sampler::getSample() {
    return getSample(0);
}
