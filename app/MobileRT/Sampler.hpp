#ifndef MOBILERT_SAMPLER_HPP
#define MOBILERT_SAMPLER_HPP

#include "MobileRT/Utils.hpp"
#include <atomic>
#include <limits>

namespace MobileRT {
    /**
     * A class which abstracts a random number generator.
     */
    class Sampler {
    public:
        ::std::atomic<::std::uint32_t> sample_ {};
        const ::std::uint32_t domainSize_ {::std::numeric_limits<::std::uint32_t>::max()};
        ::std::uint32_t samples_ {::std::numeric_limits<::std::uint32_t>::max()};

    public:
        explicit Sampler() = default;

        explicit Sampler(::std::uint32_t width, ::std::uint32_t height,
                         ::std::uint32_t samples);

        Sampler(const Sampler &sampler) = delete;

        Sampler(Sampler &&sampler) noexcept = delete;

        virtual ~Sampler();

        Sampler &operator=(const Sampler &sampler) = delete;

        Sampler &operator=(Sampler &&sampler) noexcept = delete;

        void resetSampling();

        void stopSampling();

        /**
         * Calculates a new sample.
         *
         * @param sample The index of the desired sample.
         * @return A random value between 0 and 1.
         */
        virtual float getSample(::std::uint32_t sample) = 0;

        float getSample();
    };
}//namespace MobileRT

#endif //MOBILERT_SAMPLER_HPP
