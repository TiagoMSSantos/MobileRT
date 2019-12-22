#include "MobileRT/Intersection.hpp"
#include "MobileRT/Ray.hpp"
#include "MobileRT/Shapes/Triangle.hpp"
#include "MobileRT/Utils.hpp"
#include <gtest/gtest.h>

using ::MobileRT::AABB;
using ::MobileRT::Intersection;
using ::MobileRT::Ray;
using ::MobileRT::Triangle;

class TestTriangle : public testing::Test {
protected:
	Triangle *triangle {};

	virtual void SetUp() {
		triangle = new Triangle{::glm::vec3 {0,0,0},
														::glm::vec3 {0,1,0},
														::glm::vec3 {0,0,1}};
	}

	virtual void TearDown() {
		delete triangle;
	}
};

TEST_F(TestTriangle, ConstructorVALUES) {
	ASSERT_EQ(0.0f, triangle->pointA_[0]);
	ASSERT_EQ(0.0f, triangle->pointA_[1]);
	ASSERT_EQ(0.0f, triangle->pointA_[2]);

	const ::glm::vec3 pointB {triangle->pointA_ + triangle->AB_};
	const ::glm::vec3 pointC {triangle->pointA_ + triangle->AC_};
	ASSERT_EQ(0.0f, pointB[0]);
	ASSERT_EQ(1.0f, pointB[1]);
	ASSERT_EQ(0.0f, pointB[2]);

	ASSERT_EQ(0.0f, pointC[0]);
	ASSERT_EQ(0.0f, pointC[1]);
	ASSERT_EQ(1.0f, pointC[2]);

	ASSERT_EQ(0.0f, triangle->AC_[0]);
	ASSERT_EQ(0.0f, triangle->AC_[1]);
	ASSERT_EQ(1.0f, triangle->AC_[2]);

	ASSERT_EQ(0.0f, triangle->AB_[0]);
	ASSERT_EQ(1.0f, triangle->AB_[1]);
	ASSERT_EQ(0.0f, triangle->AB_[2]);

	const ::glm::vec3 bc {pointC - pointB};
	ASSERT_EQ(0.0f, bc[0]);
	ASSERT_EQ(-1.0f, bc[1]);
	ASSERT_EQ(1.0f, bc[2]);
}

TEST_F(TestTriangle, AABB) {
	const AABB box {triangle->getAABB()};

	ASSERT_EQ(0.0f, box.pointMin_[0]);
	ASSERT_EQ(0.0f, box.pointMin_[1]);
	ASSERT_EQ(0.0f, box.pointMin_[2]);

	ASSERT_EQ(0.0f, box.pointMax_[0]);
	ASSERT_EQ(1.0f, box.pointMax_[1]);
	ASSERT_EQ(1.0f, box.pointMax_[2]);
}

