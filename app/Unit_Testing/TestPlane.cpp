#include "MobileRT/Shapes/Plane.hpp"
#include <gtest/gtest.h>

using ::MobileRT::AABB;
using ::MobileRT::Plane;

class TestPlane : public testing::Test {
protected:
	Plane *plane {};

	virtual void SetUp() {
		plane = new Plane {::glm::vec3 {-1,0,0}, ::glm::vec3 {1,0,0}, -1};
	}

	virtual void TearDown() {
		delete plane;
	}

	~TestPlane();
};

TestPlane::~TestPlane() {
}

namespace {
	const AABB box {::glm::vec3 {0, 0, -1.5F}, ::glm::vec3 {0, 1, 2.5F}};
}//namespace


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

TEST_F(TestPlane, IntersectBoxOutsideX) {
	const AABB box2 {::glm::vec3 {1, 0, 0}, ::glm::vec3 {2, 1, 1}};
	const bool intersected {plane->intersect(box2)};
	ASSERT_EQ(false, intersected);
}

TEST_F(TestPlane, IntersectBoxInsideX) {
	const AABB box2 {::glm::vec3 {-1.5F, 0, 0}, ::glm::vec3 {0.5F, 1, 1}};
	const bool intersected {plane->intersect(box2)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestPlane, IntersectBoxOutsideY) {
	const Plane plane {::glm::vec3 {0,0,0}, ::glm::vec3 {0,1,0}, -1};
	const AABB box2 {::glm::vec3 {-1, 0.5F, -1}, ::glm::vec3 {0, 1.5F, 0}};
	const bool intersected {plane.intersect(box2)};
	ASSERT_EQ(false, intersected);
}

TEST_F(TestPlane, IntersectBoxInsideY) {
	const Plane plane {::glm::vec3 {0,0,0}, ::glm::vec3 {0,1,0}, -1};
	const AABB box2 {::glm::vec3 {0, -0.5F, 0}, ::glm::vec3 {0, 0.5F, 0}};
	const bool intersected {plane.intersect(box2)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestPlane, IntersectBoxOutsideZ) {
	const Plane plane {::glm::vec3 {0,0,0}, ::glm::vec3 {0,0,-1}, -1};
	const AABB box2 {::glm::vec3 {-1, 0, 0.5F}, ::glm::vec3 {0, 1, 1.5F}};
	const bool intersected {plane.intersect(box2)};
	ASSERT_EQ(false, intersected);
}

TEST_F(TestPlane, IntersectBoxInsideZ) {
	const Plane plane {::glm::vec3 {0,0,0}, ::glm::vec3 {0,0,-1}, -1};
	const bool intersected {plane.intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestPlane, IntersectBoxInsideZ2) {
	const Plane plane {::glm::vec3 {0,0,0}, ::glm::vec3 {0,0,1}, -1};
	const bool intersected {plane.intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestPlane, DistancePoint) {
	const Plane plane {::glm::vec3 {0,0,0}, ::glm::vec3 {0,0,1}, -1};
	const ::glm::vec3 point {0, 0, -1.5F};
	const float intersected {plane.distance(point)};
	const float expected {-1.5F};
	ASSERT_LE(expected, intersected);
}

TEST_F(TestPlane, IntersectBoxBorderX) {
	const AABB box2 {::glm::vec3 {-1, 0, 0}, ::glm::vec3 {0, 1, 1}};
	const bool intersected {plane->intersect(box2)};
	ASSERT_EQ(true, intersected);
}
