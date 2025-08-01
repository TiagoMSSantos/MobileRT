#include "MobileRT/Intersection.hpp"
#include "MobileRT/Ray.hpp"
#include "MobileRT/Shapes/Triangle.hpp"
#include "MobileRT/Utils/Utils.hpp"
#include <gtest/gtest.h>

using ::MobileRT::AABB;
using ::MobileRT::Intersection;
using ::MobileRT::Ray;
using ::MobileRT::Triangle;

class TestTriangle : public testing::Test {
protected:
    ::std::unique_ptr<Triangle> triangle {};

    void SetUp () final {
        errno = 0;
        this->triangle = ::MobileRT::std::make_unique<Triangle> (
            Triangle::Builder(
                ::glm::vec3 {0, 0, 0},
                ::glm::vec3 {0, 1, 0},
                ::glm::vec3 {0, 0, 1}
            )
            .build()
        );
    }

    void TearDown () final {
    }

    ~TestTriangle () override;
};

TestTriangle::~TestTriangle () {
}

namespace {
    const Triangle triangle2 {
        Triangle::Builder(
            ::glm::vec3 {10.0F, 0.0F, 10.0F},
            ::glm::vec3 {0.0F, 0.0F, 10.0F},
            ::glm::vec3 {0.0F, 10.0F, 10.0F}
        )
            .build()
    };

    const Triangle triangle3 {
        Triangle::Builder(
            ::glm::vec3 {1, 1.59000003F, -1.03999996F},
            ::glm::vec3 {-1.01999998F, 1.59000003F, -1.03999996F},
            ::glm::vec3 {-0.990000009F, 0, -1.03999996F}
        )
            .build()
    };
}//namespace


/**
 * Helper method that checks if an AABB intersects a Triangle.
 *
 * @param min 	   The minimum point of the AABB.
 * @param max	   The maximum point of the AABB.
 * @param triangle The triangle to check the intersection.
 */
inline void assertBoxIntersectsTriangle (const ::glm::vec3 &min,
                                         const ::glm::vec3 &max,
                                         const Triangle &triangle) {
    const AABB box {min, max};
    const bool intersected {triangle.intersect(box)};
    ASSERT_EQ(true, intersected);
}

/**
 * Helper method that checks if a Ray intersects a Triangle.
 *
 * @param orig		  The origin of the ray.
 * @param dir		  The direction of the ray.
 * @param triangle	  The triangle to check the intersection.
 * @param expectedInt Whether its expected to intersect or not.
 * @param primitive   The primitive where the ray was casted from (default is null).
 */
inline void assertRayTriangle (const ::glm::vec3 &orig,
                               const ::glm::vec3 &dir,
                               const Triangle &triangle,
                               const bool expectedInt,
                               const void *const primitive = nullptr) {
    Ray ray {dir, orig, 1, true, primitive};
    Intersection intersection {::std::move(ray)};
    const float lastDist {intersection.length_};
    intersection = triangle.intersect(intersection);
    ASSERT_EQ(expectedInt, intersection.length_ < lastDist);
}

/**
 * Tests the Triangle constructor with invalid parameters.
 */
TEST_F(TestTriangle, TestInvalidConstructor) {
    const ::glm::vec3 A {::glm::vec3 {0.0F, 0.0F, 0.0F}};
    const ::glm::vec3 B {::glm::vec3 {0.0F, 0.0F, 0.0F}};
    const ::glm::vec3 C {::glm::vec3 {0.0F, 0.0F, 0.0F}};

    ASSERT_DEBUG_DEATH(Triangle::Builder(A, B, C).build();, "");
}

/**
 * Tests the Triangle constructor with invalid parameters.
 */
TEST_F(TestTriangle, TestInvalidConstructor2) {
    const ::glm::vec3 A {::glm::vec3 {10.0F, 0.0F, 10.0F}};
    const ::glm::vec3 B {::glm::vec3 {0.0F, 0.0F, 10.0F}};
    const ::glm::vec3 C {::glm::vec3 {0.0F, 10.0F, 10.0F}};
    const ::glm::vec3 normalA {::glm::vec3 {0, 0, 0}};
    const ::glm::vec3 normalB {::glm::vec3 {0, 0, 0}};
    const ::glm::vec3 normalC {::glm::vec3 {0, 0, 0}};

    ASSERT_DEBUG_DEATH(
        Triangle::Builder(A, B, C)
            .withNormals(normalA, normalB, normalC)
            .build();,
        ""
    );
}

