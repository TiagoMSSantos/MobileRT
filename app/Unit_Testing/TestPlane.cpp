#include "MobileRT/Shapes/Plane.hpp"
#include <gtest/gtest.h>

using ::MobileRT::AABB;
using ::MobileRT::Plane;

class TestPlane : public testing::Test {
protected:
    Plane *plane {};

    virtual void SetUp () {
        plane = new Plane {::glm::vec3 {-1, 0, 0}, ::glm::vec3 {1, 0, 0}, -1};
    }

    virtual void TearDown () {
    }

    ~TestPlane ();
};

TestPlane::~TestPlane () {
    delete plane;
}

namespace {
    const AABB box {::glm::vec3 {0, 0, -1.5F}, ::glm::vec3 {0, 1, 2.5F}};
}//namespace


/**
 * Tests the Plane constructor with invalid parameters.
 * In this case the normal has length of 0 and should be 1.
 */
TEST_F(TestPlane, TestInvalidConstructor) {
    const auto materialIndex {19};
    const ::glm::vec3 point {1, 2, 3};
    const ::glm::vec3 normal {0, 0, 0};

    ASSERT_DEBUG_DEATH(const Plane plane2(point, normal, materialIndex);, "");
}

/**
 * Tests the Plane constructor with invalid parameters.
 * In this case the normal have an infinite value but should have a length of 1.
 */
TEST_F(TestPlane, TestInvalidConstructor2) {
    const auto materialIndex {19};
    const ::glm::vec3 point {1, 2, 3};
    const ::glm::vec3 normal {::std::numeric_limits<float>::infinity(), 1, 0};

    ASSERT_DEBUG_DEATH(const Plane plane2(point, normal, materialIndex);, "");
}

/**
 * Tests the Plane constructor with invalid parameters.
 * In this case the normal have a NaN value but should have a length of 1.
 */
TEST_F(TestPlane, TestInvalidConstructor3) {
    const auto materialIndex {19};
    const ::glm::vec3 point {1, 2, 3};
    const ::glm::vec3 normal {1, ::std::numeric_limits<float>::quiet_NaN(), 0};

    ASSERT_DEBUG_DEATH(const Plane plane2(point, normal, materialIndex);, "");
}

/**
 * Tests the Plane constructor.
 */
TEST_F(TestPlane, TestConstructor) {
    const auto materialIndex {19};
    const ::glm::vec3 point {1, 2, 3};
    const ::glm::vec3 normal {4, 5, 6};
    const auto normal2 {::glm::normalize(normal)};
    const Plane plane2 {point, normal, materialIndex};

    ASSERT_EQ(materialIndex, plane2.getMaterialIndex());
    for (int i {0}; i < ::MobileRT::NumberOfAxes; ++i) {
        ASSERT_FLOAT_EQ(point[i], plane2.getPoint()[i]);
        ASSERT_FLOAT_EQ(normal2[i], plane2.getNormal()[i]);
    }
}

/**
 * Tests the Plane copy constructor.
 */
TEST_F(TestPlane, TestCopyConstructor) {
    const auto materialIndex {19};
    const ::glm::vec3 point {1, 2, 3};
    const ::glm::vec3 normal {4, 5, 6};
    const auto normal2 {::glm::normalize(normal)};
    const Plane plane2 {point, normal, materialIndex};
    const auto plane3 {plane2};

    ASSERT_EQ(materialIndex, plane3.getMaterialIndex());
    for (int i {0}; i < ::MobileRT::NumberOfAxes; ++i) {
        ASSERT_FLOAT_EQ(point[i], plane3.getPoint()[i]);
        ASSERT_FLOAT_EQ(normal2[i], plane3.getNormal()[i]);
    }
}

/**
 * Tests the Plane move constructor.
 */
