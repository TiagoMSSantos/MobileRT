#ifndef MOBILERT_UTILS_CONSTANTS_HPP
#define MOBILERT_UTILS_CONSTANTS_HPP

#include <cstdint>

namespace MobileRT {
    /**
     * The epsilon is to counter floating-point accuracy problems.
     *
     * The fact that floating-point numbers cannot precisely represent all real
     * numbers, and that floating-point operations cannot precisely represent
     * true arithmetic operations, leads to many surprising situations.
     * This is related to the finite precision with which computers generally
     * represent numbers.
     *
     * The value of the epsilon depends on the floating-point precision being
     * used.
     * It should be small enough that only very grazing intersections are culled,
     * but large enough that it avoids the speckling problem. It can be
     * determined by experiment, or by just eyeballing it.
     */
    const float Epsilon {1.0e-06F};

    /**
     * A higher epsilon to counter floating-point accuracy problems.
     * This is currently being used only for the ray sphere intersection.
     */
    const float EpsilonLarge {1.0e-05F};

    /**
     * The maximum distance that a ray can travel.
     */
    const float RayLengthMax {1.0e+30F};

    /**
     * The number of minimum bounces that a ray must do when being traced.
     * Useful for Path Tracing algorithm.
     */
    const ::std::int32_t RayDepthMin {1};

    /**
     * The number of maximum bounces that a ray must do when being traced.
     * Useful for many Ray Tracing algorithms like Whitted and Path Tracing.
     */
    const ::std::int32_t RayDepthMax {6};

    /**
     * The number of tiles (blocks) that divide an image plane.
     */
    const ::std::int32_t NumberOfTiles {256};

    /**
     * The number of axes in the scene.
     * Typically is just 3: X (length), Y (height) and Z (width).
     */
    const ::std::int32_t NumberOfAxes {3};

    /**
     * The size of a stack.
     * This is currently being used for the stacks in BVH.
     */
    const ::std::int32_t StackSize {512};

    /**
     * A mask that is used to get an index in an array more efficiently.
     * For example: index = counter++ & ArrayMask
     *
     * Where the counter is just incremented and may reach overflow.
     */
    const ::std::uint32_t ArrayMask {0xFFFFF};

    /**
     * The size of an array.
     * The size is just the mask + 1, so the mask can be used when getting the
     * index in the array.
     * This is currently being used for the arrays of static samplers which
     * contain the random values.
     */
    const ::std::uint32_t ArraySize {ArrayMask + 1};
}//namespace MobileRT


#endif //MOBILERT_UTILS_CONSTANTS_HPP
