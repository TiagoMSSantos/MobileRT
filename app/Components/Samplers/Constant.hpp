#ifndef COMPONENTS_SAMPLERS_CONSTANT_HPP
#define COMPONENTS_SAMPLERS_CONSTANT_HPP

#include "MobileRT/Sampler.hpp"

namespace Components {
    class Constant final : public ::MobileRT::Sampler {
    private:
        const float value_{};

    public:
        explicit Constant () noexcept = delete;

        explicit Constant(float value) noexcept;

        Constant(const Constant &constant) noexcept = delete;

        Constant(Constant &&constant) noexcept = delete;

        ~Constant() noexcept final = default;

        Constant &operator=(const Constant &constant) noexcept = delete;

        Constant &operator=(Constant &&constant) noexcept = delete;

        float getSample(::std::uint32_t sample) noexcept final;
    };
}//namespace Components

#endif //COMPONENTS_SAMPLERS_CONSTANT_HPP
