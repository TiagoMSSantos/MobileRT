#include "MobileRT/Texture.hpp"
#include "MobileRT/Utils/Utils.hpp"
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
}
