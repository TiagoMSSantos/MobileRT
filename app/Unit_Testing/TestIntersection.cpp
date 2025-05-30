#include "MobileRT/Intersection.hpp"
#include <gtest/gtest.h>

using ::MobileRT::Intersection;
using ::MobileRT::Ray;

class TestIntersection : public testing::Test {
protected:
    void SetUp () final {
        errno = 0;
    }

    void TearDown () final {
    }

    ~TestIntersection () override;
};

TestIntersection::~TestIntersection () {
}

/**
 * Tests the Intersection constructor with invalid parameters.
 * In this case the normal has length of 0 and should be 1.
 */
TEST_F(TestIntersection, TestInvalidConstructor) {
    const ::glm::vec3 intPoint {::glm::vec3 {10.0F, 0.0F, 10.0F}};
    const float dist {1.2F};

    // Normal must have a length of 1.
    const ::glm::vec3 normal {::glm::vec3 {0.0F, 0.0F, 0.0F}};
    const void *primitive {nullptr};
    const ::std::int32_t materialIndex {0};
    const ::glm::vec2 texCoords {0.4F, 0.6F};
    Ray ray {::glm::vec3 {1.0F}, ::glm::vec3 {}, 0, false};

    ASSERT_DEBUG_DEATH(const Intersection intersection(::std::move(ray), intPoint, dist, normal, primitive, materialIndex, texCoords);, "");
}

/**
 * Tests the Intersection constructor with invalid parameters.
 * In this case the normal has length of 10 and should be 1.
 */
TEST_F(TestIntersection, TestInvalidConstructor2) {
    const ::glm::vec3 intPoint {::glm::vec3 {10.0F, 0.0F, 10.0F}};
    const float dist {0.0F};
    const ::glm::vec3 normal {::glm::vec3 {0.0F, 0.0F, 10.0F}};
    const void *primitive {nullptr};
    const ::std::int32_t materialIndex {0};
    const ::glm::vec2 texCoords {0.4F, 0.6F};
    Ray ray {::glm::vec3 {1.0F}, ::glm::vec3 {}, 0, false};

    ASSERT_DEBUG_DEATH(const Intersection intersection(::std::move(ray), intPoint, dist, normal, primitive, materialIndex, texCoords);, "");
}

/**
 * Tests the Intersection constructor with invalid parameters.
 * In this case the distance is of length 0 and should be a positive value.
 */
TEST_F(TestIntersection, TestInvalidConstructor3) {
    const float dist {0.0F};
    Ray ray {::glm::vec3 {1.0F}, ::glm::vec3 {}, 0, false};

    ASSERT_DEBUG_DEATH(const Intersection intersection(::std::move(ray), dist);, "");
}

/**
 * Tests the Intersection constructor.
 */
TEST_F(TestIntersection, TestConstructor) {
    const ::glm::vec3 intPoint {::glm::vec3 {10.0F, 0.0F, 10.0F}};
    const float dist {1.2F};
    const ::glm::vec3 normal {::glm::normalize(::glm::vec3 {0.0F, 0.0F, 10.0F})};
    const void *primitive {nullptr};
    const ::std::int32_t materialIndex {0};
    const ::glm::vec2 texCoords {0.4F, 0.6F};
    Ray ray {::glm::vec3 {1.0F}, ::glm::vec3 {}, 0, false};

    const Intersection intersection {::std::move(ray), intPoint, dist, normal, primitive, materialIndex, texCoords};

    ASSERT_EQ(dist, intersection.length_);
    ASSERT_EQ(primitive, intersection.primitive_);
    ASSERT_EQ(materialIndex, intersection.materialIndex_);
    for (int i {0}; i < ::MobileRT::NumberOfAxes; ++i) {
        ASSERT_FLOAT_EQ(intPoint[i], intersection.point_[i]);
        ASSERT_FLOAT_EQ(normal[i], intersection.normal_[i]);
    }
}

/**
 * Tests the Intersection constructor.
 */
TEST_F(TestIntersection, TestConstructor2) {
    const float dist {0.1F};
    Ray ray {::glm::vec3 {1.0F}, ::glm::vec3 {}, 0, false};

    const Intersection intersection {::std::move(ray), dist};

    ASSERT_EQ(dist, intersection.length_);
}
