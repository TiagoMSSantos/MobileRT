#include "Components/Samplers/HaltonSeq.hpp"

#include "MobileRT/Utils/Utils.hpp"

using ::Components::HaltonSeq;

HaltonSeq::HaltonSeq(const ::std::uint32_t width, const ::std::uint32_t height,
                     const ::std::uint32_t samples) :
    Sampler {width, height, samples} {
}

float HaltonSeq::getSample(const ::std::uint32_t sample) {
    const ::std::uint32_t current {this->sample_.fetch_add(1, ::std::memory_order_relaxed)};
    if (current >= (this->domainSize_ * (sample + 1))) {
        this->sample_.fetch_sub(1, ::std::memory_order_relaxed);
        return 1.0F;
    }
    const ::std::uint32_t index {current - (sample * this->domainSize_)};
    const float res {::MobileRT::haltonSequence(index, 2)};
    return res;
}
