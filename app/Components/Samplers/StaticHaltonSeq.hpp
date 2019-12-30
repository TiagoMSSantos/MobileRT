#ifndef COMPONENTS_SAMPLERS_STATICHALTONSEQ_HPP
#define COMPONENTS_SAMPLERS_STATICHALTONSEQ_HPP

#include "MobileRT/Sampler.hpp"
#include <algorithm>
#include <random>

namespace Components {
    class StaticHaltonSeq final : public ::MobileRT::Sampler {
    public:
        explicit StaticHaltonSeq();

        explicit StaticHaltonSeq(::std::uint32_t width, ::std::uint32_t height, ::std::uint32_t samples);

        StaticHaltonSeq(const StaticHaltonSeq &random) = delete;

        StaticHaltonSeq(StaticHaltonSeq &&random) noexcept = delete;

        ~StaticHaltonSeq() final = default;

        StaticHaltonSeq &operator=(const StaticHaltonSeq &random) = delete;

        StaticHaltonSeq &operator=(StaticHaltonSeq &&random) noexcept = delete;

        float getSample(::std::uint32_t sample) final;
    };
}//namespace Components

#endif //COMPONENTS_SAMPLERS_STATICHALTONSEQ_HPP
