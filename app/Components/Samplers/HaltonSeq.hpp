//
// Created by Tiago on 21-11-2016.
//

#ifndef COMPONENTS_SAMPLERS_HALTONSEQ_HPP
#define COMPONENTS_SAMPLERS_HALTONSEQ_HPP

#include "MobileRT/Sampler.hpp"
#include <type_traits>
#include <utility>

namespace Components {
    class HaltonSeq final : public ::MobileRT::Sampler {
    public:
        explicit HaltonSeq() noexcept = default;

        explicit HaltonSeq(::std::uint32_t width, ::std::uint32_t height,
                           ::std::uint32_t samples) noexcept;

        HaltonSeq(const HaltonSeq &haltonSeq) noexcept = delete;

        HaltonSeq(HaltonSeq &&haltonSeq) noexcept = delete;

        ~HaltonSeq() noexcept final = default;

        HaltonSeq &operator=(const HaltonSeq &haltonSeq) noexcept = delete;

        HaltonSeq &operator=(HaltonSeq &&haltonSeq) noexcept = delete;

        float getSample(::std::uint32_t sample) noexcept final;
    };
}//namespace Components

#endif //COMPONENTS_SAMPLERS_HALTONSEQ_HPP
