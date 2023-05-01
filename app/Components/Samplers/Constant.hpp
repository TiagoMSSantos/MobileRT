#ifndef COMPONENTS_SAMPLERS_CONSTANT_HPP
#define COMPONENTS_SAMPLERS_CONSTANT_HPP

#include "MobileRT/Sampler.hpp"

namespace Components {

    /**
     * This sampler always returns the same value.
     */
    class Constant final : public ::MobileRT::Sampler {
    private:
        const float value_ {};

    public:
        explicit Constant () = delete;

        explicit Constant(float value);

        Constant(const Constant &constant) = delete;

        Constant(Constant &&constant) noexcept = delete;

        ~Constant() final = default;

        Constant &operator=(const Constant &constant) = delete;

        Constant &operator=(Constant &&constant) noexcept = delete;

        float getSample(::std::uint32_t sample) final;
    };
}//namespace Components

#endif //COMPONENTS_SAMPLERS_CONSTANT_HPP
