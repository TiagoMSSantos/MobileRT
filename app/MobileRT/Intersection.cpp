#include "MobileRT/Intersection.hpp"

using ::MobileRT::Intersection;

/**
 * The constructor.
 *
 * @param ray  The casted ray into the scene.
 * @param dist The distance between the intersection point and the origin of the ray.
 */
Intersection::Intersection(Ray &&ray, const float dist) :
    length_ {dist},
    ray_ {::std::move(ray)} {
    checkArguments();
}

/**
 * The constructor.
 *
 * @param ray           The casted ray into the scene.
 * @param intPoint      The intersection point.
 * @param dist          The distance between the intersection point and the origin of the ray.
 * @param normal        The normal of the intersected point.
 * @param primitive     The pointer to the intersected primitive.
 * @param materialIndex The index of the material of the intersected shape.
 * @param texCoords     The texture coordinates of the intersected point.
 */
Intersection::Intersection(
    Ray &&ray,
    const ::glm::vec3 &intPoint,
    const float dist,
    const ::glm::vec3 &normal,
    const void *const primitive,
    const ::std::int32_t materialIndex,
    const ::glm::vec2 &texCoords) :
    point_ {intPoint},
    normal_ {normal},
    length_ {dist},
    primitive_ {primitive},
    materialIndex_ {materialIndex},
    texCoords_ {texCoords},
    ray_ {::std::move(ray)} {
    checkArguments();
}

/**
 * Helper method which checks for invalid fields.
 */
void Intersection::checkArguments() const {
    ASSERT(isValid(this->normal_), "normal must have valid values.");
    ASSERT(!equal(this->normal_, ::glm::vec3 {0}), "normal can't be zero.");
    ASSERT(equal(::glm::length(this->normal_), 1.0F), "normal length must be 1.");

    ASSERT(isValid(this->point_), "point must have valid values.");

    ASSERT(isValid(this->texCoords_), "texCoords must have valid values.");

    ASSERT(isValid(this->length_), "length must have valid values.");
    ASSERT(::std::isnormal(this->length_), "length can't be negative or zero.");

    ASSERT(this->material_ == nullptr, "material must be null.");

    ASSERT(this->materialIndex_ >= -1, "materialIndex must be valid.");
}
