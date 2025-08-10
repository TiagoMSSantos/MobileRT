#include "MobileRT/Shapes/Triangle.hpp"

#include "MobileRT/Utils/Utils.hpp"

#include <cmath>
#include <functional>

using ::MobileRT::AABB;
using ::MobileRT::Triangle;
using ::MobileRT::Intersection;

/**
 * The constructor.
 *
 * @param builder A triangle builder.
 */
Triangle::Triangle(const Triangle::Builder &builder) noexcept :
        AC_ {builder.AC_},
        AB_ {builder.AB_},
        pointA_ {builder.pointA_},
        normalA_ {::glm::normalize(builder.normalA_)},
        normalB_ {::glm::normalize(builder.normalB_)},
        normalC_ {::glm::normalize(builder.normalC_)},
        texCoordA_ {builder.texCoordA_},
        texCoordB_ {builder.texCoordB_},
        texCoordC_ {builder.texCoordC_},
        materialIndex_ {builder.materialIndex_} {
    checkArguments();
}

/**
 * Helper method which checks for invalid fields.
 */
void Triangle::checkArguments() const {
    ASSERT(isValid(this->normalA_), "normalA (", ::glm::to_string(this->normalA_), ") must be valid.");
    ASSERT(!equal(this->normalA_, ::glm::vec3 {0}), "normalA (", this->normalA_,") can't be zero.");
    ASSERT(equal(::glm::length(this->normalA_), 1.0F), "normalA (", this->normalA_, ") length must be 1.");

    ASSERT(isValid(this->normalB_), "normalB (", this->normalB_, ") must be valid.");
    ASSERT(!equal(this->normalB_, ::glm::vec3 {0}), "normalB (", this->normalB_,") can't be zero.");
    ASSERT(equal(::glm::length(this->normalB_), 1.0F), "normalB (", this->normalB_, ") length must be 1.");

    ASSERT(isValid(this->normalC_), "normalC (", this->normalC_, ") must be valid.");
    ASSERT(!equal(this->normalC_, ::glm::vec3 {0}), "normalC (", this->normalC_,") can't be zero.");
    ASSERT(equal(::glm::length(this->normalC_), 1.0F), "normalC (", this->normalC_, ") length must be 1.");

    ASSERT(isValid(this->pointA_), "pointA (", this->pointA_, ") must be valid.");

    ASSERT(isValid(this->AB_), "AB (", this->AB_, ") must be valid.");
    ASSERT(!equal(this->AB_, ::glm::vec3 {0}), "AB (", this->AB_,") can't be zero.");

    ASSERT(isValid(this->AC_), "AC (", this->AC_, ") must be valid.");
    ASSERT(!equal(this->AC_, ::glm::vec3 {0}), "AC (", this->AC_,") can't be zero.");

    ASSERT(isValid(this->texCoordA_), "texCoordA (", this->texCoordA_, ") must be valid.");
    ASSERT(isValid(this->texCoordB_), "texCoordB (", this->texCoordB_, ") must be valid.");
    ASSERT(isValid(this->texCoordC_), "texCoordC (", this->texCoordC_, ") must be valid.");
}

/**
 * Determines if a ray intersects this triangle or not and calculates the intersection point.
 *
 * @param intersection The previous intersection of the ray in the scene.
 * @return The intersection point.
 */
