#ifndef COMPONENTS_SAMPLERS_HALTONSEQ_HPP
#define COMPONENTS_SAMPLERS_HALTONSEQ_HPP

#include "MobileRT/Sampler.hpp"
#include <type_traits>
#include <utility>

namespace Components {
    class HaltonSeq final : public ::MobileRT::Sampler {
    public:
        explicit HaltonSeq() = default;

        explicit HaltonSeq(::std::uint32_t width, ::std::uint32_t height, ::std::uint32_t samples);

        HaltonSeq(const HaltonSeq &haltonSeq) = delete;

        HaltonSeq(HaltonSeq &&haltonSeq) noexcept = delete;

        ~HaltonSeq() final = default;

        HaltonSeq &operator=(const HaltonSeq &haltonSeq) = delete;

        HaltonSeq &operator=(HaltonSeq &&haltonSeq) noexcept = delete;

        float getSample(::std::uint32_t sample) final;
    };
}//namespace Components

#endif //COMPONENTS_SAMPLERS_HALTONSEQ_HPP
