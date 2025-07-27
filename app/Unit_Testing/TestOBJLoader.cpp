#include "Components/Loaders/OBJLoader.hpp"
#include "Components/Samplers/Constant.hpp"
#include <gtest/gtest.h>

class TestOBJLoader : public testing::Test {
protected:

    ::MobileRT::Scene scene {};
    ::std::unordered_map<::std::string, ::MobileRT::Texture> texturesCache {};
    const ::std::function<::std::unique_ptr<::MobileRT::Sampler>()> samplerForLights {
        []() {return ::MobileRT::std::make_unique<Components::Constant>(0.0F);}
    };

    void SetUp() final {
        errno = 0;
        ASSERT_TRUE(this->scene.triangles_.empty());
        ASSERT_TRUE(this->scene.spheres_.empty());
        ASSERT_TRUE(this->scene.planes_.empty());
        ASSERT_TRUE(this->scene.lights_.empty());
        ASSERT_TRUE(this->scene.materials_.empty());
        ASSERT_TRUE(this->texturesCache.empty());
    }

    void TearDown() final {
    }

    ~TestOBJLoader() override;
};

TestOBJLoader::~TestOBJLoader() {
}

TEST_F(TestOBJLoader, testLoadingSingleTriangleWithoutMaterial) {
    const ::std::string objDefinition {R"(
v 0 0 0
v 0 0 1
v 0 1 0
f 1 2 3
    )"};

    const ::std::string mtlDefinition {R"(
    )"};

    const ::std::istringstream isObj {objDefinition};
    const ::std::istringstream isMtl {mtlDefinition};

    ::Components::OBJLoader objLoader {::std::istream {isObj.rdbuf()}, ::std::istream {isMtl.rdbuf()}};
    ASSERT_TRUE(objLoader.isProcessed());
    ASSERT_TRUE(objLoader.fillScene(&this->scene, this->samplerForLights, "test", &this->texturesCache));

    // Validate triangle was inserted into the scene.
    ASSERT_EQ(1, this->scene.triangles_.size());
    const ::glm::vec3 expectedA {0.0, 0.0, 0.0};
    const ::glm::vec3 expectedB {0.0, 0.0, 1.0};
    const ::glm::vec3 expectedC {0.0, 1.0, 0.0};
    ASSERT_EQ(expectedA, this->scene.triangles_[0].getA());
    ASSERT_EQ(expectedB - expectedA, this->scene.triangles_[0].getAB());
    ASSERT_EQ(expectedC - expectedA, this->scene.triangles_[0].getAC());

    // Validate material was inserted into the scene.
    ASSERT_EQ(1, this->scene.materials_.size());
    const ::MobileRT::Material expectedMaterial {
        // By default the diffuse color is white.
        ::glm::vec3 {1, 1, 1}, ::glm::vec3 {}, ::glm::vec3 {}, 1.0, ::glm::vec3 {}
    };
    for (int i {0}; i < 3; ++i) {
        ASSERT_FLOAT_EQ(expectedMaterial.Kd_[i], this->scene.materials_[0].Kd_[i]);
        ASSERT_FLOAT_EQ(expectedMaterial.Ks_[i], this->scene.materials_[0].Ks_[i]);
        ASSERT_FLOAT_EQ(expectedMaterial.Kt_[i], this->scene.materials_[0].Kt_[i]);
        ASSERT_FLOAT_EQ(expectedMaterial.Le_[i], this->scene.materials_[0].Le_[i]);
    }
    ASSERT_EQ(expectedMaterial.refractiveIndice_, this->scene.materials_[0].refractiveIndice_);
    ASSERT_EQ(expectedMaterial.texture_, this->scene.materials_[0].texture_);

    // Validate the rest was not updated.
    ASSERT_TRUE(this->scene.spheres_.empty());
    ASSERT_TRUE(this->scene.planes_.empty());
    ASSERT_TRUE(this->scene.lights_.empty());
    ASSERT_TRUE(this->texturesCache.empty());
}