Intersection Triangle::intersect(Intersection intersection) const {
    if (intersection.ray_.primitive_ == this) {
        return intersection;
    }

    const ::glm::vec3 &perpendicularVector {::glm::cross(intersection.ray_.direction_, this->AC_)};
    const float normalizedProjection {::glm::dot(this->AB_, perpendicularVector)};
    if (::std::abs(normalizedProjection) < Epsilon) {
        return intersection;
    }

    //u v = barycentric coordinates (uv-space are inside a unit triangle)
    const float normalizedProjectionInv {1.0F / normalizedProjection};
    const ::glm::vec3 &vectorToCamera {intersection.ray_.origin_ - this->pointA_};
    const float u {normalizedProjectionInv * ::glm::dot(vectorToCamera, perpendicularVector)};
    if (u < 0.0F || u > 1.0F) {
        return intersection;
    }

    const ::glm::vec3 &upPerpendicularVector {::glm::cross(vectorToCamera, this->AB_)};
    const float v {normalizedProjectionInv * ::glm::dot (intersection.ray_.direction_, upPerpendicularVector)};
    if (v < 0.0F || (u + v) > 1.0F) {
        return intersection;
    }

    // at this stage we can compute t to find out where
    // the intersection point is on the line
    const float distanceToIntersection {normalizedProjectionInv * ::glm::dot(AC_, upPerpendicularVector)};

    if (distanceToIntersection < Epsilon || distanceToIntersection >= intersection.length_) {
        return intersection;
    }

    const float w {1.0F - u - v};
    const ::glm::vec3 &intersectionNormal {::glm::normalize(this->normalA_ * w + this->normalB_ * u + this->normalC_ * v)};
    const ::glm::vec2 &texCoords {this->texCoordA_ * w + this->texCoordB_ * u + this->texCoordC_ * v};
    const ::glm::vec3 &intersectionPoint {intersection.ray_.origin_ + intersection.ray_.direction_ * distanceToIntersection};
    const Intersection res {::std::move(intersection.ray_),
                            intersectionPoint, distanceToIntersection,
                            intersectionNormal,
                            this,
                            this->materialIndex_,
                            texCoords
    };

    return res;
}

/**
 * Calculates the bounding box of the triangle.
 *
 * @return The bounding box of the triangle.
 */
AABB Triangle::getAABB() const {
    const ::glm::vec3 &pointB {this->pointA_ + this->AB_};
    const ::glm::vec3 &pointC {this->pointA_ + this->AC_};
    const ::glm::vec3 &min {::glm::min(this->pointA_, ::glm::min(pointB, pointC))};
    const ::glm::vec3 &max {::glm::max(this->pointA_, ::glm::max(pointB, pointC))};
    const AABB res {min, max};
    return res;
}

/**
 * Checks if near and far are valid.
 *
 * @param near The distance of near.
 * @param far  The distance of far.
 * @return Whether they are invalid or not.
 */
bool Triangle::isNearFarInvalid(const float near, const float far) {
    return (near > far) || (far < 0);
}

/**
 * Checks if a bounding box intersects the triangle or not.
 *
 * @param box A bounding box.
 * @return Whether if the bounding box intersects the triangle or not.
 */
