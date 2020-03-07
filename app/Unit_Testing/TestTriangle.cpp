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
		triangle = new Triangle {::glm::vec3 {0,0,0},
								 ::glm::vec3 {0,1,0},
								 ::glm::vec3 {0,0,1}, -1};
	}

	virtual void TearDown() {
		delete triangle;
	}

	~TestTriangle();
};

TestTriangle::~TestTriangle() {
    LOG("TESTTRIANGLE DESTROYED!!!");
}

TEST_F(TestTriangle, ConstructorVALUES) {
	ASSERT_EQ(0.0F, triangle->pointA_[0]);
	ASSERT_EQ(0.0F, triangle->pointA_[1]);
	ASSERT_EQ(0.0F, triangle->pointA_[2]);

	const ::glm::vec3 pointB {triangle->pointA_ + triangle->AB_};
	const ::glm::vec3 pointC {triangle->pointA_ + triangle->AC_};
	ASSERT_EQ(0.0F, pointB[0]);
	ASSERT_EQ(1.0F, pointB[1]);
	ASSERT_EQ(0.0F, pointB[2]);

	ASSERT_EQ(0.0F, pointC[0]);
	ASSERT_EQ(0.0F, pointC[1]);
	ASSERT_EQ(1.0F, pointC[2]);

	ASSERT_EQ(0.0F, triangle->AC_[0]);
	ASSERT_EQ(0.0F, triangle->AC_[1]);
	ASSERT_EQ(1.0F, triangle->AC_[2]);

	ASSERT_EQ(0.0F, triangle->AB_[0]);
	ASSERT_EQ(1.0F, triangle->AB_[1]);
	ASSERT_EQ(0.0F, triangle->AB_[2]);

	const ::glm::vec3 bc {pointC - pointB};
	ASSERT_EQ(0.0F, bc[0]);
	ASSERT_EQ(-1.0F, bc[1]);
	ASSERT_EQ(1.0F, bc[2]);
}

