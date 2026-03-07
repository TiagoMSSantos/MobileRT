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

const ::std::vector<::std::int32_t> expectedBitmap {
    -15526892, 0, 0, 0, -15527148, 0, 0, -15526891, 0, 0, 0, -15461099, 0, 0, -15526891, 0, 0, 0, -15526892, 0, 0, -15526891, 0, 0, 0, -15592684, 0, 0, -15592684, 0, 0, 0, -15592684, 0, 0, -15526892, 0, 0, 0, -15461099, 0, 0, -15461099, 0, 0, 0, -15461355, 0, 0, -15526891, 0, 0, 0, -15526891, 0, 0, -15526891, 0, 0, 0, -15592684, 0, 0, -15461355, 0, 0, 0, -15526891, 0, 0, -15395562, 0, 0, 0, -15000548, 0, 0, -15461099, 0, 0, 0, -15395306, 0, 0, -15526891, 0, 0, 0, -15526892, 0, 0, -15920724, 0, 0, 0, -15526892, 0, 0, -15526891, 0, 0, 0, -1, 0, 0, -1, 0, 0, 0, -15197670, 0, 0, 0, -15526892, 0, 0, -15526892, 0, 0, 0, -15986532, 0, 0, -15592684, 0, 0, 0, -15461099, 0, 0, -15329512, 0, 0, 0, -1, 0, 0, -1, 0, 0, 0, -15329512, 0, 0, -15526892, 0, 0, 0, -8968915, 0, 0, -15920725, 0, 0, 0, -15526892, 0, 0, -15526892, 0, 0, 0, -15461099, 0, 0, -15461099, 0, 0, 0, -15395562, 0, 0, -15526892, 0, 0, 0, -15527148, 0, 0, -9100244, 0, 0, 0, -15920731, 0, 0, -15592685, 0, 0, 0, -15527148, 0, 0, -15526891, 0, 0, 0, -15527148, 0, 0, -15527148, 0, 0, 0, -15592684, 0, 0, -9428438, 0, 0, 0, -15920728, 0, 0, -15986527, 0, 0, 0, -7959409, 0, 0, -5459017, 0, 0, 0, -4603194, 0, 0, 0, -6314326, 0, 0, -7498601, 0, 0, 0, -10018778, 0, 0, -9231317, 0, 0, 0, -16052329, 0, 0, -7235429, 0, 0, 0, -4735037, 0, 0, -4471864, 0, 0, 0, -4340279, 0, 0, -4932416, 0, 0, 0, -8025203, 0, 0, -9625047, 0, 0, 0, -15920728, 0, 0, -16052332, 0, 0, 0, -6314326, 0, 0, -5064259, 0, 0, 0, -4735037, 0, 0, -4537658, 0, 0, 0, -7827823, 0, 0, -10806238, 0, 0, 0, -9821912, 0, 0, -15920730, 0, 0, 0, -7103843, 0, 0, -5590346, 0, 0, 0, -5064002, 0, 0, -4866623, 0, 0, 0, -6116947, 0, 0, -7367272, 0, 0, 0, -10018778, 0, 0, -15986532, 0, 0, 0, -15986793, 0, 0, 0, -6577755, 0, 0, -6248534, 0, 0, 0, -5129795, 0, 0, -5656396, 0, 0, 0, -6116947, 0, 0, -11068639, 0, 0, 0, -9887705, 0, 0, -15986791, 0, 0, 0, -8288374, 0, 0, -6314327, 0, 0, 0, -5853775, 0, 0, -5722189, 0, 0, 0, -5787982, 0, 0, -8420217, 0, 0, 0, -10543837, 0, 0, -16052593, 0, 0, 0, -16052333, 0, 0, -7367015, 0, 0, 0, -7301479, 0, 0, -5853519, 0, 0, 0, -6116947, 0, 0, -7038306, 0, 0, 0, -10871774, 0, 0, -10740702, 0, 0, 0, -16118397, 0, 0, -16118394, 0, 0, 0, -8090995, 0, 0, -6775134, 0, 0, 0, -7169892, 0, 0, -7367271, 0, 0, 0, -8288374, 0, 0, -11527906, 0, 0, 0, -16052593, 0, 0, 0, -16118399, 0, 0, -7893616, 0, 0, 0, -7893616, 0, 0, -6577498, 0, 0, 0, -6840671, 0, 0, -8288374, 0, 0, 0, -11593698, 0, 0, -10740702, 0, 0, 0, -16118137, 0, 0, -16250005, 0, 0, 0, -15526892, 0, 0, -7564651, 0, 0, 0, -7827823, 0, 0, -7498858, 0, 0, 0, -14808564, 0, 0, -11659235, 0, 0, 0, -16118135, 0, 0, -16184195, 0, 0, 0, -16184208, 0, 0, -15461099, 0, 0, 0, -8091252, 0, 0, -8814975, 0, 0, 0, -16316408, 0, 0, -14677492, 0, 0, 0, -11396833, 0, 0, -16184195, 0, 0, 0, -16184203, 0, 0, -16118405, 0, 0, 0, -9887192, 0, 0, -8156788, 0, 0, 0, -16316665, 0, 0, 0, -16777216, 0, 0, -15333624, 0, 0, 0, -16250005, 0, 0, -16184195, 0, 0, 0, -16184465, 0, 0, -16777216, 0, 0, 0, -9472906, 0, 0, -13816016, 0, 0, 0, -16777216, 0, 0, -15464953, 0, 0, 0, -11134176, 0, 0, -16250264, 0, 0, 0, -14474203, 0, 0, -16447964, 0, 0, 0, -16250872, 0, 0, -14145238, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -15202551, 0, 0, -11396833, 0, 0, 0, -16448219, 0, 0, -16645343, 0, 0, 0, -16250872, 0, 0, -14545906, 0, 0, 0, -16645630, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -15005172, 0, 0, 0, -16448219, 0, 0, -16448222, 0, 0, 0, -16579834, 0, 0, -11842741, 0, 0, 0, -13026243, 0, 0, 0, -16711157, 0, 0, -16777216, 0, 0, 0, -16317950, 0, 0, -15202038, 0, 0, 0, -13816016, 0, 0, -14013652, 0, 0, 0, -16316408, 0, 0, -16250615, 0, 0, 0, -16645630, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -15071222, 0, 0, 0, -14408410, 0, 0, -16447954, 0, 0, 0, -16250872, 0, 0, -16316664, 0, 0, 0, -16250871, 0, 0, -12960450, 0, 0, 0, -16711423, 0, 0, -16055291, 0, 0, 0, -15136502, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0, -16777216, 0, 0, -16777216, 0, 0, 0
};