TEST_F(TestTriangle, intersectBoxInside01) {
	const ::glm::vec3 min {-1, -1, -1};
	const ::glm::vec3 max {2, 2, 2};
	const AABB box {min, max};
	const bool intersected {triangle->intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestTriangle, intersectBoxInside02) {
	const ::glm::vec3 min {0, 0, 0};
	const ::glm::vec3 max {2, 2, 2};
	const AABB box {min, max};
	const bool intersected {triangle->intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestTriangle, intersectBoxInside03) {
	const ::glm::vec3 min {0, 0, 0};
	const ::glm::vec3 max {0, 1, 1};
	const AABB box {min, max};
	const bool intersected {triangle->intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestTriangle, intersectBoxInside04) {
	const ::glm::vec3 min {0, 0, 0};
	const ::glm::vec3 max {0, 0.5, 0.5};
	const AABB box {min, max};
	const bool intersected {triangle->intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestTriangle, intersectBoxInside05) {
	const ::glm::vec3 min {-1, -1, -1};
	const ::glm::vec3 max {0.1f, 0.1f, 0.1f};
	const AABB box {min, max};
	const bool intersected {triangle->intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestTriangle, intersectBoxInside06) {
	const ::glm::vec3 min {-1, 0.4f, 0.4f};
	const ::glm::vec3 max {1, 1.4f, 1.4f};
	const AABB box {min, max};
	const bool intersected {triangle->intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestTriangle, intersectBoxInside07) {
	const ::glm::vec3 min {-1, 0.4f, 0.7f};
	const ::glm::vec3 max {1, 1.4f, 1.4f};
	const AABB box {min, max};
	const bool intersected {triangle->intersect(box)};
	ASSERT_EQ(false, intersected);
}

TEST_F(TestTriangle, intersectBoxInside08) {
	const Triangle triangle2 {
		::glm::vec3 {10.0f, 0.0f, 10.0f},
		::glm::vec3 {0.0f, 0.0f, 10.0f},
		::glm::vec3 {0.0f, 10.0f, 10.0f}};
	const ::glm::vec3 min {1.25f, 1.25f, 10};
	const ::glm::vec3 max {2.5f, 2.5f, 10};
	const AABB box {min, max};
	const bool intersected {triangle2.intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestTriangle, intersectBoxInside09) {
	const Triangle triangle2 {
		::glm::vec3 {10.0f, 0.0f, 10.0f},
		::glm::vec3 {0.0f, 0.0f, 10.0f},
		::glm::vec3 {0.0f, 10.0f, 10.0f}};
	const ::glm::vec3 min {-1, -1, 10};
	const ::glm::vec3 max {11, 11, 10};
	const AABB box {min, max};
	const bool intersected {triangle2.intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestTriangle, intersectBoxInside10) {
	const Triangle triangle2 {
		::glm::vec3 {1, 1.59000003f, -1.03999996f},
		::glm::vec3 {-1.01999998f, 1.59000003f, -1.03999996f},
		::glm::vec3 {-0.990000009f, 0, -1.03999996f}};
	const ::glm::vec3 min {-11.0200005f, 0.794949531f, -11.04f};
	const ::glm::vec3 max {-0.0100002289f, 11.5899992f, -0.0250005722f};
	const AABB box {min, max};
	const bool intersected {triangle2.intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestTriangle, ConstructorCOPY) {
	const ::glm::vec3 point1 {1.0f, 2.0f, 3.0f};

	ASSERT_EQ(1.0f, point1[0]);
	ASSERT_EQ(2.0f, point1[1]);
	ASSERT_EQ(3.0f, point1[2]);
}

TEST_F(TestTriangle, ConstructorMOVE) {
	const ::glm::vec3 point1 {1.0f, 2.0f, 3.0f};

	ASSERT_EQ(1.0f, point1[0]);
	ASSERT_EQ(2.0f, point1[1]);
	ASSERT_EQ(3.0f, point1[2]);
}

TEST_F(TestTriangle, OperatorLESS) {
	const ::glm::vec3 point1 {3.0f, 2.0f, 1.0f};
	const ::glm::vec3 point2 {1.0f, 2.0f, 3.0f};
	const ::glm::vec3 vector {point2 - point1};

	ASSERT_EQ(-2.0f, vector[0]);
	ASSERT_EQ(0.0f, vector[1]);
	ASSERT_EQ(2.0f, vector[2]);
}

TEST_F(TestTriangle, OperatorMORE) {
	const ::glm::vec3 vector {3.0f, 2.0f, 1.0f};
	const ::glm::vec3 point {1.0f, 2.0f, 3.0f};
	const ::glm::vec3 dest {point + vector};

	ASSERT_EQ(4.0f, dest[0]);
	ASSERT_EQ(4.0f, dest[1]);
	ASSERT_EQ(4.0f, dest[2]);
}

TEST_F(TestTriangle, intersectRayInside01) {
	const ::glm::vec3 orig {2, 0, 0};
	const ::glm::vec3 dir {::glm::vec3 {0, 0, 0} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection{::MobileRT::RayLengthMax, nullptr};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(true, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayInside02) {
	const ::glm::vec3 orig {2, 0, 0};
	const ::glm::vec3 dir {::glm::vec3 {0, 1, 0} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection{::MobileRT::RayLengthMax, nullptr};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(true, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayInside03) {
	const ::glm::vec3 orig {2, 0, 0};
	const ::glm::vec3 dir {::glm::vec3 {0, 0, 1} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection{::MobileRT::RayLengthMax, nullptr};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(true, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayInside04) {
	const ::glm::vec3 orig {2, 0, 0};
	const ::glm::vec3 dir {::glm::vec3 {0, 1, 0} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection{::MobileRT::RayLengthMax, nullptr};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(true, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayOutside01) {
	const ::glm::vec3 orig {2, 0, 0};
	const ::glm::vec3 dir {::glm::vec3 {0, 1.000001, 0} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection{::MobileRT::RayLengthMax, nullptr};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(false, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayOutside02) {
	const ::glm::vec3 orig {2, 0, 0};
	const ::glm::vec3 dir {::glm::vec3 {0, 0, 1.000001} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection{::MobileRT::RayLengthMax, nullptr};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(false, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayOutside03) {
	const ::glm::vec3 orig {2, 2, 2};
	const ::glm::vec3 dir {::glm::vec3 {0.000001, 0, 0} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection{::MobileRT::RayLengthMax, nullptr};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(false, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayOutside04) {
	const ::glm::vec3 orig {2, 2, 2};
	const ::glm::vec3 dir {::glm::vec3 {-1, 0, 0} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection{::MobileRT::RayLengthMax, nullptr};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(false, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayOutside05) {
	const ::glm::vec3 orig {2, 0, 0};
	const ::glm::vec3 dir {::glm::vec3 {0, -0.000001, 0} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection{::MobileRT::RayLengthMax, nullptr};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(false, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayOutside06) {
	const ::glm::vec3 orig {2, 0, 0};
	const ::glm::vec3 dir {::glm::vec3 {0, 0, -0.000001} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection{::MobileRT::RayLengthMax, nullptr};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(false, intersection.length_ < lastDist);
}