/**
 * Tests the Triangle constructor.
 */
TEST_F(TestTriangle, TestConstructor) {
    const ::glm::vec3 A {::glm::vec3 {10.0F, 0.0F, 10.0F}};
    const ::glm::vec3 B {::glm::vec3 {0.0F, 0.0F, 10.0F}};
    const ::glm::vec3 C {::glm::vec3 {0.0F, 10.0F, 10.0F}};
    const ::glm::vec3 normalA {::glm::vec3 {1, 2, 3}};
    const ::glm::vec3 normalB {::glm::vec3 {4, 5, 6}};
    const ::glm::vec3 normalC {::glm::vec3 {7, 8, 9}};
    const ::glm::vec2 texCoordsA {::glm::vec2 {1, 2}};
    const ::glm::vec2 texCoordsB {::glm::vec2 {4, 5}};
    const ::glm::vec2 texCoordsC {::glm::vec2 {7, 8}};
    const ::std::int32_t materialIndex {19};
    const Triangle triangle4 {
        Triangle::Builder(A, B, C)
            .withNormals(normalA, normalB, normalC)
            .withTexCoords(texCoordsA, texCoordsB, texCoordsC)
            .withMaterialIndex(materialIndex)
            .build()
    };
    const ::glm::vec3 pointA {triangle4.getA()};
    const ::glm::vec3 pointB {pointA + triangle4.getAB()};
    const ::glm::vec3 pointC {pointA + triangle4.getAC()};
    const ::glm::vec3 normalA2 {::glm::normalize(normalA)};
    const ::glm::vec3 normalB2 {::glm::normalize(normalB)};
    const ::glm::vec3 normalC2 {::glm::normalize(normalC)};

    ASSERT_EQ(materialIndex, triangle4.getMaterialIndex());
    for (int i {0}; i < ::MobileRT::NumberOfAxes; ++i) {
        ASSERT_FLOAT_EQ(A[i], pointA[i]);
        ASSERT_FLOAT_EQ(B[i], pointB[i]);
        ASSERT_FLOAT_EQ(C[i], pointC[i]);
        ASSERT_FLOAT_EQ(normalA2[i], triangle4.getNormalA()[i]);
        ASSERT_FLOAT_EQ(normalB2[i], triangle4.getNormalB()[i]);
        ASSERT_FLOAT_EQ(normalC2[i], triangle4.getNormalC()[i]);
    }
    for (int i {0}; i < 2; ++i) {
        ASSERT_FLOAT_EQ(texCoordsA[i], triangle4.getTexCoordA()[i]);
        ASSERT_FLOAT_EQ(texCoordsB[i], triangle4.getTexCoordB()[i]);
        ASSERT_FLOAT_EQ(texCoordsC[i], triangle4.getTexCoordC()[i]);
    }
}

/**
 * Tests the Triangle constructor.
 */
TEST_F(TestTriangle, ConstructorVALUES) {
    const ::glm::vec3 pointA {triangle->getA()};
    const ::glm::vec3 AC {triangle->getAC()};
    const ::glm::vec3 AB {triangle->getAB()};
    ASSERT_EQ(0.0F, pointA[0]);
    ASSERT_EQ(0.0F, pointA[1]);
    ASSERT_EQ(0.0F, pointA[2]);

    const ::glm::vec3 pointB {pointA + AB};
    const ::glm::vec3 pointC {pointA + AC};
    ASSERT_EQ(0.0F, pointB[0]);
    ASSERT_EQ(1.0F, pointB[1]);
    ASSERT_EQ(0.0F, pointB[2]);

    ASSERT_EQ(0.0F, pointC[0]);
    ASSERT_EQ(0.0F, pointC[1]);
    ASSERT_EQ(1.0F, pointC[2]);

    ASSERT_EQ(0.0F, AC[0]);
    ASSERT_EQ(0.0F, AC[1]);
    ASSERT_EQ(1.0F, AC[2]);

    ASSERT_EQ(0.0F, AB[0]);
    ASSERT_EQ(1.0F, AB[1]);
    ASSERT_EQ(0.0F, AB[2]);

    const ::glm::vec3 bc {pointC - pointB};
    ASSERT_EQ(0.0F, bc[0]);
    ASSERT_EQ(-1.0F, bc[1]);
    ASSERT_EQ(1.0F, bc[2]);
}

/**
 * Tests the calculation of an AABB of a triangle.
 */
