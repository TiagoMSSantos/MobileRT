#ifndef COMPONENTS_SAMPLERS_PCG_HPP
#define COMPONENTS_SAMPLERS_PCG_HPP

#include "MobileRT/Sampler.hpp"

namespace Components {
    class PCG final : public ::MobileRT::Sampler {
    public:
        explicit PCG() = default;

        PCG(const PCG &random) = delete;

        PCG(PCG &&random) noexcept = delete;

        ~PCG() final = default;

        PCG &operator=(const PCG &random) = delete;

        PCG &operator=(PCG &&random) noexcept = delete;

        float getSample(::std::uint32_t sample) final;
    };
}//namespace Components

#endif //COMPONENTS_SAMPLERS_PCG_HPP