bool Triangle::intersect(const AABB &box) const {
    const ::std::function<bool(const ::glm::vec3&, const ::glm::vec3&)> lambdaIntersectRayAABB {
        [&](const ::glm::vec3 &orig, const ::glm::vec3 &vec) -> bool {
            ::glm::vec3 t1 {};
            ::glm::vec3 t2 {}; // vectors to hold the T-values for every direction
            float tNear {::std::numeric_limits<float>::min()};
            float tFar {::std::numeric_limits<float>::max()};
            if (::std::fabs(vec[0]) < ::std::numeric_limits<float>::epsilon()) {
                // ray parallel to planes in this direction
                if ((orig[0] < box.getPointMin()[0]) || ((orig[0] + vec[0]) > box.getPointMax()[0])) {
                    return false; // parallel AND outside box : no intersection possible
                }
            } else { // ray not parallel to planes in this direction
                t1[0] = ((box.getPointMin()[0] - orig[0]) / vec[0]);
                t2[0] = ((box.getPointMax()[0] - orig[0]) / vec[0]);
                if (t1[0] > t2[0]) { // we want t1 to hold values for intersection with near plane
                    ::std::swap(t1, t2);
                }
                tNear = ::std::max(t1[0], tNear);
                tFar = ::std::min(t2[0], tFar);
                if (isNearFarInvalid(tNear, tFar)) {
                    return false;
                }
            }
            if (::std::fabs(vec[1]) < ::std::numeric_limits<float>::epsilon()) {
                // ray parallel to planes in this direction
                if ((orig[1] < box.getPointMin()[1]) || ((orig[1] + vec[1]) > box.getPointMax()[1])) {
                    return false; // parallel AND outside box : no intersection possible
                }
            } else { // ray not parallel to planes in this direction
                t1[1] = ((box.getPointMin()[1] - orig[1]) / vec[1]);
                t2[1] = ((box.getPointMax()[1] - orig[1]) / vec[1]);
                if (t1[1] > t2[1]) { // we want t1 to hold values for intersection with near plane
                    ::std::swap(t1, t2);
                }
                tNear = ::std::max(t1[1], tNear);
                tFar = ::std::min(t2[1], tFar);
                if (isNearFarInvalid(tNear, tFar)) {
                    return false;
                }
            }
            if (::std::fabs(vec[2]) < ::std::numeric_limits<float>::epsilon()) {
                // ray parallel to planes in this direction
                if ((orig[2] < box.getPointMin()[2]) || ((orig[2] + vec[2]) > box.getPointMax()[2])) {
                    return false; // parallel AND outside box : no intersection possible
                }
            } else { // ray not parallel to planes in this direction
                t1[2] = ((box.getPointMin()[2] - orig[2]) / vec[2]);
                t2[2] = ((box.getPointMax()[2] - orig[2]) / vec[2]);
                if (t1[2] > t2[2]) { // we want t1 to hold values for intersection with near plane
                    ::std::swap(t1, t2);
                }
                tNear = ::std::max(t1[2], tNear);
                tFar = ::std::min(t2[2], tFar);
                if (isNearFarInvalid(tNear, tFar)) {
                    return false;
                }
            }
            return true; // if we made it here, there was an intersection - YAY
        }};

    const ::std::function<bool(const ::glm::vec3&)> lambdaIsOverTriangle {
        [&](const ::glm::vec3 &vec) -> bool {
            const ::glm::vec3 &perpendicularVector {::glm::cross(vec, this->AC_)};
            const float normalizedProjection {::glm::dot(this->AB_, perpendicularVector)};
            const bool res {::std::abs(normalizedProjection) < Epsilon};
            return res;
        }
    };

    const ::glm::vec3 &min {box.getPointMin()};
    const ::glm::vec3 &max {box.getPointMax()};
    const ::glm::vec3 &vec {max - min};
    Ray ray {vec, min, 1, false};
    const bool intersectedAB {lambdaIntersectRayAABB(this->pointA_, this->AB_)};
    const bool intersectedAC {lambdaIntersectRayAABB(this->pointA_, this->AC_)};
    const ::glm::vec3 &pointB {this->pointA_ + this->AB_};
    const ::glm::vec3 &pointC {this->pointA_ + this->AC_};
    const bool intersectedBC {lambdaIntersectRayAABB(pointB, pointC - pointB)};
    Intersection intersection {::std::move(ray)};
    const float lastDist {intersection.length_};
    intersection = intersect(intersection);
    const bool intersectedRay {intersection.length_ < lastDist};
    const bool insideTriangle {lambdaIsOverTriangle(vec)};
    const bool res {intersectedAB || intersectedAC || intersectedBC || intersectedRay || insideTriangle};

    return res;
}

/**
 * Gets the AC vector of this triangle.
 *
 * @return The AC vector.
 */
::glm::vec3 Triangle::getAC () const {
    return this->AC_;
}

/**
 * Gets the AB vector of this triangle.
 *
 * @return The AB vector.
 */
::glm::vec3 Triangle::getAB () const {
    return this->AB_;
}

/**
 * Gets the point A of this triangle.
 *
 * @return The point A.
 */
::glm::vec3 Triangle::getA() const {
    return this->pointA_;
}

/**
 * Gets the normal of vertex A of this triangle.
 *
 * @return The normal A.
 */
::glm::vec3 Triangle::getNormalA () const {
    return this->normalA_;
}

