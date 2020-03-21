#include "MobileRT/Intersection.hpp"
#include <gtest/gtest.h>

using ::MobileRT::Intersection;

class TestIntersection : public testing::Test {
protected:
    virtual void SetUp() {
    }

    virtual void TearDown() {
    }

    ~TestIntersection();
};

TestIntersection::~TestIntersection() {
}

/**
 * Tests the Intersection constructor with invalid parameters.
 */
TEST_F(TestIntersection, TestInvalidConstructor) {
    const auto intPoint {::glm::vec3 {10.0F, 0.0F, 10.0F}};
    const float dist {1.2F};
    const auto normal {::glm::vec3 {0.0F, 0.0F, 0.0F}};
    const auto primitive {nullptr};
    const auto materialIndex {0};
    const ::glm::vec2 texCoords {0.4F, 0.6F};

    ASSERT_DEBUG_DEATH(const Intersection intersection (intPoint, dist, normal, primitive, materialIndex, texCoords);, "");
}

/**
 * Tests the Intersection constructor with invalid parameters.
 */
TEST_F(TestIntersection, TestInvalidConstructor2) {
    const auto intPoint {::glm::vec3 {10.0F, 0.0F, 10.0F}};
    const float dist {0.0F};
    const auto normal {::glm::vec3 {0.0F, 0.0F, 10.0F}};
    const auto primitive {nullptr};
    const auto materialIndex {0};
    const ::glm::vec2 texCoords {0.4F, 0.6F};

    ASSERT_DEBUG_DEATH(const Intersection intersection (intPoint, dist, normal, primitive, materialIndex, texCoords);, "");
}

/**
 * Tests the Intersection constructor with invalid parameters.
 */
TEST_F(TestIntersection, TestInvalidConstructor3) {
    const float dist {0.0F};

    ASSERT_DEBUG_DEATH(const Intersection intersection (dist);, "");
}

/**
 * Tests the Intersection constructor.
 */
TEST_F(TestIntersection, TestConstructor) {
    const auto intPoint {::glm::vec3 {10.0F, 0.0F, 10.0F}};
    const float dist {1.2F};
    const auto normal {::glm::vec3 {0.0F, 0.0F, 10.0F}};
    const auto primitive {nullptr};
    const auto materialIndex {0};
    const ::glm::vec2 texCoords {0.4F, 0.6F};

    const Intersection intersection {intPoint, dist, normal, primitive, materialIndex, texCoords};

    ASSERT_EQ(dist, intersection.length_);
    ASSERT_EQ(primitive, intersection.primitive_);
    ASSERT_EQ(materialIndex, intersection.materialIndex_);
    for(int i {0}; i < ::MobileRT::NumberOfAxes; ++i) {
        ASSERT_FLOAT_EQ(intPoint[i], intersection.point_[i]);
        ASSERT_FLOAT_EQ(normal[i], intersection.normal_[i]);
    }
}

/**
 * Tests the Intersection constructor.
 */
TEST_F(TestIntersection, TestConstructor2) {
    const float dist {0.1F};

    const Intersection intersection {dist};

    ASSERT_EQ(dist, intersection.length_);
}
