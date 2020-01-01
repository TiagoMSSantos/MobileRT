#include "MobileRT/Texture.hpp"
#include "MobileRT/Utils.hpp"
#include <gtest/gtest.h>

using ::MobileRT::Texture;

class TestTextureLoader : public testing::Test {
protected:

    virtual void SetUp() {
    }

    virtual void TearDown() {
    }

    ~TestTextureLoader();
};

TestTextureLoader::~TestTextureLoader() {
    LOG("TestTextureLoader DESTROYED!!!");
}

TEST_F(TestTextureLoader, TestTextureLoader1) {
    const auto texture {Texture::createTexture("/mnt/D/Projects/San_Miguel/textures/FL29pet1.png")};

    ASSERT_TRUE(texture.isValid());
}