TEST_F(TestOBJLoader, testLoadingSingleTriangleWithMaterial) {
    const ::std::string objDefinition {R"(
mtllib materials
g Base
usemtl material1
v 0 0 0
v 0 0 1
v 0 1 0
f 1 2 3
    )"};

    const ::std::string mtlDefinition {R"(
newmtl material1
Ns 2
Ni 1.5
d 0.4 # A value of 1.0 for "d" (dissolve) is the default and means fully opaque
# others use 'Tr' (inverted: Tr = 1 - d) => same as kT in MobileRT
# MobileRT kT = transmittance * (1 - dissolve)
Tf 1 2 3
illum 2
Ka 1 2 3
Kd 4 5 6
Ks 7 8 9
Ke 0 0 0 # Make sure the triangle is not a light.
    )"};

    const ::std::istringstream isObj {objDefinition};
    const ::std::istringstream isMtl {mtlDefinition};
    ::Components::OBJLoader objLoader {::std::istream {isObj.rdbuf()}, ::std::istream {isMtl.rdbuf()}};
    ASSERT_TRUE(objLoader.isProcessed());
    ASSERT_TRUE(objLoader.fillScene(&this->scene, this->samplerForLights, "test", &this->texturesCache));

    // Validate triangle was inserted into the scene.
    ASSERT_EQ(1, this->scene.triangles_.size());
    const ::glm::vec3 expectedA {0.0, 0.0, 0.0};
    const ::glm::vec3 expectedB {0.0, 0.0, 1.0};
    const ::glm::vec3 expectedC {0.0, 1.0, 0.0};
    ASSERT_EQ(expectedA, this->scene.triangles_[0].getA());
    ASSERT_EQ(expectedB - expectedA, this->scene.triangles_[0].getAB());
    ASSERT_EQ(expectedC - expectedA, this->scene.triangles_[0].getAC());

    // Validate material was inserted into the scene.
    ASSERT_EQ(1, this->scene.materials_.size());
    const ::MobileRT::Material expectedMaterial {
        ::glm::vec3 {4, 5, 6},
        ::glm::vec3 {7, 8, 9},
        ::glm::vec3 {(1 - 0.4) * 1, (1 - 0.4) * 2, (1 - 0.4) * 3},
        1.5,
        ::glm::vec3 {0, 0, 0}
    };
    for (int i {0}; i < 3; ++i) {
        ASSERT_FLOAT_EQ(expectedMaterial.Kd_[i], this->scene.materials_[0].Kd_[i]);
        ASSERT_FLOAT_EQ(expectedMaterial.Ks_[i], this->scene.materials_[0].Ks_[i]);
        ASSERT_FLOAT_EQ(expectedMaterial.Kt_[i], this->scene.materials_[0].Kt_[i]);
        ASSERT_FLOAT_EQ(expectedMaterial.Le_[i], this->scene.materials_[0].Le_[i]);
    }
    ASSERT_EQ(expectedMaterial.refractiveIndice_, this->scene.materials_[0].refractiveIndice_);
    ASSERT_EQ(expectedMaterial.texture_, this->scene.materials_[0].texture_);

    // Validate the rest was not updated.
    ASSERT_TRUE(this->scene.spheres_.empty());
    ASSERT_TRUE(this->scene.planes_.empty());
    ASSERT_TRUE(this->scene.lights_.empty());
    ASSERT_TRUE(this->texturesCache.empty());
}

TEST_F(TestOBJLoader, testLoadingSingleTriangleLight) {
    const ::std::string objDefinition {R"(
mtllib materials
g Base
usemtl material1
v 0 0 0
v 0 0 1
v 0 1 0
f 1 2 3
    )"};

    const ::std::string mtlDefinition {R"(
newmtl material1
Ns 2
Ni 1.5
d 0.4 # A value of 1.0 for "d" (dissolve) is the default and means fully opaque
# others use 'Tr' (inverted: Tr = 1 - d) => same as kT in MobileRT
# MobileRT kT = transmittance * (1 - dissolve)
Tf 1 2 3
illum 2
Ka 1 2 3
Kd 4 5 6
Ks 7 8 9
Ke 1 2 3 # Make sure the triangle is a light and needs to be normalized.
    )"};

    const ::std::istringstream isObj {objDefinition};
    const ::std::istringstream isMtl {mtlDefinition};
    ::Components::OBJLoader objLoader {::std::istream {isObj.rdbuf()}, ::std::istream {isMtl.rdbuf()}};
    ASSERT_TRUE(objLoader.isProcessed());
    ASSERT_TRUE(objLoader.fillScene(&this->scene, this->samplerForLights, "test", &this->texturesCache));

    // Validate a light was inserted into the scene.
    ASSERT_EQ(1, this->scene.lights_.size());

    // Validate material was inserted into the scene.
    const ::MobileRT::Material expectedMaterial {
        ::glm::vec3 {4, 5, 6},
        ::glm::vec3 {7, 8, 9},
        ::glm::vec3 {(1 - 0.4) * 1, (1 - 0.4) * 2, (1 - 0.4) * 3},
        1.5,
        ::glm::vec3 {static_cast<float> (1) / 3, static_cast<float> (2) / 3, static_cast<float> (3) / 3}
    };
    for (int i {0}; i < 3; ++i) {
        ASSERT_FLOAT_EQ(expectedMaterial.Kd_[i], this->scene.lights_[0]->radiance_.Kd_[i]);
        ASSERT_FLOAT_EQ(expectedMaterial.Ks_[i], this->scene.lights_[0]->radiance_.Ks_[i]);
        ASSERT_FLOAT_EQ(expectedMaterial.Kt_[i], this->scene.lights_[0]->radiance_.Kt_[i]);
        ASSERT_FLOAT_EQ(expectedMaterial.Le_[i], this->scene.lights_[0]->radiance_.Le_[i]);
    }
    ASSERT_EQ(expectedMaterial.refractiveIndice_, this->scene.lights_[0]->radiance_.refractiveIndice_);
    ASSERT_EQ(expectedMaterial.texture_, this->scene.lights_[0]->radiance_.texture_);

    // Validate the rest was not updated.
    ASSERT_TRUE(this->scene.triangles_.empty());
    ASSERT_TRUE(this->scene.spheres_.empty());
    ASSERT_TRUE(this->scene.planes_.empty());
    ASSERT_TRUE(this->texturesCache.empty());
    ASSERT_TRUE(this->scene.materials_.empty());
}