TEST_F(TestPlane, TestMoveConstructor) {
    const auto materialIndex {19};
    const ::glm::vec3 point {1, 2, 3};
    const ::glm::vec3 normal {4, 5, 6};
    const auto normal2 {::glm::normalize(normal)};
    Plane plane2 {point, normal, materialIndex};
    const auto plane3 {::std::move(plane2)};

    ASSERT_EQ(materialIndex, plane3.getMaterialIndex());
    for (int i {0}; i < ::MobileRT::NumberOfAxes; ++i) {
        ASSERT_FLOAT_EQ(point[i], plane3.getPoint()[i]);
        ASSERT_FLOAT_EQ(normal2[i], plane3.getNormal()[i]);
    }
}

/**
 * Tests intersecting an AABB with a plane.
 * The AABB in this case should not intersect the plane.
 */
TEST_F(TestPlane, IntersectBoxOutsideX) {
    const AABB box2 {::glm::vec3 {1, 0, 0}, ::glm::vec3 {2, 1, 1}};
    const bool intersected {plane->intersect(box2)};
    ASSERT_EQ(false, intersected);
}

/**
 * Tests intersecting an AABB with a plane.
 * The AABB in this case should intersect the plane.
 */
TEST_F(TestPlane, IntersectBoxInsideX) {
    const AABB box2 {::glm::vec3 {-1.5F, 0, 0}, ::glm::vec3 {0.5F, 1, 1}};
    const bool intersected {plane->intersect(box2)};
    ASSERT_EQ(true, intersected);
}

/**
 * Tests intersecting an AABB with a plane.
 * The AABB in this case should not intersect the plane.
 */
TEST_F(TestPlane, IntersectBoxOutsideY) {
    const Plane plane2 {::glm::vec3 {0, 0, 0}, ::glm::vec3 {0, 1, 0}, -1};
    const AABB box2 {::glm::vec3 {-1, 0.5F, -1}, ::glm::vec3 {0, 1.5F, 0}};
    const bool intersected {plane2.intersect(box2)};
    ASSERT_EQ(false, intersected);
}

/**
 * Tests intersecting an AABB with a plane.
 * The AABB in this case should intersect the plane.
 */
TEST_F(TestPlane, IntersectBoxInsideY) {
    const Plane plane2 {::glm::vec3 {0, 0, 0}, ::glm::vec3 {0, 1, 0}, -1};
    const AABB box2 {::glm::vec3 {0, -0.5F, 0}, ::glm::vec3 {0, 0.5F, 0}};
    const bool intersected {plane2.intersect(box2)};
    ASSERT_EQ(true, intersected);
}

/**
 * Tests intersecting an AABB with a plane.
 * The AABB in this case should not intersect the plane.
 */
TEST_F(TestPlane, IntersectBoxOutsideZ) {
    const Plane plane2 {::glm::vec3 {0, 0, 0}, ::glm::vec3 {0, 0, -1}, -1};
    const AABB box2 {::glm::vec3 {-1, 0, 0.5F}, ::glm::vec3 {0, 1, 1.5F}};
    const bool intersected {plane2.intersect(box2)};
    ASSERT_EQ(false, intersected);
}

/**
 * Tests intersecting an AABB with a plane.
 * The AABB in this case should intersect the plane.
 */
TEST_F(TestPlane, IntersectBoxInsideZ) {
    const Plane plane2 {::glm::vec3 {0, 0, 0}, ::glm::vec3 {0, 0, -1}, -1};
    const bool intersected {plane2.intersect(box)};
    ASSERT_EQ(true, intersected);
}

/**
 * Tests intersecting an AABB with a plane.
 * The AABB in this case should intersect the plane.
 */
TEST_F(TestPlane, IntersectBoxInsideZ2) {
    const Plane plane2 {::glm::vec3 {0, 0, 0}, ::glm::vec3 {0, 0, 1}, -1};
    const bool intersected {plane2.intersect(box)};
    ASSERT_EQ(true, intersected);
}

/**
 * Tests the calculation of the distance between a point and the plane.
 */
TEST_F(TestPlane, DistancePoint) {
    const Plane plane2 {::glm::vec3 {0, 0, 0}, ::glm::vec3 {0, 0, 1}, -1};
    const ::glm::vec3 point {0, 0, -1.5F};
    const float distanceFromPoint {plane2.distance(point)};
    const float expectedDistance {-1.5F};
    ASSERT_LE(expectedDistance, distanceFromPoint);
}