/**
 * Gets the normal of vertex B of this triangle.
 *
 * @return The normal B.
 */
::glm::vec3 Triangle::getNormalB () const {
    return this->normalB_;
}

/**
 * Gets the normal of vertex C of this triangle.
 *
 * @return The normal C.
 */
::glm::vec3 Triangle::getNormalC () const {
    return this->normalC_;
}

/**
 * Gets the texture coordinate of vertex A of this triangle.
 *
 * @return The texture coordinate A.
 */
::glm::vec2 Triangle::getTexCoordA() const {
    return this->texCoordA_;
}

/**
 * Gets the texture coordinate of vertex B of this triangle.
 *
 * @return The texture coordinate B.
 */
::glm::vec2 Triangle::getTexCoordB() const {
    return this->texCoordB_;
}

/**
 * Gets the texture coordinate of vertex C of this triangle.
 *
 * @return The texture coordinate C.
 */
::glm::vec2 Triangle::getTexCoordC() const {
    return this->texCoordC_;
}

/**
 * Gets the material index of this plane.
 *
 * @return The material index.
 */
::std::int32_t Triangle::getMaterialIndex () const {
    return this->materialIndex_;
}

/**
 * The constructor.
 *
 * @param pointA A vertex of the triangle.
 * @param pointB A vertex of the triangle.
 * @param pointC A vertex of the triangle.
 */
Triangle::Builder::Builder(
        const ::glm::vec3 &pointA,
        const ::glm::vec3 &pointB,
        const ::glm::vec3 &pointC
) noexcept :
        AC_ {pointC - pointA},
        AB_ {pointB - pointA},
        pointA_ {pointA},
        normalA_ {::glm::normalize(::glm::cross(AC_, AB_))},
        normalB_ {::glm::normalize(::glm::cross(AC_, AB_))},
        normalC_ {::glm::normalize(::glm::cross(AC_, AB_))} {
}

/**
 * The constructor.
 *
 * @param normalA A normal of the triangle.
 * @param normalB A normal of the triangle.
 * @param normalC A normal of the triangle.
 * @return A builder for the triangle.
 */
Triangle::Builder Triangle::Builder::withNormals(
        const ::glm::vec3 &normalA,
        const ::glm::vec3 &normalB,
        const ::glm::vec3 &normalC) {
    this->normalA_ = normalA;
    this->normalB_ = normalB;
    this->normalC_ = normalC;
    return *this;
}

/**
 * The constructor.
 *
 * @param texCoordA A texture coordinate of the triangle.
 * @param texCoordB A texture coordinate of the triangle.
 * @param texCoordC A texture coordinate of the triangle.
 * @return A builder for the triangle.
 */
Triangle::Builder Triangle::Builder::withTexCoords(
        const ::glm::vec2 &texCoordA,
        const ::glm::vec2 &texCoordB,
        const ::glm::vec2 &texCoordC) {
    this->texCoordA_ = texCoordA;
    this->texCoordB_ = texCoordB;
    this->texCoordC_ = texCoordC;
    return *this;
}

/**
 * The constructor.
 *
 * @param materialIndex The index of the material for the triangle.
 * @return A builder for the triangle.
 */
Triangle::Builder Triangle::Builder::withMaterialIndex(const ::std::int32_t materialIndex) {
    this->materialIndex_ = materialIndex;
    return *this;
}

/**
 * The build method.
 *
 * @return A new instance of a triangle.
 */
Triangle Triangle::Builder::build() {
    return Triangle(*this);
}

/**
 * Convert class to output stream.
 */
::std::ostream& MobileRT::operator << (::std::ostream &os, const Triangle& triangle) {
    const ::std::string &pointA {::glm::to_string(triangle.pointA_)};
    const ::std::string &pointB {::glm::to_string(triangle.pointA_ + triangle.AB_)};
    const ::std::string &pointC {::glm::to_string(triangle.pointA_ + triangle.AC_)};

    return (os << "A: " << pointA  << ", B: " << pointB << ", C: " << pointC);
}
