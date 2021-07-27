#ifndef COMPONENTS_SAMPLERS_STRATIFIED_HPP
#define COMPONENTS_SAMPLERS_STRATIFIED_HPP

#include "MobileRT/Sampler.hpp"

namespace Components {

    /**
     * This sampler returns an arbitrary sequence that picks the middle of the sequence first.
     *
     * This is useful to choose the pixels to render first.
     */
    class Stratified final : public ::MobileRT::Sampler {
    public:
        explicit Stratified() = default;

        explicit Stratified(::std::uint32_t width, ::std::uint32_t height, ::std::uint32_t samples);

        Stratified(const Stratified &stratified) = delete;

        Stratified(Stratified &&stratified) noexcept = delete;

        ~Stratified() final = default;

        Stratified &operator=(const Stratified &stratified) = delete;

        Stratified &operator=(Stratified &&stratified) noexcept = delete;

        float getSample(::std::uint32_t sample) final;
    };
}//namespace Components

#endif //COMPONENTS_SAMPLERS_STRATIFIED_HPP
