#include "MobileRT/Sampler.hpp"

using ::MobileRT::Sampler;

/**
 * The constructor.
 *
 * @param width   The width of the rendered image.
 * @param height  The height of the rendered image.
 * @param samples The number of samples per pixel.
 */
Sampler::Sampler(const ::std::uint32_t width, const ::std::uint32_t height,
                 const ::std::uint32_t samples) :
    domainSize_ {(width / (width / static_cast<::std::uint32_t>(::std::sqrt(NumberOfTiles)))) *
                (height / (width / static_cast<::std::uint32_t>(::std::sqrt(NumberOfTiles))))},
    samples_ {samples} {
}

/**
 * The destructor.
 */
Sampler::~Sampler() {
    LOG("SAMPLER DESTROYED!!!");
}

/**
 * Resets the sampling counter.
 */
void Sampler::resetSampling() {
    this->sample_ = 0;
}

/**
 * Stops the sampling process.
 */
void Sampler::stopSampling() {
    this->samples_ = 0;
}

/**
 * Calculates a new sample.
 *
 * @return A random value between 0 and 1.
 */
float Sampler::getSample() {
    return getSample(0);
}
