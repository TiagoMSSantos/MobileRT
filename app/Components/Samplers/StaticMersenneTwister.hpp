#ifndef COMPONENTS_SAMPLERS_STATICMERSENNETWISTER_HPP
#define COMPONENTS_SAMPLERS_STATICMERSENNETWISTER_HPP

#include "MobileRT/Sampler.hpp"
#include <random>

namespace Components {
    class StaticMersenneTwister final : public ::MobileRT::Sampler {
    public:
        explicit StaticMersenneTwister() noexcept;

        StaticMersenneTwister(const StaticMersenneTwister &random) noexcept = delete;

        StaticMersenneTwister(StaticMersenneTwister &&random) noexcept = delete;

        ~StaticMersenneTwister() noexcept final = default;

        StaticMersenneTwister &operator=(const StaticMersenneTwister &random) noexcept = delete;

        StaticMersenneTwister &operator=(StaticMersenneTwister &&random) noexcept = delete;

        float getSample(::std::uint32_t sample) noexcept final;
    };
}//namespace Components

#endif //COMPONENTS_SAMPLERS_STATICMERSENNETWISTER_HPP
