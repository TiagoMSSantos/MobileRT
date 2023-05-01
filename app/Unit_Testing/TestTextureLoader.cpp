#include "MobileRT/Texture.hpp"
#include "MobileRT/Utils/Utils.hpp"
#include <gtest/gtest.h>

using ::MobileRT::Texture;

class TestTextureLoader : public testing::Test {
protected:

    void SetUp() final {
    }

    void TearDown() final {
    }

    ~TestTextureLoader() override;
};

TestTextureLoader::~TestTextureLoader() {
}
