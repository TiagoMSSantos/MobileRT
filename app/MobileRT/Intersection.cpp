#include "MobileRT/Intersection.hpp"
#include <boost/assert.hpp>

using ::MobileRT::Intersection;

/**
 * The constructor.
 *
 * @param dist The distance between the intersection point and the origin of the ray.
 */
Intersection::Intersection(const float dist) :
    length_ {dist} {
    checkArguments();
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
    const ::std::int32_t materialIndex,
    const ::glm::vec2 &texCoords) :
    point_ {intPoint},
    normal_ {normal},
    length_ {dist},
    primitive_ {primitive},
    materialIndex_ {materialIndex},
    texCoords_ {texCoords} {
    checkArguments();
}

/**
 * Helper method which checks for invalid fields.
 */
void Intersection::checkArguments() const {
    BOOST_ASSERT_MSG(isValid(this->normal_), "normal must have valid values.");
    BOOST_ASSERT_MSG(!equal(this->normal_, ::glm::vec3 {0}), "normal can't be zero.");

    BOOST_ASSERT_MSG(isValid(this->point_), "point must have valid values.");

    BOOST_ASSERT_MSG(isValid(this->texCoords_), "texCoords must have valid values.");

    BOOST_ASSERT_MSG(isValid(this->length_), "length must have valid values.");
    BOOST_ASSERT_MSG(::std::isnormal(this->length_), "length can't be negative or zero.");

    BOOST_ASSERT_MSG(this->material_ == nullptr, "material must be null.");

    BOOST_ASSERT_MSG(this->materialIndex_ >= -1, "materialIndex must be valid.");
}
