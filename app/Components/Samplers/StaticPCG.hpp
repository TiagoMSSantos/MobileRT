#ifndef COMPONENTS_SAMPLERS_STATICPCG_HPP
#define COMPONENTS_SAMPLERS_STATICPCG_HPP

#include "MobileRT/Sampler.hpp"

namespace Components {
    class StaticPCG final : public ::MobileRT::Sampler {
    public:
        explicit StaticPCG();

        StaticPCG(const StaticPCG &random) = delete;

        StaticPCG(StaticPCG &&random) noexcept = delete;

        ~StaticPCG() final = default;

        StaticPCG &operator=(const StaticPCG &random) = delete;

        StaticPCG &operator=(StaticPCG &&random) noexcept = delete;

        float getSample(::std::uint32_t sample) final;
    };
}//namespace Components

#endif //COMPONENTS_SAMPLERS_STATICPCG_HPP
