#ifndef COMPONENTS_SAMPLERS_MERSENNETWISTER_HPP
#define COMPONENTS_SAMPLERS_MERSENNETWISTER_HPP

#include "MobileRT/Sampler.hpp"
#include <random>

namespace Components {
    class MersenneTwister final : public ::MobileRT::Sampler {
    public:
        explicit MersenneTwister() = default;

        MersenneTwister(const MersenneTwister &random) = delete;

        MersenneTwister(MersenneTwister &&random) noexcept = delete;

        ~MersenneTwister() final = default;

        MersenneTwister &operator=(const MersenneTwister &random) = delete;

        MersenneTwister &operator=(MersenneTwister &&random) noexcept = delete;

        float getSample(::std::uint32_t sample) final;
    };
}//namespace Components

#endif //COMPONENTS_SAMPLERS_MERSENNETWISTER_HPP
