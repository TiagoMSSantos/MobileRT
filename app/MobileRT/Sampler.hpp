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

    protected:
        /**
         * An auxiliary method that increments the sample counter and gets the
         * current sample from an array received via parameters.
         *
         * @tparam S The size of the array.
         * @param values The array to read the current sample.
         * @return The value in the array corresponding to the current sample.
         */
        template <const ::std::size_t S>
        float getSampleFromArray(const ::std::array<float, S> &values) {
            const auto current {this->sample_.fetch_add(1, ::std::memory_order_relaxed)};
            const auto it {values.begin() + (current & ::MobileRT::ARRAY_MASK)};
            return *it;
        }
    };
}//namespace MobileRT

#endif //MOBILERT_SAMPLER_HPP