/**
 * Tests intersect an AABB with a plane.
 * In this case it should intersect because of the border in axis X of the AABB.
 */
TEST_F(TestPlane, IntersectBoxBorderX) {
    const AABB box2 {::glm::vec3 {-1, 0.5F, 0.5F}, ::glm::vec3 {0, 1, 1}};
    const bool intersected {plane->intersect(box2)};
    ASSERT_EQ(true, intersected);
}

/**
 * Tests intersect an AABB with a plane.
 * In this case it should intersect because of the border in axis Y of the AABB.
 */
TEST_F(TestPlane, IntersectBoxBorderY) {
    const Plane plane2 {::glm::vec3 {0, 0, 0}, ::glm::vec3 {0, 1, 0}, -1};
    const AABB box2 {::glm::vec3 {0.5F, 0, 0.5F}, ::glm::vec3 {1, 1, 1}};
    const bool intersected {plane2.intersect(box2)};
    ASSERT_EQ(true, intersected);
}

/**
 * Tests intersect an AABB with a plane.
 * In this case it should intersect because of the border in axis Z of the AABB.
 */
TEST_F(TestPlane, IntersectBoxBorderZ) {
    const Plane plane2 {::glm::vec3 {0, 0, 0}, ::glm::vec3 {0, 0, 1}, -1};
    const AABB box2 {::glm::vec3 {0.5F, 0.5F, 0}, ::glm::vec3 {1, 1, 1}};
    const bool intersected {plane2.intersect(box2)};
    ASSERT_EQ(true, intersected);
}

/**
 * Tests the calculation of an AABB around a plane.
 * To get the AABB of a plane, it must calculate first the right vector and then multiply it by 100.
 */
TEST_F(TestPlane, Box) {
    const auto box2 {plane->getAABB()};
    const AABB
        expectedBox {::glm::vec3 {-1, -70.7107F, -70.7107F}, ::glm::vec3 {-1, 70.7107F, 70.7107F}};

    for (auto axis {0}; axis < ::MobileRT::NumberOfAxes; ++axis) {
        ASSERT_FLOAT_EQ(expectedBox.getPointMin()[axis], box2.getPointMin()[axis]);
        ASSERT_FLOAT_EQ(expectedBox.getPointMax()[axis], box2.getPointMax()[axis]);
    }
}

/**
 * Tests the intersection of a Ray with a Plane.
 * The intersection shouldn't happen since the Ray started in the axis X with value 0 and
 * the direction is positive of that axis, but the plane is placed in the other side.
 */
TEST_F(TestPlane, IntersectionRayOutsideX) {
    const auto direction {::glm::vec3 {10.0F, 0.0F, 10.0F}};
    const auto origin {::glm::vec3 {0.0F, 0.0F, 10.0F}};
    const auto depth {19};
    ::MobileRT::Ray ray {direction, origin, depth, false, nullptr};
    ::MobileRT::Intersection intersection {::std::move(ray)};

    const float lastDist {intersection.length_};
    intersection = plane->intersect(intersection);
    ASSERT_EQ(false, intersection.length_ < lastDist);
}

/**
 * Tests the intersection of a Ray with a Plane.
 * The intersection should happen since the Ray started in the axis X with value 0 and
 * the direction is negative of that axis.
 */
TEST_F(TestPlane, IntersectionRayInsideX) {
    const auto direction {::glm::vec3 {-10.0F, 0.0F, 10.0F}};
    const auto origin {::glm::vec3 {0.0F, 0.0F, 10.0F}};
    const auto depth {19};
    ::MobileRT::Ray ray {direction, origin, depth, false, nullptr};
    ::MobileRT::Intersection intersection {::std::move(ray)};

    const float lastDist {intersection.length_};
    intersection = plane->intersect(intersection);
    ASSERT_EQ(true, intersection.length_ < lastDist);
}
