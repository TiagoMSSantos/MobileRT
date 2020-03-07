#include "MobileRT/Intersection.hpp"

using ::MobileRT::Intersection;

/**
 * The constructor.
 *
 * @param dist The distance between the intersection point and the origin of the ray.
 */
Intersection::Intersection(const float dist) :
    length_ {dist} {
}

/**
 * The constructor.
 *
 * @param intPoint      The intersection point.
 * @param dist          The distance between the intersection point and the origin of the ray.
 * @param normal        The normal of the intersected point.
 * @param primitive     The pointer to the intersected primitive.
 * @param materialIndex The index of the material of the intersected shape.
 * @param texCoords     The texture coordinates of the intersected point.
 */
Intersection::Intersection(
    const ::glm::vec3 &intPoint,
    const float dist,
    const ::glm::vec3 &normal,
    const void *const primitive,
    const ::std::int32_t materialIndex, ::glm::vec2 texCoords) :
    point_ {intPoint},
    normal_ {normal},
    length_ {dist},
    primitive_ {primitive},
    materialIndex_ {materialIndex},
    texCoords_ {texCoords} {
        assert(!::glm::all(::glm::isnan(this->normal_)));
        assert(!::glm::all(::glm::isinf(this->normal_)));
        assert(!equal(this->normal_, ::glm::vec3 {0}));
}