TEST_F(TestTriangle, AABB) {
    const AABB box {triangle->getAABB()};

    ASSERT_EQ(0.0F, box.getPointMin()[0]);
    ASSERT_EQ(0.0F, box.getPointMin()[1]);
    ASSERT_EQ(0.0F, box.getPointMin()[2]);

    ASSERT_EQ(0.0F, box.getPointMax()[0]);
    ASSERT_EQ(1.0F, box.getPointMax()[1]);
    ASSERT_EQ(1.0F, box.getPointMax()[2]);
}

/**
 * Tests intersecting an AABB with a Triangle.
 */
TEST_F(TestTriangle, intersectBoxInside01) {
    const ::glm::vec3 min {-1, -1, -1};
    const ::glm::vec3 max {2, 2, 2};
    assertBoxIntersectsTriangle(min, max, *this->triangle.get());
}

/**
 * Tests intersecting an AABB with a Triangle.
 */
TEST_F(TestTriangle, intersectBoxInside02) {
    const ::glm::vec3 min {0, 0, 0};
    const ::glm::vec3 max {3, 3, 3};
    assertBoxIntersectsTriangle(min, max, *this->triangle.get());
}

/**
 * Tests intersecting an AABB with a Triangle.
 */
TEST_F(TestTriangle, intersectBoxInside03) {
    const ::glm::vec3 min {0, 0, 0};
    const ::glm::vec3 max {0, 1, 1};
    assertBoxIntersectsTriangle(min, max, *this->triangle.get());
}

/**
 * Tests intersecting an AABB with a Triangle.
 */
TEST_F(TestTriangle, intersectBoxInside04) {
    const ::glm::vec3 min {0, 0, 0};
    const ::glm::vec3 max {0, 0.5, 0.5};
    assertBoxIntersectsTriangle(min, max, *this->triangle.get());
}

/**
 * Tests intersecting an AABB with a Triangle.
 */
TEST_F(TestTriangle, intersectBoxInside05) {
    const ::glm::vec3 min {-1, -1, -1};
    const ::glm::vec3 max {0.1F, 0.1F, 0.1F};
    assertBoxIntersectsTriangle(min, max, *this->triangle.get());
}

TEST_F(TestTriangle, intersectBoxInside06) {
    const ::glm::vec3 min {-1, 0.4F, 0.4F};
    const ::glm::vec3 max {1, 1.4F, 1.4F};
    assertBoxIntersectsTriangle(min, max, *this->triangle.get());
}

/**
 * Tests intersecting an AABB with a Triangle.
 */
TEST_F(TestTriangle, intersectBoxInside07) {
    const ::glm::vec3 min {-1, 0.4F, 0.7F};
    const ::glm::vec3 max {1, 1.4F, 1.4F};
    const AABB box {min, max};
    const bool intersected {triangle->intersect(box)};
    ASSERT_EQ(false, intersected);
}

/**
 * Tests intersecting an AABB with a Triangle.
 */
TEST_F(TestTriangle, intersectBoxInside08) {
    const ::glm::vec3 min {1.25F, 1.25F, 10};
    const ::glm::vec3 max {2.5F, 2.5F, 10};
    assertBoxIntersectsTriangle(min, max, triangle3);
}

/**
 * Tests intersecting an AABB with a Triangle.
 */
TEST_F(TestTriangle, intersectBoxInside09) {
    const ::glm::vec3 min {-1, -1, 10};
    const ::glm::vec3 max {11, 11, 10};
    assertBoxIntersectsTriangle(min, max, triangle2);
}

/**
 * Tests intersecting an AABB with a Triangle.
 */
TEST_F(TestTriangle, intersectBoxInside10) {
    const ::glm::vec3 min {-11.0200005F, 0.794949531F, -11.04F};
    const ::glm::vec3 max {-0.0100002289F, 11.5899992F, -0.0250005722F};
    assertBoxIntersectsTriangle(min, max, triangle3);
}

/**
 * Tests the copy constructor of a ::glm::vector.
 */
TEST_F(TestTriangle, ConstructorCOPY) {
    const ::glm::vec3 point1 {1.0F, 2.0F, 3.0F};

    ASSERT_EQ(1.0F, point1[0]);
    ASSERT_EQ(2.0F, point1[1]);
    ASSERT_EQ(3.0F, point1[2]);
}

/**
 * Tests the operator less of a ::glm::vector.
 */
