#include "MobileRT/Utils.hpp"

namespace MobileRT {

    /**
     * Calculates the highest value that is smaller than the first parameter and is a multiple of the second parameter.
     *
     * @param value    The maximum value that can be multiple of the second parameter.
     * @param multiple The desired value needs to be multiple of this value.
     * @return The highest value that is smaller than the first parameter and is a multiple of the second parameter.
     */
    ::std::int32_t roundDownToMultipleOf(const ::std::int32_t value, const ::std::int32_t multiple) {
        const auto rest {value % multiple};
        const auto res {rest > 1 ? value - rest : value};
        return res;
    }

    /**
     * Calculates the Nth value of the Halton Sequence.
     * In statistics, Halton sequences are sequences used to generate points in space for numerical methods such as
     * Monte Carlo simulations.
     * @see <a href="https://en.wikipedia.org/wiki/Halton_sequence">https://en.wikipedia.org/wiki/Halton_sequence</a>
     *
     * @param index The index of the Halton Sequence.
     * @param base  The numerical base of the sequence.
     * @return A value in the Halton Sequence.
     */
    float haltonSequence(::std::uint32_t index, const ::std::uint32_t base) {
        auto fraction {1.0F};
        auto nextValue {0.0F};
        while (index > 0) {
            fraction /= base;
            nextValue += fraction * (index % base);
            index = static_cast<::std::uint32_t> (::std::floor(index / base));
        }
        return nextValue;
    }

    /**
     * Calculates the new average as an integer with the new sample and the number of samples already done.
     * <br>
     * The average is calculated as: newAvg = ((size - 1) * oldAvg + newNum) / size;
     *
     * @param sample    The new sample for the average.
     * @param avg       The old average.
     * @param numSample The number of samples.
     * @return The current average.
     */
    ::std::int32_t incrementalAvg(
            const ::glm::vec3 &sample, const ::std::int32_t avg, const ::std::int32_t numSample) {
        const auto avgUnsigned {static_cast<::std::uint32_t> (avg)};
        const auto numSampleUnsigned {static_cast<::std::uint32_t> (numSample)};

        const auto lastRed {avgUnsigned & 0xFFU};
        const auto lastGreen {(avgUnsigned >> 8U) & 0xFFU};
        const auto lastBlue {(avgUnsigned >> 16U) & 0xFFU};

        const auto samplerRed {static_cast<::std::uint32_t> (sample[0] * 255U)};
        const auto samplerGreen {static_cast<::std::uint32_t> (sample[1] * 255U)};
        const auto samplerBlue {static_cast<::std::uint32_t> (sample[2] * 255U)};

        const auto currentRed {((numSampleUnsigned - 1U) * lastRed + samplerRed) / numSampleUnsigned};
        const auto currentGreen {((numSampleUnsigned - 1U) * lastGreen + samplerGreen) / numSampleUnsigned};
        const auto currentBlue {((numSampleUnsigned - 1U) * lastBlue + samplerBlue) / numSampleUnsigned};

        const auto retR {::std::min(currentRed, 255U)};
        const auto retG {::std::min(currentGreen, 255U)};
        const auto retB {::std::min(currentBlue, 255U)};

        const auto res {static_cast<::std::int32_t> (0xFF000000 | retB << 16U | retG << 8U | retR)};

        return res;
    }

    /**
     * Converts a sequence of chars to a vec3.
     *
     * @param values The sequence of chars that should contain 3 values.
     * @return The values parsed into a vec3.
     */
    ::glm::vec3 toVec3(const char *const values) {
        ::std::stringstream data {values};
        auto x {0.0F};
        auto y {0.0F};
        auto z {0.0F};
        data >> x;
        data >> y;
        data >> z;

        return ::glm::vec3 {x, y, z};
    }

    /**
     * Converts a sequence of chars to a vec2.
     *
     * @param values The sequence of chars that should contain 2 values.
     * @return The values parsed into a vec2.
     */
    ::glm::vec2 toVec2(const char *const values) {
        ::std::stringstream data {values};
        auto x {0.0F};
        auto y {0.0F};
        data >> x;
        data >> y;

        return ::glm::vec2 {x, y};
    }

