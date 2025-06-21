#include "Components/Lights/AreaLight.hpp"
#include "Components/Lights/PointLight.hpp"
#include "Components/Samplers/MersenneTwister.hpp"
#include "Components/Samplers/StaticHaltonSeq.hpp"
#include "Scenes/Scenes.hpp"

#include <glm/glm.hpp>

using ::MobileRT::Material;
using ::MobileRT::Scene;
using ::MobileRT::Triangle;
using ::MobileRT::Sphere;
using ::MobileRT::Plane;
using ::MobileRT::Sampler;
using ::Components::AreaLight;
using ::Components::PointLight;
using ::Components::StaticHaltonSeq;

namespace {
    const Material lightMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                             ::glm::vec3 {0.0F, 0.0F, 0.0F},
                             ::glm::vec3 {0.0F, 0.0F, 0.0F},
                             1.0F,
                             ::glm::vec3 {0.9F, 0.9F, 0.9F}};

    const Material mirrorMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                              ::glm::vec3 {0.9F, 0.9F, 0.9F},
                              ::glm::vec3 {0.0F, 0.0F, 0.0F}, 1.0F};

    const Material transmissionMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                                    ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                    ::glm::vec3 {0.9F, 0.9F, 0.9F}, 1.9F};

    const Material lightGrayMat {::glm::vec3 {0.7F, 0.7F, 0.7F}};

    const Material redMat {::glm::vec3 {0.9F, 0.0F, 0.0F}};

    const Material yellowMat {::glm::vec3 {0.9F, 0.9F, 0.0F}};

    const Material greenMat {::glm::vec3 {0.0F, 0.9F, 0.0F}};

    const Material blueMat {::glm::vec3 {0.0F, 0.0F, 0.9F}};

    const Material sandMat {::glm::vec3 {0.914F, 0.723F, 0.531F}};

    const Material lightBlueMat {::glm::vec3 {0.0F, 0.9F, 0.9F}};

    Triangle::Builder triangleBuilder {Triangle::Builder(
        ::glm::vec3 {0.5F, -0.5F, 0.99F},
        ::glm::vec3 {0.5F, 0.5F, 1.001F},
        ::glm::vec3 {-0.5F, -0.5F, 0.99F}
    )};

    const ::glm::vec3 back {0.0F, 0.0F, 1.0F};

    const ::glm::vec3 front {0.0F, 0.0F, -1.0F};

    const ::glm::vec3 bottom {0.0F, -1.0F, 0.0F};

    const ::glm::vec3 top {0.0F, 1.0F, 0.0F};
}//namespace

inline Scene cornellBox(Scene scene) {
    // back wall - white
    scene.planes_.emplace_back(
        back, front,
        static_cast<::std::int32_t> (scene.materials_.size())
    );
    scene.materials_.emplace_back(lightGrayMat);

    // front wall - light blue
    scene.planes_.emplace_back (
        ::glm::vec3 {0.0F, 0.0F, -3.5F}, ::glm::vec3 {0.0F, 0.0F, 1.0F},
        static_cast<::std::int32_t> (scene.materials_.size())
    );
    scene.materials_.emplace_back(lightBlueMat);

    // floor - white
    scene.planes_.emplace_back(
        bottom, top,
        static_cast<::std::int32_t> (scene.materials_.size())
    );
    scene.materials_.emplace_back(lightGrayMat);

    // ceiling - white
    scene.planes_.emplace_back(
        top, bottom,
        static_cast<::std::int32_t> (scene.materials_.size())
    );
    scene.materials_.emplace_back(lightGrayMat);

    // left wall - red
    scene.planes_.emplace_back(
        ::glm::vec3 {-1.0F, 0.0F, 0.0F}, ::glm::vec3 {1.0F, 0.0F, 0.0F},
        static_cast<::std::int32_t> (scene.materials_.size())
    );
    scene.materials_.emplace_back(redMat);

    // right wall - blue
    scene.planes_.emplace_back(::glm::vec3 {1.0F, 0.0F, 0.0F}, ::glm::vec3 {-1.0F, 0.0F, 0.0F}, static_cast<::std::int32_t> (scene.materials_.size()));
    scene.materials_.emplace_back(blueMat);

    return scene;
}

