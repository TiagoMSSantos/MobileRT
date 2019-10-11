//
// Created by Tiago on 21-11-2016.
//

#include "Components/Samplers/HaltonSeq.hpp"

using ::Components::HaltonSeq;

HaltonSeq::HaltonSeq(const ::std::uint32_t width, const ::std::uint32_t height,
                     const ::std::uint32_t samples) noexcept :
        Sampler(width, height, samples) {
}

float HaltonSeq::getSample(const ::std::uint32_t sample) noexcept {
    const ::std::uint32_t current {this->sample_.fetch_add(1, ::std::memory_order_relaxed)};
    if (current >= (this->domainSize_ * (sample + 1))) {
        this->sample_.fetch_sub(1, ::std::memory_order_relaxed);
        return 1.0f;
    }
    const float res{::MobileRT::haltonSequence(current - (sample * this->domainSize_), 2)};
    return res;
}
