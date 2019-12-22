#include "MobileRT/Shapes/Plane.hpp"
#include <gtest/gtest.h>

using ::MobileRT::AABB;
using ::MobileRT::Plane;
using ::glm::vec3;

class TestPlane : public testing::Test {
protected:
	Plane *plane {};

	virtual void SetUp() {
		plane = new Plane {::glm::vec3 {-1,0,0}, ::glm::vec3 {1,0,0}};
	}

	virtual void TearDown() {
		delete plane;
	}
};

TEST_F(TestPlane, IntersectBoxOutsideX) {
	const AABB box {::glm::vec3 {1, 0, 0}, ::glm::vec3 {2, 1, 1}};
	const bool intersected {plane->intersect(box)};
	ASSERT_EQ(false, intersected);
}

TEST_F(TestPlane, IntersectBoxInsideX) {
	const AABB box {::glm::vec3 {-1.5F, 0, 0}, ::glm::vec3 {0.5F, 1, 1}};
	const bool intersected {plane->intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestPlane, IntersectBoxOutsideY) {
	const Plane plane {::glm::vec3 {0,0,0}, ::glm::vec3 {0,1,0}};
	const AABB box {::glm::vec3 {-1, 0.5F, -1}, ::glm::vec3 {0, 1.5F, 0}};
	const bool intersected {plane.intersect(box)};
	ASSERT_EQ(false, intersected);
}

TEST_F(TestPlane, IntersectBoxInsideY) {
	const Plane plane {::glm::vec3 {0,0,0}, ::glm::vec3 {0,1,0}};
	const AABB box {::glm::vec3 {0, -0.5F, 0}, ::glm::vec3 {0, 0.5F, 0}};
	const bool intersected {plane.intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestPlane, IntersectBoxOutsideZ) {
	const Plane plane {::glm::vec3 {0,0,0}, ::glm::vec3 {0,0,-1}};
	const AABB box {::glm::vec3 {-1, 0, 0.5F}, ::glm::vec3 {0, 1, 1.5F}};
	const bool intersected {plane.intersect(box)};
	ASSERT_EQ(false, intersected);
}

TEST_F(TestPlane, IntersectBoxInsideZ) {
	const Plane plane {::glm::vec3 {0,0,0}, ::glm::vec3 {0,0,-1}};
	const AABB box {::glm::vec3 {0, 0, -1.5F}, ::glm::vec3 {0, 1, 2.5F}};
	const bool intersected {plane.intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestPlane, IntersectBoxInsideZ2) {
	const Plane plane {::glm::vec3 {0,0,0}, ::glm::vec3 {0,0,1}};
	const AABB box {::glm::vec3 {0, 0, -1.5F}, ::glm::vec3 {0, 1, 2.5F}};
	const bool intersected {plane.intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestPlane, DistancePoint) {
	const Plane plane {::glm::vec3 {0,0,0}, ::glm::vec3 {0,0,1}};
	const ::glm::vec3 point {0, 0, -1.5F};
	const float intersected {plane.distance(point)};
	const float expected {-1.5F};
	ASSERT_LE(expected, intersected);
}

TEST_F(TestPlane, IntersectBoxBorderX) {
	const AABB box {::glm::vec3 {-1, 0, 0}, ::glm::vec3 {0, 1, 1}};
	const bool intersected {plane->intersect(box)};
	ASSERT_EQ(true, intersected);
}
