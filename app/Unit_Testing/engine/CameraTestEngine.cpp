#include "System_dependent/Native/C_wrapper.h"
#include <gtest/gtest.h>

#include "MobileRT/Shader.hpp"

class CameraTestEngine : public testing::Test {
protected:
    ::MobileRT::Config config {};

    void SetUp () final {
        config.width = 30;
        config.height = 30;
        config.threads = 3;
        config.samplesPixel = 1;
        config.samplesLight = 1;
        config.repeats = 1;
        config.printStdOut = true;
        config.objFilePath = ::std::string {""};
        config.mtlFilePath = ::std::string {""};
        config.camFilePath = ::std::string {""};
        const ::std::uint32_t size {static_cast<::std::uint32_t> (config.width) * static_cast<::std::uint32_t> (config.height)};
        config.bitmap = ::std::vector<::std::int32_t> (size);
    }

    void TearDown () override {
    }

    ~CameraTestEngine () override;
};

CameraTestEngine::~CameraTestEngine () {
}

TEST_F(CameraTestEngine, testRenderSceneWithOrthographic) {
    ::MobileRT::checkSystemError("testRenderSceneWithOrthographic start");

    config.sceneIndex = 1; // Spheres
    config.shader = 1; // Whitted
    config.accelerator = ::MobileRT::Shader::Accelerator::ACC_BVH;

    ASSERT_TRUE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));
    RayTrace(config, false);
    ASSERT_FALSE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));

    ::MobileRT::checkSystemError("testRenderSceneWithOrthographic end");
}

TEST_F(CameraTestEngine, testRenderSceneWithPerspective) {
    ::MobileRT::checkSystemError("testRenderSceneWithPerspective start");

    config.sceneIndex = 0; // CornellBox
    config.shader = 1; // Whitted
    config.accelerator = ::MobileRT::Shader::Accelerator::ACC_BVH;

    ASSERT_TRUE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));
    RayTrace(config, false);
    ASSERT_FALSE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));

    ::MobileRT::checkSystemError("testRenderSceneWithPerspective end");
}