static bool pixelEqual(const ::std::uint32_t a, const ::std::uint32_t b, const ::std::int32_t maxAcceptableDiff) {
    const ::std::int64_t diff {::std::abs(static_cast<::std::int64_t>(a) - static_cast<::std::int64_t>(b))};
    if (diff > maxAcceptableDiff) {
        return false;
    }
    return true;
}

static void assertVectorsEqual(const std::vector<::std::int32_t>& a, const std::vector<::std::int32_t>& b, const ::std::int32_t maxAcceptableDiff) {
    if (a.size() != b.size()) {
        std::ostringstream oss;
        oss << "Vector sizes differ: a.size()=" << a.size() << ", b.size()=" << b.size();
        throw std::runtime_error(oss.str());
    }
    for (size_t i = 0; i < a.size(); ++i) {
        const ::std::uint32_t pixel1Unsigned {static_cast<::std::uint32_t> (a[i])};
        const ::std::uint32_t pixel2Unsigned {static_cast<::std::uint32_t> (b[i])};

        const ::std::uint32_t red1 {pixel1Unsigned & 0xFFU};
        const ::std::uint32_t green1 {(pixel1Unsigned >> 8U) & 0xFFU};
        const ::std::uint32_t blue1 {(pixel1Unsigned >> 16U) & 0xFFU};

        const ::std::uint32_t red2 {pixel2Unsigned & 0xFFU};
        const ::std::uint32_t green2 {(pixel2Unsigned >> 8U) & 0xFFU};
        const ::std::uint32_t blue2 {(pixel2Unsigned >> 16U) & 0xFFU};
        if (!pixelEqual(red1, red2, maxAcceptableDiff) || !pixelEqual(green1, green2, maxAcceptableDiff) || !pixelEqual(blue1, blue2, maxAcceptableDiff)) {
            std::ostringstream oss;
            oss << "Vectors differ at index " << i << ": a[i]=" << pixel1Unsigned << ", b[i]=" << pixel2Unsigned;
            oss << " (red1=" << red1 << ", green1=" << green1 << ", blue1=" << blue1;
            oss << ", red2=" << red2 << ", green2=" << green2 << ", blue2=" << blue2 << ")";
            throw std::runtime_error(oss.str());
        }
    }
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

    assertVectorsEqual(config.bitmap, expectedBitmap, 43);

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

    assertVectorsEqual(config.bitmap, expectedBitmap, 44);

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

    assertVectorsEqual(config.bitmap, expectedBitmap, 43);

    ::MobileRT::checkSystemError("testRenderSceneWithBVH end");
}
