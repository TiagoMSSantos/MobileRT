#include "MobileRT/Ray.hpp"
#include "MobileRT/Utils/Constants.hpp"
#include <gtest/gtest.h>

using ::MobileRT::Ray;

class TestRay : public testing::Test {
protected:
    virtual void SetUp() {
    }

    virtual void TearDown() {
    }

    ~TestRay();
};

TestRay::~TestRay() {
}

/**
 * Tests the Ray constructor with invalid parameters.
 */
TEST_F(TestRay, TestInvalidConstructor) {
    const auto direction {::glm::vec3 {0.0F, 0.0F, 0.0F}};
    const auto origin {::glm::vec3 {0.0F, 0.0F, 10.0F}};
    const auto depth {19};
    const auto primitive {nullptr};

    ASSERT_DEBUG_DEATH(const Ray ray (direction, origin, depth, primitive);, "");
}

/**
 * Tests the Ray constructor.
 */
TEST_F(TestRay, TestConstructor) {
    const auto direction {::glm::vec3 {10.0F, 0.0F, 10.0F}};
    const auto origin {::glm::vec3 {0.0F, 0.0F, 10.0F}};
    const auto depth {19};
    const auto primitive {nullptr};
    const Ray ray {direction, origin, depth, primitive};

    ASSERT_EQ(depth, ray.depth_);
    ASSERT_EQ(primitive, ray.primitive_);
    for (int i {0}; i < ::MobileRT::NumberOfAxes; ++i) {
        ASSERT_FLOAT_EQ(direction[i], ray.direction_[i]);
        ASSERT_FLOAT_EQ(origin[i], ray.origin_[i]);
    }
}

/**
 * Tests the Ray id generator.
 */
TEST_F(TestRay, TestId) {
    const auto direction {::glm::vec3 {10.0F, 0.0F, 10.0F}};
    const auto origin {::glm::vec3 {0.0F, 0.0F, 10.0F}};
    const auto depth {19};
    const auto primitive {nullptr};
    const Ray ray1 {direction, origin, depth, primitive};
    const Ray ray2 {direction, origin, depth, primitive};

    ASSERT_EQ(ray2.id_, ray1.id_ + 1);
}
