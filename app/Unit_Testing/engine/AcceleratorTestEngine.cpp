#include "System_dependent/Native/C_wrapper.h"
#include <gtest/gtest.h>

#include "MobileRT/Shader.hpp"

class AcceleratorTestEngine : public testing::Test {
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

    ~AcceleratorTestEngine () override;
};

AcceleratorTestEngine::~AcceleratorTestEngine () {
}

TEST_F(AcceleratorTestEngine, testRenderSceneWithNaive) {
    config.sceneIndex = -1; // OBJ
    config.shader = 1; // Whitted
    config.accelerator = ::MobileRT::Shader::Accelerator::ACC_NAIVE;

    config.objFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.obj"};
    config.mtlFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.mtl"};
    config.camFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.cam"};

    RayTrace(config, false);
}

TEST_F(AcceleratorTestEngine, testRenderSceneWithRegularGrid) {
    config.sceneIndex = -1; // OBJ
    config.shader = 1; // Whitted
    config.accelerator = ::MobileRT::Shader::Accelerator::ACC_REGULAR_GRID;

    config.objFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.obj"};
    config.mtlFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.mtl"};
    config.camFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.cam"};

    RayTrace(config, false);
}

TEST_F(AcceleratorTestEngine, testRenderSceneWithBVH) {
    config.sceneIndex = -1; // OBJ
    config.shader = 1; // Whitted
    config.accelerator = ::MobileRT::Shader::Accelerator::ACC_BVH;

    config.objFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.obj"};
    config.mtlFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.mtl"};
    config.camFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.cam"};

    RayTrace(config, false);
}