Scene cornellBox_Scene(Scene scene) {
    LOG_DEBUG("SCENE: cornellBox_Scene");
    scene.lights_.emplace_back(::MobileRT::std::make_unique<PointLight> (
        lightMat,
        ::glm::vec3 {0.0F, 0.99F, 0.0F}
    ));

    // triangle - yellow
    const Triangle triangle {
        triangleBuilder.withMaterialIndex(static_cast<::std::int32_t> (scene.materials_.size())).build()
    };

    scene.triangles_.emplace_back(triangle);
    scene.materials_.emplace_back(yellowMat);

    // sphere - mirror
    scene.spheres_.emplace_back(::glm::vec3 {0.45F, -0.65F, 0.4F}, 0.35F, static_cast<::std::int32_t> (scene.materials_.size()));
    scene.materials_.emplace_back(mirrorMat);

    // sphere - green
    scene.spheres_.emplace_back(::glm::vec3 {-0.45F, -0.1F, 0.0F}, 0.35F, static_cast<::std::int32_t> (scene.materials_.size()));
    scene.materials_.emplace_back(greenMat);

    scene = cornellBox(::std::move(scene));

    return scene;
}

::std::unique_ptr<::MobileRT::Camera> cornellBox_Cam(const float ratio) {
    LOG_DEBUG("CAMERA: cornellBox_Cam");
    const float fovX {45.0F * ratio};
    const float fovY {45.0F};
    ::std::unique_ptr<::MobileRT::Camera> res {::MobileRT::std::make_unique<Components::Perspective> (
        ::glm::vec3 {0.0F, 0.0F, -3.4F},
        ::glm::vec3 {0.0F, 0.0F, 1.0F},
        ::glm::vec3 {0.0F, 1.0F, 0.0F},
        fovX, fovY
    )};
    return res;
}

Scene cornellBox2_Scene(Scene scene) {
    LOG_DEBUG("SCENE: cornellBox2_Scene");
    ::std::unique_ptr<Sampler> samplerPoint1 {::MobileRT::std::make_unique<StaticHaltonSeq> ()};
    ::std::unique_ptr<Sampler> samplerPoint2 {::MobileRT::std::make_unique<StaticHaltonSeq> ()};

    Triangle triangle1 {
        Triangle::Builder(
            ::glm::vec3{-0.25F, 0.99F, -0.25F},
            ::glm::vec3{0.25F, 0.99F, -0.25F},
            ::glm::vec3{0.25F, 0.99F, 0.25F}
        )
        .build()
    };

    Triangle triangle2 {
        Triangle::Builder(
            ::glm::vec3{0.25F, 0.99F, 0.25F},
            ::glm::vec3{-0.25F, 0.99F, 0.25F},
            ::glm::vec3{-0.25F, 0.99F, -0.25F}
        )
        .build()
    };

    scene.lights_.emplace_back(::MobileRT::std::make_unique<AreaLight> (
        lightMat,
        ::std::move(samplerPoint1),
        ::std::move(triangle1)
    ));

    scene.lights_.emplace_back(::MobileRT::std::make_unique<AreaLight> (
        lightMat,
        ::std::move(samplerPoint2),
        ::std::move(triangle2)
    ));

    const Triangle triangle3 {
        triangleBuilder.withMaterialIndex(static_cast<::std::int32_t> (scene.materials_.size())).build()
    };

    // triangle - yellow
    scene.triangles_.emplace_back(triangle3);
    scene.materials_.emplace_back(yellowMat);

    // triangle - green

    const Triangle triangle4 {
        Triangle::Builder(
            ::glm::vec3 {-0.5F, 0.5F, 0.99F},
            ::glm::vec3 {-0.5F, -0.5F, 0.99F},
            ::glm::vec3 {0.5F, 0.5F, 0.99F}
        )
        .withMaterialIndex(static_cast<::std::int32_t> (scene.materials_.size()))
        .build()
    };

    scene.triangles_.emplace_back(triangle4);
    scene.materials_.emplace_back(greenMat);

    // sphere - mirror
    scene.spheres_.emplace_back(::glm::vec3 {0.45F, -0.65F, 0.4F}, 0.35F, static_cast<::std::int32_t> (scene.materials_.size()));
    scene.materials_.emplace_back(mirrorMat);

    // sphere - transmission
    scene.spheres_.emplace_back(::glm::vec3 {-0.4F, -0.3F, 0.0F}, 0.35F, static_cast<::std::int32_t> (scene.materials_.size()));
    scene.materials_.emplace_back(transmissionMat);

    scene = cornellBox(::std::move(scene));

    return scene;
}

