#include "MobileRT/Accelerators/AABB.hpp"
#include <gtest/gtest.h>

using ::MobileRT::AABB;

class TestAABB : public testing::Test {
protected:
    virtual void SetUp () {
    }

    virtual void TearDown () {
    }

    ~TestAABB ();
};

TestAABB::~TestAABB () {
}

namespace {
    const AABB box1 {::glm::vec3 {0.0F, 0.0F, 0.0F}, ::glm::vec3 {1.0F, 0.0F, 0.0F}};
}//namespace

/**
 * Tests the AABB constructor with invalid parameters.
 */
TEST_F(TestAABB, TestInvalidConstructor) {
    const auto pointMin {::glm::vec3 {0.0F, 0.0F, 10.0F}};
    const auto pointMax {::glm::vec3 {0.0F, 0.0F, 10.0F}};

    ASSERT_DEBUG_DEATH(const AABB box(pointMin, pointMax);, "");
}

/**
 * Tests the AABB constructor.
 */
TEST_F(TestAABB, TestConstructor) {
    const auto pointMin {::glm::vec3 {1.0F, 2.0F, 3.0F}};
    const auto pointMax {::glm::vec3 {4.0F, 5.0F, 6.0F}};
    const AABB box(pointMin, pointMax);

    for (int i {0}; i < ::MobileRT::NumberOfAxes; ++i) {
        ASSERT_FLOAT_EQ(box.getPointMin()[i], pointMin[i]);
        ASSERT_FLOAT_EQ(box.getPointMax()[i], pointMax[i]);
    }
}

/**
 * Tests the calculation of the centroid in an AABB.
 */
TEST_F(TestAABB, TestCentroid) {
    const ::glm::vec3 expectedCentroid {0.5F, 0.0F, 0.0F};

    ASSERT_EQ(box1.getCentroid(), expectedCentroid);
}

/**
 * Tests the calculation of the surface area in an AABB.
 */
TEST_F(TestAABB, TestSurfaceArea) {
    const auto expectedSurfaceArea {0.0F};

    ASSERT_EQ(box1.getSurfaceArea(), expectedSurfaceArea);
}

/**
 * Tests the calculation of the surface area in an AABB.
 */
TEST_F(TestAABB, TestSurfaceArea2) {
    const AABB box2 {::glm::vec3 {0.0F, 0.0F, 0.0F}, ::glm::vec3 {1.0F, 1.0F, 0.0F}};
    const auto expectedSurfaceArea {2.0F};

    ASSERT_EQ(box2.getSurfaceArea(), expectedSurfaceArea);
}

/**
 * Tests the calculation of the minimum point in an AABB.
 */
TEST_F(TestAABB, TestMinPoint) {
    const auto pointMin {::glm::vec3 {0.0F, 0.0F, 0.0F}};

    ASSERT_EQ(box1.getPointMin(), pointMin);
}

/**
 * Tests the calculation of the maximum point in an AABB.
 */
TEST_F(TestAABB, TestMaxPoint) {
    const auto pointMax {::glm::vec3 {1.0F, 0.0F, 0.0F}};

    ASSERT_EQ(box1.getPointMax(), pointMax);
}

/**
 * Tests the calculation of the surrounding box of 2 AABBs.
 */
TEST_F(TestAABB, TestSurroundingBox) {
    const AABB box2 {::glm::vec3 {0.0F, 1.0F, 0.0F}, ::glm::vec3 {1.0F, 1.0F, 0.0F}};
    const auto surroundingBox {::MobileRT::surroundingBox(box1, box2)};

    const auto expectedMin {::glm::vec3 {0.0F, 0.0F, 0.0F}};
    const auto expectedMax {::glm::vec3 {1.0F, 1.0F, 0.0F}};

    ASSERT_EQ(surroundingBox.getPointMin(), expectedMin);
    ASSERT_EQ(surroundingBox.getPointMax(), expectedMax);
}
