#include "System_dependent/Native/C_wrapper.h"
#include <gtest/gtest.h>

#include "MobileRT/Shader.hpp"

class AcceleratorTestEngine : public testing::Test {
protected:
    ::MobileRT::Config config {};

    void SetUp () final {
        errno = 0;
        config.width = 30;
        config.height = 30;
        config.threads = 3;
        config.samplesPixel = 1;
        config.samplesLight = 1;
        config.repeats = 1;
        config.objFilePath = ::std::string {""};
        config.mtlFilePath = ::std::string {""};
        config.camFilePath = ::std::string {""};
        const ::std::uint32_t size {static_cast<::std::uint32_t> (config.width) * static_cast<::std::uint32_t> (config.height)};
        config.bitmap = ::std::vector<::std::int32_t> (size);
    }

    void TearDown () override {
    }

    ~AcceleratorTestEngine () override;
};

AcceleratorTestEngine::~AcceleratorTestEngine () {
}

TEST_F(AcceleratorTestEngine, testRenderSceneWithNaive) {
    ::MobileRT::checkSystemError("testRenderSceneWithNaive start");

    config.sceneIndex = -1; // OBJ
    config.shader = 1; // Whitted
    config.accelerator = ::MobileRT::Shader::Accelerator::ACC_NAIVE;

    config.objFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.obj"};
    config.mtlFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.mtl"};
    config.camFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.cam"};

    ASSERT_TRUE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));
    RayTrace(config, false);
    ASSERT_FALSE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));

    ::MobileRT::checkSystemError("testRenderSceneWithNaive end");
}

TEST_F(AcceleratorTestEngine, testRenderSceneWithRegularGrid) {
    ::MobileRT::checkSystemError("testRenderSceneWithRegularGrid start");
    config.sceneIndex = -1; // OBJ
    config.shader = 1; // Whitted
    config.accelerator = ::MobileRT::Shader::Accelerator::ACC_REGULAR_GRID;

    config.objFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.obj"};
    config.mtlFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.mtl"};
    config.camFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.cam"};

    ASSERT_TRUE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));
    RayTrace(config, false);
    ASSERT_FALSE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));

    ::MobileRT::checkSystemError("testRenderSceneWithRegularGrid end");
}

TEST_F(AcceleratorTestEngine, testRenderSceneWithBVH) {
    ::MobileRT::checkSystemError("testRenderSceneWithBVH start");
    config.sceneIndex = -1; // OBJ
    config.shader = 1; // Whitted
    config.accelerator = ::MobileRT::Shader::Accelerator::ACC_BVH;

    config.objFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.obj"};
    config.mtlFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.mtl"};
    config.camFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.cam"};

    ASSERT_TRUE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));
    RayTrace(config, false);
    ASSERT_FALSE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));

    ::MobileRT::checkSystemError("testRenderSceneWithBVH end");
}
