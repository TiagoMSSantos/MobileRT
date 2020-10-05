#include "MobileRT/Ray.hpp"
#include <atomic>

using ::MobileRT::Ray;

namespace {

    /**
     * An atomic counter to generate Ray ids.
     */
    ::std::atomic<::std::uint64_t> counter {};

    /**
     * A helper method that resets the Ray id generator counter.
     */
    void resetIdCounter() {
        counter.store(0L, ::std::memory_order_relaxed);
    }

    /**
     * A helper method that generates an id by using a counter.
     *
     * @return The new value of the counter.
     */
    ::std::uint64_t generateId() {
        const auto currentId {counter.fetch_add(1L, ::std::memory_order_relaxed)};
        return currentId;
    }

    /**
     * Helper method that gets the current id generated.
     *
     * @return The current value of the counter.
     */
    ::std::uint64_t getCurrentId() {
        return counter.load(::std::memory_order_relaxed);
    }
}//namespace

/**
 * The constructor.
 *
 * @param dir       The direction of the ray.
 * @param origin    The origin point of the ray.
 * @param depth     The number of bounces that the previous ray made.
 * @param primitive The pointer to the primitive where this ray is casted from.
 */
Ray::Ray(const ::glm::vec3 &dir, const ::glm::vec3 &origin,
         const ::std::int32_t depth, const void *const primitive) :
    origin_ {origin},
    direction_ {dir},
    depth_ {depth},
    id_ {generateId()},
    primitive_ {primitive} {
    checkArguments();
}

/**
 * Helper method which checks for invalid fields.
 */
void Ray::checkArguments() const {
    ASSERT(isValid(this->direction_), "direction must be valid.");
    ASSERT(!equal(this->direction_, ::glm::vec3 {0}), "direction can't be zero.");

    ASSERT(isValid(this->origin_), "origin must be valid.");
}

/**
 * Helper method that gets the number of casted rays in the scene.
 *
 * @return The number of casted rays.
 */
::std::uint64_t Ray::getNumberOfCastedRays() noexcept {
    return getCurrentId();
}

/**
 * A helper method that resets the Ray id generator counter.
 */
void Ray::resetIdGenerator() noexcept {
    resetIdCounter();
}
