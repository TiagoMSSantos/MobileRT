#ifndef COMPONENTS_SAMPLERS_STATICHALTONSEQ_HPP
#define COMPONENTS_SAMPLERS_STATICHALTONSEQ_HPP

#include "MobileRT/Sampler.hpp"
#include <algorithm>
#include <random>

namespace Components {
    class StaticHaltonSeq final : public ::MobileRT::Sampler {
    public:
        explicit StaticHaltonSeq() noexcept;

        explicit StaticHaltonSeq(::std::uint32_t width, ::std::uint32_t height, ::std::uint32_t samples) noexcept;

        StaticHaltonSeq(const StaticHaltonSeq &random) noexcept = delete;

        StaticHaltonSeq(StaticHaltonSeq &&random) noexcept = delete;

        ~StaticHaltonSeq() noexcept final = default;

        StaticHaltonSeq &operator=(const StaticHaltonSeq &random) noexcept = delete;

        StaticHaltonSeq &operator=(StaticHaltonSeq &&random) noexcept = delete;

        float getSample(::std::uint32_t sample) noexcept final;
    };
}//namespace Components

#endif //COMPONENTS_SAMPLERS_STATICHALTONSEQ_HPP