    /**
     * Converts an array of floats to a vec3.
     *
     * @param values The array of floats that should contain 3 values.
     * @return The values put into a vec3.
     */
    ::glm::vec3 toVec3(const float *const values) {
        return ::glm::vec3 {values[0], values[1], values[2]};
    }

    /**
     * Determines whether two floating point values are equal.
     * <br>
     * This method assumes two floats are equal if the difference between them is less than Epsilon.
     *
     * @param a A floating point value.
     * @param b A floating point value.
     * @return Whether the two values are equal or not.
     */
    bool equal(const float a, const float b) {
        const auto absValue {::std::fabs(a - b)};
        const auto res {absValue <= EpsilonLarge};
        return res;
    }

    /**
     * Determines whether two ::glm::vec3 are equal.
     * <br>
     * This method assumes two ::glm::vec3 are equal if the difference between them is less than Epsilon.
     *
     * @param a A vec3 floating point values.
     * @param b A vec3 floating point values.
     * @return Whether the two vec3 are equal or not.
     */
    bool equal(const ::glm::vec3 &a, const ::glm::vec3 &b) {
        bool res {equal(a[0], b[0])};
        for (auto i {1}; i < NumberOfAxes; ++i) {
            res = res && equal(a[i], b[i]);
        }
        return res;
    }

    /**
     * Determines whether a floating point value is valid or not.
     *
     * @param value A floating point value.
     * @return Whether the floating point value is valid or not.
     */
    bool isValid(const float value) {
        const auto isNaN {::std::isnan(value)};
        const auto isInf {::std::isinf(value)};
        const auto res {!isNaN && !isInf};
        return res;
    }

    /**
     * Normalizes a vec2.
     * This means that the vec2 values are putting into the [0, 1] range values.
     *
     * @param textureCoordinates The vec2 value.
     * @return A normalized vec2 value.
     */
    ::glm::vec2 normalize(const ::glm::vec2 &textureCoordinates) {
        const auto texCoords {::glm::fract(textureCoordinates)};
        return texCoords;
    }

    /**
     * Normalizes a vec3.
     * This means that the vec3 values are putting into the [0, 1] range values.
     *
     * @param color The vec3 value.
     * @return A normalized vec3 value.
     */
    ::glm::vec3 normalize(const ::glm::vec3 &color) {
        const auto max {::std::max(::std::max(color[0], color[1]), color[2])};
        auto res {color};
        if (max > 1.0F) {
            res = color / max;
        }
        return res;
    }

    /**
     * Calculates the refraction part from the Fresnel equation.
     *
     * @param I   The incident vector.
     * @param N   The normal vector;
     * @param ior The index of refraction of the material.
     * @return The refraction part from the Fresnel equation.
     */
    float fresnel(const ::glm::vec3 &I, const ::glm::vec3 &N, const float ior) {
        float cosi {::glm::clamp(-1.0F, 1.0F, ::glm::dot(I, N))};
        float etai {1.0F};
        float etat {ior};
        if (cosi > 0) {
            ::std::swap(etai, etat);
        }
        // Compute sini using Snell's law
        const float sint {etai / etat * ::std::sqrt(::std::max(0.0F, 1.0F - cosi * cosi))};
        float kr {0.0F};
        // Total internal reflection
        if (sint >= 1.0F) {
            kr = 1.0F;
        } else {
            const float cost {::std::sqrt(::std::max(0.0F, 1.0F - sint * sint))};
            cosi = ::std::abs(cosi);
            const float Rs {((etat * cosi) - (etai * cost)) / ((etat * cosi) + (etai * cost))};
            const float Rp {((etai * cosi) - (etat * cost)) / ((etai * cosi) + (etat * cost))};
            kr = (Rs * Rs + Rp * Rp) / 2.0F;
        }
        // As a consequence of the conservation of energy, transmittance is given by:
        // kt = 1 - kr;
        return kr;
    }

}//namespace MobileRT
