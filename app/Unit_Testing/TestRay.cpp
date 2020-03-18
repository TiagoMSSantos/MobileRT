#include "MobileRT/Ray.hpp"
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
 * Tests the Ray constructor.
 */
TEST_F(TestRay, TestConstructor) {
    const auto direction {::glm::vec3 {10.0F, 0.0F, 10.0F}};
    const auto origin {::glm::vec3 {0.0F, 0.0F, 10.0F}};
    const auto depth {19};
    const auto primitive {nullptr};
    const auto id {0};
    const Ray ray {direction, origin, depth, primitive};

    ASSERT_EQ(depth, ray.depth_);
    ASSERT_EQ(primitive, ray.primitive_);
    ASSERT_EQ(id, ray.id_);
    for(int i {0}; i < ::MobileRT::NumberOfAxes; ++i) {
        ASSERT_FLOAT_EQ(direction[i], ray.direction_[i]);
        ASSERT_FLOAT_EQ(origin[i], ray.origin_[i]);
    }
}