TEST_F(TestTriangle, OperatorLESS) {
    const ::glm::vec3 point1 {3.0F, 2.0F, 1.0F};
    const ::glm::vec3 point2 {1.0F, 2.0F, 3.0F};
    const ::glm::vec3 vector {point2 - point1};

    ASSERT_EQ(-2.0F, vector[0]);
    ASSERT_EQ(0.0F, vector[1]);
    ASSERT_EQ(2.0F, vector[2]);
}

/**
 * Tests the operator plus of a ::glm::vector.
 */
TEST_F(TestTriangle, OperatorMORE) {
    const ::glm::vec3 vector {3.0F, 2.0F, 1.0F};
    const ::glm::vec3 point {1.0F, 2.0F, 3.0F};
    const ::glm::vec3 dest {point + vector};

    ASSERT_EQ(4.0F, dest[0]);
    ASSERT_EQ(4.0F, dest[1]);
    ASSERT_EQ(4.0F, dest[2]);
}

/**
 * Tests the intersection of a Ray with a Triangle.
 */
TEST_F(TestTriangle, intersectRayInside01) {
    const ::glm::vec3 orig {2, 0, 0};
    const ::glm::vec3 dir {::glm::vec3 {0, 0, 0} - orig};
    assertRayTriangle(orig, dir, *this->triangle.get(), true);
}

/**
 * Tests the intersection of a Ray with a Triangle.
 */
TEST_F(TestTriangle, intersectRayInside02) {
    const ::glm::vec3 orig {2, 0, 0};
    const ::glm::vec3 dir {::glm::vec3 {0, 1, 0} - orig};
    assertRayTriangle(orig, dir, *this->triangle.get(), true);
}

/**
 * Tests the intersection of a Ray with a Triangle.
 */
TEST_F(TestTriangle, intersectRayInside03) {
    const ::glm::vec3 orig {2, 0, 0};
    const ::glm::vec3 dir {::glm::vec3 {0, 0, 1} - orig};
    assertRayTriangle(orig, dir, *this->triangle.get(), true);
}

/**
 * Tests the intersection of a Ray with a Triangle.
 */
TEST_F(TestTriangle, intersectRayOutside01) {
    const ::glm::vec3 orig {2, 0, 0};
    const ::glm::vec3 dir {::glm::vec3 {0, 1.000001, 0} - orig};
    assertRayTriangle(orig, dir, *this->triangle.get(), false);
}

/**
 * Tests the intersection of a Ray with a Triangle.
 */
TEST_F(TestTriangle, intersectRayOutside02) {
    const ::glm::vec3 orig {2, 0, 0};
    const ::glm::vec3 dir {::glm::vec3 {0, 0, 1.000001} - orig};
    assertRayTriangle(orig, dir, *this->triangle.get(), false);
}

/**
 * Tests the intersection of a Ray with a Triangle.
 */
TEST_F(TestTriangle, intersectRayOutside03) {
    const ::glm::vec3 orig {2, 2, 2};
    const ::glm::vec3 dir {::glm::vec3 {0.000001, 0, 0} - orig};
    assertRayTriangle(orig, dir, *this->triangle.get(), false);
}

/**
 * Tests the intersection of a Ray with a Triangle.
 */
TEST_F(TestTriangle, intersectRayOutside04) {
    const ::glm::vec3 orig {2, 2, 2};
    const ::glm::vec3 dir {::glm::vec3 {-1, 0, 0} - orig};
    assertRayTriangle(orig, dir, *this->triangle.get(), false);
}

/**
 * Tests the intersection of a Ray with a Triangle.
 */
TEST_F(TestTriangle, intersectRayOutside05) {
    const ::glm::vec3 orig {2, 0, 0};
    const ::glm::vec3 dir {::glm::vec3 {0, -0.000001, 0} - orig};
    assertRayTriangle(orig, dir, *this->triangle.get(), false);
}

/**
 * Tests the intersection of a Ray with a Triangle.
 */
TEST_F(TestTriangle, intersectRayOutside06) {
    const ::glm::vec3 orig {2, 0, 0};
    const ::glm::vec3 dir {::glm::vec3 {0, 0, -0.000001} - orig};
    assertRayTriangle(orig, dir, *this->triangle.get(), false);
}

/**
 * Tests intersecting a Ray with a Triangle and that Ray has the origin from the same Triangle,
 * so it shouldn't intersect.
 */
TEST_F(TestTriangle, intersectRayFromPrimitive) {
    const ::glm::vec3 orig {2, 0, 0};
    const ::glm::vec3 dir {::glm::vec3 {0, 0, 0} - orig};
    assertRayTriangle(orig, dir, *this->triangle.get(), false, this->triangle.get());
}