TEST_F(TestTriangle, AABB) {
	const AABB box {triangle->getAABB()};

	ASSERT_EQ(0.0F, box.pointMin_[0]);
	ASSERT_EQ(0.0F, box.pointMin_[1]);
	ASSERT_EQ(0.0F, box.pointMin_[2]);

	ASSERT_EQ(0.0F, box.pointMax_[0]);
	ASSERT_EQ(1.0F, box.pointMax_[1]);
	ASSERT_EQ(1.0F, box.pointMax_[2]);
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
	const ::glm::vec3 max {0.1F, 0.1F, 0.1F};
	const AABB box {min, max};
	const bool intersected {triangle->intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestTriangle, intersectBoxInside06) {
	const ::glm::vec3 min {-1, 0.4F, 0.4F};
	const ::glm::vec3 max {1, 1.4F, 1.4F};
	const AABB box {min, max};
	const bool intersected {triangle->intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestTriangle, intersectBoxInside07) {
	const ::glm::vec3 min {-1, 0.4F, 0.7F};
	const ::glm::vec3 max {1, 1.4F, 1.4F};
	const AABB box {min, max};
	const bool intersected {triangle->intersect(box)};
	ASSERT_EQ(false, intersected);
}

TEST_F(TestTriangle, intersectBoxInside08) {
	const Triangle triangle2 {
		::glm::vec3 {10.0F, 0.0F, 10.0F},
		::glm::vec3 {0.0F, 0.0F, 10.0F},
		::glm::vec3 {0.0F, 10.0F, 10.0F}, -1};
	const ::glm::vec3 min {1.25F, 1.25F, 10};
	const ::glm::vec3 max {2.5F, 2.5F, 10};
	const AABB box {min, max};
	const bool intersected {triangle2.intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestTriangle, intersectBoxInside09) {
	const Triangle triangle2 {
		::glm::vec3 {10.0F, 0.0F, 10.0F},
		::glm::vec3 {0.0F, 0.0F, 10.0F},
		::glm::vec3 {0.0F, 10.0F, 10.0F}, -1};
	const ::glm::vec3 min {-1, -1, 10};
	const ::glm::vec3 max {11, 11, 10};
	const AABB box {min, max};
	const bool intersected {triangle2.intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestTriangle, intersectBoxInside10) {
	const Triangle triangle2 {
		::glm::vec3 {1, 1.59000003F, -1.03999996F},
		::glm::vec3 {-1.01999998F, 1.59000003F, -1.03999996F},
		::glm::vec3 {-0.990000009F, 0, -1.03999996F}, -1};
	const ::glm::vec3 min {-11.0200005F, 0.794949531F, -11.04F};
	const ::glm::vec3 max {-0.0100002289F, 11.5899992F, -0.0250005722F};
	const AABB box {min, max};
	const bool intersected {triangle2.intersect(box)};
	ASSERT_EQ(true, intersected);
}

TEST_F(TestTriangle, ConstructorCOPY) {
	const ::glm::vec3 point1 {1.0F, 2.0F, 3.0F};

	ASSERT_EQ(1.0F, point1[0]);
	ASSERT_EQ(2.0F, point1[1]);
	ASSERT_EQ(3.0F, point1[2]);
}

TEST_F(TestTriangle, ConstructorMOVE) {
	const ::glm::vec3 point1 {1.0F, 2.0F, 3.0F};

	ASSERT_EQ(1.0F, point1[0]);
	ASSERT_EQ(2.0F, point1[1]);
	ASSERT_EQ(3.0F, point1[2]);
}

TEST_F(TestTriangle, OperatorLESS) {
	const ::glm::vec3 point1 {3.0F, 2.0F, 1.0F};
	const ::glm::vec3 point2 {1.0F, 2.0F, 3.0F};
	const ::glm::vec3 vector {point2 - point1};

	ASSERT_EQ(-2.0F, vector[0]);
	ASSERT_EQ(0.0F, vector[1]);
	ASSERT_EQ(2.0F, vector[2]);
}

TEST_F(TestTriangle, OperatorMORE) {
	const ::glm::vec3 vector {3.0F, 2.0F, 1.0F};
	const ::glm::vec3 point {1.0F, 2.0F, 3.0F};
	const ::glm::vec3 dest {point + vector};

	ASSERT_EQ(4.0F, dest[0]);
	ASSERT_EQ(4.0F, dest[1]);
	ASSERT_EQ(4.0F, dest[2]);
}

TEST_F(TestTriangle, intersectRayInside01) {
	const ::glm::vec3 orig {2, 0, 0};
	const ::glm::vec3 dir {::glm::vec3 {0, 0, 0} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection {};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(true, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayInside02) {
	const ::glm::vec3 orig {2, 0, 0};
	const ::glm::vec3 dir {::glm::vec3 {0, 1, 0} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection {};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(true, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayInside03) {
	const ::glm::vec3 orig {2, 0, 0};
	const ::glm::vec3 dir {::glm::vec3 {0, 0, 1} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection {};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(true, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayInside04) {
	const ::glm::vec3 orig {2, 0, 0};
	const ::glm::vec3 dir {::glm::vec3 {0, 1, 0} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection {};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(true, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayOutside01) {
	const ::glm::vec3 orig {2, 0, 0};
	const ::glm::vec3 dir {::glm::vec3 {0, 1.000001, 0} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection {};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(false, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayOutside02) {
	const ::glm::vec3 orig {2, 0, 0};
	const ::glm::vec3 dir {::glm::vec3 {0, 0, 1.000001} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection {};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(false, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayOutside03) {
	const ::glm::vec3 orig {2, 2, 2};
	const ::glm::vec3 dir {::glm::vec3 {0.000001, 0, 0} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection {};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(false, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayOutside04) {
	const ::glm::vec3 orig {2, 2, 2};
	const ::glm::vec3 dir {::glm::vec3 {-1, 0, 0} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection {};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(false, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayOutside05) {
	const ::glm::vec3 orig {2, 0, 0};
	const ::glm::vec3 dir {::glm::vec3 {0, -0.000001, 0} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection {};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(false, intersection.length_ < lastDist);
}

TEST_F(TestTriangle, intersectRayOutside06) {
	const ::glm::vec3 orig {2, 0, 0};
	const ::glm::vec3 dir {::glm::vec3 {0, 0, -0.000001} - orig};
	const Ray ray {dir, orig, 1};
    Intersection intersection {};
	const float lastDist {intersection.length_};
	intersection = triangle->intersect(intersection, ray);
	ASSERT_EQ(false, intersection.length_ < lastDist);
}
