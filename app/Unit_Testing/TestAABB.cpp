#include "MobileRT/Accelerators/AABB.hpp"
#include <gtest/gtest.h>

using ::MobileRT::AABB;

class TestAABB : public testing::Test {
protected:
    virtual void SetUp() {
    }

    virtual void TearDown() {
    }

    ~TestAABB();
};

TestAABB::~TestAABB() {
}

/**
 * Tests the AABB constructor with invalid parameters.
 */
TEST_F(TestAABB, TestInvalidConstructor) {
    const auto pointMin {::glm::vec3 {0.0F, 0.0F, 10.0F}};
    const auto pointMax {::glm::vec3 {0.0F, 0.0F, 10.0F}};

    ASSERT_DEBUG_DEATH(const AABB box (pointMin, pointMax);, "");
}

/**
 * Tests the AABB constructor.
 */
TEST_F(TestAABB, TestConstructor) {
    const auto pointMin {::glm::vec3 {1.0F, 2.0F, 3.0F}};
    const auto pointMax {::glm::vec3 {4.0F, 5.0F, 6.0F}};
    const AABB box (pointMin, pointMax);

    for(int i {0}; i < ::MobileRT::NumberOfAxes; ++i) {
        ASSERT_FLOAT_EQ(box.getPointMin()[i], pointMin[i]);
        ASSERT_FLOAT_EQ(box.getPointMax()[i], pointMax[i]);
    }
}
