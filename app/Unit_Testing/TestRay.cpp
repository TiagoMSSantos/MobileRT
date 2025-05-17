#include "MobileRT/Ray.hpp"
#include "MobileRT/Utils/Constants.hpp"
#include <gtest/gtest.h>

using ::MobileRT::Ray;

class TestRay : public testing::Test {
protected:
    void SetUp() final {
        errno = 0;
    }

    void TearDown() final {
    }

    ~TestRay() override;
};

TestRay::~TestRay() {
}

/**
 * Tests the Ray constructor with invalid parameters.
 */
TEST_F(TestRay, TestInvalidConstructor) {
    const ::glm::vec3 direction {::glm::vec3 {0.0F, 0.0F, 0.0F}};
    const ::glm::vec3 origin {::glm::vec3 {0.0F, 0.0F, 10.0F}};
    const ::std::int32_t depth {19};
    const void* primitive {nullptr};

    ASSERT_DEBUG_DEATH(const Ray ray (direction, origin, depth, false, primitive);, "");
}

/**
 * Tests the Ray constructor.
 */
TEST_F(TestRay, TestConstructor) {
    const ::glm::vec3 direction {::glm::vec3 {10.0F, 0.0F, 10.0F}};
    const ::glm::vec3 origin {::glm::vec3 {0.0F, 0.0F, 10.0F}};
    const ::std::int32_t depth {19};
    const void* primitive {nullptr};
    const Ray ray {direction, origin, depth, false, primitive};

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
    const ::glm::vec3 direction {::glm::vec3 {10.0F, 0.0F, 10.0F}};
    const ::glm::vec3 origin {::glm::vec3 {0.0F, 0.0F, 10.0F}};
    const ::std::int32_t depth {19};
    const void* primitive {nullptr};
    const Ray ray1 {direction, origin, depth, false, primitive};
    const Ray ray2 {direction, origin, depth, false, primitive};

    ASSERT_EQ(ray2.id_, ray1.id_ + 1);
}
