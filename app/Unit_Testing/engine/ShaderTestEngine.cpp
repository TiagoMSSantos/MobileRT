#include "System_dependent/Native/C_wrapper.h"
#include <gtest/gtest.h>

#include "MobileRT/Shader.hpp"

class ShaderTestEngine : public testing::Test {
protected:
    ::MobileRT::Config config {};

    void SetUp () final {
        config.width = 30;
        config.height = 30;
        config.threads = 3;
        config.sceneIndex = 1;
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

    void TearDown () final {
    }

    ~ShaderTestEngine () override;
};

ShaderTestEngine::~ShaderTestEngine () {
}

TEST_F(ShaderTestEngine, testRenderSceneWithNoShadows) {
    ::MobileRT::checkSystemError("testRenderSceneWithNoShadows start");

    config.sceneIndex = -1; // OBJ
    config.shader = 0; // No Shadows
    config.accelerator = ::MobileRT::Shader::Accelerator::ACC_BVH;

    config.objFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.obj"};
    config.mtlFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.mtl"};
    config.camFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.cam"};

    ASSERT_TRUE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));
    RayTrace(config, false);
    ASSERT_FALSE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));

    ::MobileRT::checkSystemError("testRenderSceneWithNoShadows end");
}

TEST_F(ShaderTestEngine, testRenderSceneWithWhitted) {
    ::MobileRT::checkSystemError("testRenderSceneWithWhitted start");

    config.sceneIndex = -1; // OBJ
    config.shader = 1; // Whitted
    config.accelerator = ::MobileRT::Shader::Accelerator::ACC_BVH;

    config.objFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.obj"};
    config.mtlFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.mtl"};
    config.camFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.cam"};

    ASSERT_TRUE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));
    RayTrace(config, false);
    ASSERT_FALSE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));

    ::MobileRT::checkSystemError("testRenderSceneWithWhitted end");
}

TEST_F(ShaderTestEngine, testRenderSceneWithPathTracing) {
    ::MobileRT::checkSystemError("testRenderSceneWithPathTracing start");

    config.sceneIndex = -1; // OBJ
    config.shader = 2; // PathTracing
    config.accelerator = ::MobileRT::Shader::Accelerator::ACC_BVH;

    config.objFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.obj"};
    config.mtlFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.mtl"};
    config.camFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.cam"};

    ASSERT_TRUE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));
    RayTrace(config, false);
    ASSERT_FALSE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));

    ::MobileRT::checkSystemError("testRenderSceneWithPathTracing end");
}

TEST_F(ShaderTestEngine, testRenderSceneWithDepthMap) {
    ::MobileRT::checkSystemError("testRenderSceneWithDepthMap start");

    config.sceneIndex = -1; // OBJ
    config.shader = 3; // DepthMap
    config.accelerator = ::MobileRT::Shader::Accelerator::ACC_BVH;

    config.objFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.obj"};
    config.mtlFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.mtl"};
    config.camFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.cam"};

    ASSERT_TRUE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));
    RayTrace(config, false);
    ASSERT_FALSE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));

    ::MobileRT::checkSystemError("testRenderSceneWithDepthMap end");
}

TEST_F(ShaderTestEngine, testRenderSceneWithDiffuse) {
    ::MobileRT::checkSystemError("testRenderSceneWithDiffuse start");

    config.sceneIndex = -1; // OBJ
    config.shader = 4; // DiffuseMaterial
    config.accelerator = ::MobileRT::Shader::Accelerator::ACC_BVH;

    config.objFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.obj"};
    config.mtlFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.mtl"};
    config.camFilePath = ::std::string {"./app/src/androidTest/resources/CornellBox/CornellBox-Water.cam"};

    ASSERT_TRUE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));
    RayTrace(config, false);
    ASSERT_FALSE(::std::all_of(config.bitmap.begin()+1, config.bitmap.end(), ::std::bind(std::equal_to<int>(), ::std::placeholders::_1, config.bitmap.front())));

    ::MobileRT::checkSystemError("testRenderSceneWithDiffuse end");
}
