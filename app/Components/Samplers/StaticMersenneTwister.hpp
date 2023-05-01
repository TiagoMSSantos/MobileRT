#ifndef COMPONENTS_SAMPLERS_STATICMERSENNETWISTER_HPP
#define COMPONENTS_SAMPLERS_STATICMERSENNETWISTER_HPP

#include "MobileRT/Sampler.hpp"
#include <random>

namespace Components {

    /**
     * This sampler returns the Mersenne Twister sequence that was pre-calculated.
     */
    class StaticMersenneTwister final : public ::MobileRT::Sampler {
    public:
        explicit StaticMersenneTwister();

        StaticMersenneTwister(const StaticMersenneTwister &random) = delete;

        StaticMersenneTwister(StaticMersenneTwister &&random) noexcept = delete;

        ~StaticMersenneTwister() final = default;

        StaticMersenneTwister &operator=(const StaticMersenneTwister &random) = delete;

        StaticMersenneTwister &operator=(StaticMersenneTwister &&random) noexcept = delete;

        float getSample(::std::uint32_t sample) final;
    };
}//namespace Components

#endif //COMPONENTS_SAMPLERS_STATICMERSENNETWISTER_HPP
