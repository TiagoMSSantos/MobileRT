#include "Components/Loaders/PerspectiveLoader.hpp"
#include <gtest/gtest.h>

class TestCameraLoader : public testing::Test {
protected:

    void SetUp() final {
        errno = 0;
    }

    void TearDown() final {
    }

    ~TestCameraLoader() override;
};

TestCameraLoader::~TestCameraLoader() {
}

TEST_F(TestCameraLoader, TestCameraLoader1) {
    const ::std::string data {R"(
t perspective
p 0 30.0 -200.0
l 0.0 30.0 100.0
u 0.0 1.0 0.0
f 44 45
    )"};

    ::std::stringstream stream {data};
    const float aspectRatio {1.0F};
    const ::std::unique_ptr<::MobileRT::Camera> camera {::Components::PerspectiveLoader().loadFromStream(::std::move(stream), aspectRatio)};
    const ::Components::Perspective *perspectiveCamera {dynamic_cast<const ::Components::Perspective *> (camera.get())};

    ASSERT_TRUE(perspectiveCamera != nullptr);

    ASSERT_FLOAT_EQ(perspectiveCamera->position_.x, 0.0F);
    ASSERT_FLOAT_EQ(perspectiveCamera->position_.y, 30.0F);
    ASSERT_FLOAT_EQ(perspectiveCamera->position_.z, -200.0F);

    ASSERT_FLOAT_EQ(perspectiveCamera->direction_.x, 0.0F);
    ASSERT_FLOAT_EQ(perspectiveCamera->direction_.y, 0.0F);
    ASSERT_FLOAT_EQ(perspectiveCamera->direction_.z, 1.0F);

    ASSERT_FLOAT_EQ(perspectiveCamera->up_.x, 0.0F);
    ASSERT_FLOAT_EQ(perspectiveCamera->up_.y, 1.0F);
    ASSERT_FLOAT_EQ(perspectiveCamera->up_.z, 0.0F);

    ASSERT_FLOAT_EQ(perspectiveCamera->getHFov(), 44.0F);
    ASSERT_FLOAT_EQ(perspectiveCamera->getVFov(), 45.0F);
}