Scene spheres_Scene(Scene scene) {
    LOG_DEBUG("SCENE: spheres_Scene");
    // create one sphere
    scene.spheres_.emplace_back(
        ::glm::vec3 {4.0F, 4.0F, 4.0F}, 4.0F,
        static_cast<::std::int32_t> (scene.materials_.size())
    );
    scene.materials_.emplace_back(redMat);

    const Triangle triangle {
        Triangle::Builder(
            ::glm::vec3 {0.0F, 10.0F, 10.0F},
            ::glm::vec3 {0.0F, 0.0F, 10.0F},
            ::glm::vec3 {10.0F, 0.0F, 10.0F}
        )
        .withMaterialIndex(static_cast<::std::int32_t> (scene.materials_.size()))
        .build()
    };

    scene.triangles_.emplace_back(triangle);
    scene.materials_.emplace_back(sandMat);
    return scene;
}

::std::unique_ptr<::MobileRT::Camera> spheres_Cam(float ratio) {
    LOG_DEBUG("CAMERA: spheres_Cam");
    const float sizeH {10.0F * ratio};
    const float sizeV {10.0F};
    ::std::unique_ptr<::MobileRT::Camera> res {::MobileRT::std::make_unique<Components::Orthographic> (
        ::glm::vec3 {0.0F, 1.0F, -10.0F},
        ::glm::vec3 {0.0F, 1.0F, 7.0F},
        ::glm::vec3 {0.0F, 1.0F, 0.0F},
        sizeH, sizeV
    )};
    return res;
}

Scene spheres2_Scene(Scene scene) {
    LOG_DEBUG("SCENE: spheres2_Scene");
    scene.lights_.emplace_back(::MobileRT::std::make_unique<PointLight> (lightMat, ::glm::vec3 {0.0F, 15.0F, 4.0F}));

    // create one sphere
    scene.spheres_.emplace_back(::glm::vec3 {-1.0F, 1.0F, 6.0F}, 1.0F, static_cast<::std::int32_t> (scene.materials_.size()));
    scene.materials_.emplace_back(redMat);
    scene.spheres_.emplace_back(::glm::vec3 {-0.5F, 2.0F, 5.0F}, 0.3F, static_cast<::std::int32_t> (scene.materials_.size()));
    scene.materials_.emplace_back(blueMat);
    scene.spheres_.emplace_back(::glm::vec3 {0.0F, 2.0F, 7.0F}, 1.0F, static_cast<::std::int32_t> (scene.materials_.size()));
    scene.materials_.emplace_back(mirrorMat);
    scene.spheres_.emplace_back(::glm::vec3 {0.5F, 0.5F, 5.0F}, 0.2F, static_cast<::std::int32_t> (scene.materials_.size()));
    scene.materials_.emplace_back(yellowMat);
    scene.spheres_.emplace_back(::glm::vec3 {1.0F, 0.5F, 4.5F}, 0.5F, static_cast<::std::int32_t> (scene.materials_.size()));
    scene.materials_.emplace_back(greenMat);
    scene.planes_.emplace_back(::glm::vec3 {0.0F, 0.0F, 0.0F}, top, static_cast<::std::int32_t> (scene.materials_.size()));
    scene.materials_.emplace_back(sandMat);

    return scene;
}

::std::unique_ptr<::MobileRT::Camera> spheres2_Cam(const float ratio) {
    LOG_DEBUG("CAMERA: spheres2_Cam");
    const float fovX {60.0F * ratio};
    const float fovY {60.0F};
    ::std::unique_ptr<::MobileRT::Camera> res {::MobileRT::std::make_unique<Components::Perspective> (
        ::glm::vec3{0.0F, 0.5F, 1.0F},
        ::glm::vec3{0.0F, 0.0F, 7.0F},
        ::glm::vec3{0.0F, 1.0F, 0.0F},
        fovX, fovY
    )};
    return res;
}
