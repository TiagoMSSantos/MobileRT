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

Scene cornellBoxScene(Scene scene) noexcept {
    // point light - white
    const Material lightMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      1.0F,
                                      ::glm::vec3 {0.9F, 0.9F, 0.9F}};
    scene.lights_.emplace_back(::std::make_unique<PointLight> (
            lightMat,
            ::glm::vec3 {0.0F, 0.99F, 0.0F}
    ));

    // triangle - yellow
    const Material yellowMat {::glm::vec3 {0.9F, 0.9F, 0.0F}};
    scene.triangles_.emplace_back(Triangle {
            ::glm::vec3 {0.5F, -0.5F, 0.99F},
            ::glm::vec3 {0.5F, 0.5F, 1.001F},
            ::glm::vec3 {-0.5F, -0.5F, 0.99F}, static_cast<::std::int32_t> (scene.materials_.size())});
    scene.materials_.emplace_back(yellowMat);

    // sphere - mirror
    const Material mirrorMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                                       ::glm::vec3 {0.9F, 0.9F, 0.9F}};
    scene.spheres_.emplace_back(Sphere {
            ::glm::vec3 {0.45F, -0.65F, 0.4F}, 0.35F, static_cast<::std::int32_t> (scene.materials_.size())});
    scene.materials_.emplace_back(mirrorMat);

    // sphere - green
    const Material greenMat {::glm::vec3 {0.0F, 0.9F, 0.0F},
                                      ::glm::vec3 {0.0F, 0.2F, 0.0F}};
    scene.spheres_.emplace_back(Sphere {
            ::glm::vec3 {-0.45F, -0.1F, 0.0F}, 0.35F, static_cast<::std::int32_t> (scene.materials_.size())});
    scene.materials_.emplace_back(greenMat);

    // back wall - white
    const Material lightGrayMat {::glm::vec3 {0.7F, 0.7F, 0.7F}};
    scene.planes_.emplace_back(Plane {
        ::glm::vec3 {0.0F, 0.0F, 1.0F}, ::glm::vec3 {0.0F, 0.0F, -1.0F},
        static_cast<::std::int32_t> (scene.materials_.size())
    });
    scene.materials_.emplace_back(lightGrayMat);

    // front wall - light blue
    /*const Material &lightBlueMat {::glm::vec3 {0.0F, 0.9F, 0.9F}};
     scene.planes_.emplace_back (Plane {
       ::glm::vec3 {0.0F, 0.0F, -3.5F}, ::glm::vec3 {0.0F, 0.0F, 1.0F}}, lightBlueMat);*/

    // floor - white
    scene.planes_.emplace_back(Plane {
        ::glm::vec3 {0.0F, -1.0F, 0.0F}, ::glm::vec3 {0.0F, 1.0F, 0.0F},
        static_cast<::std::int32_t> (scene.materials_.size())
    });
    scene.materials_.emplace_back(lightGrayMat);

    // ceiling - white
    scene.planes_.emplace_back(Plane {
        ::glm::vec3 {0.0F, 1.0F, 0.0F}, ::glm::vec3 {0.0F, -1.0F, 0.0F},
        static_cast<::std::int32_t> (scene.materials_.size())
    });
    scene.materials_.emplace_back(lightGrayMat);

    // left wall - red
    const Material redMat {::glm::vec3 {0.9F, 0.0F, 0.0F}};
    scene.planes_.emplace_back(Plane {
        ::glm::vec3 {-1.0F, 0.0F, 0.0F}, ::glm::vec3 {1.0F, 0.0F, 0.0F},
        static_cast<::std::int32_t> (scene.materials_.size())
    });
    scene.materials_.emplace_back(redMat);

    // right wall - blue
    const Material blueMat {::glm::vec3 {0.0F, 0.0F, 0.9F}};
    scene.planes_.emplace_back(Plane {
            ::glm::vec3 {1.0F, 0.0F, 0.0F}, ::glm::vec3 {-1.0F, 0.0F, 0.0F},
            static_cast<::std::int32_t> (scene.materials_.size())
    });
    scene.materials_.emplace_back(blueMat);

    return scene;
}

Scene cornellBoxScene2(Scene scene) noexcept {
    const Material lightMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      1.0F,
                                      ::glm::vec3 {0.9F, 0.9F, 0.9F}};
    ::std::unique_ptr<Sampler> samplerPoint1 {::std::make_unique<StaticHaltonSeq> ()};
    ::std::unique_ptr<Sampler> samplerPoint2 {::std::make_unique<StaticHaltonSeq> ()};

    scene.lights_.emplace_back(::std::make_unique<AreaLight> (
        lightMat,
        ::std::move(samplerPoint1),
        ::glm::vec3 {-0.25F, 0.99F, -0.25F},
        ::glm::vec3 {0.25F, 0.99F, -0.25F},
        ::glm::vec3 {0.25F, 0.99F, 0.25F}
    ));

    scene.lights_.emplace_back(::std::make_unique<AreaLight> (
        lightMat,
        ::std::move(samplerPoint2),
        ::glm::vec3 {0.25F, 0.99F, 0.25F},
        ::glm::vec3 {-0.25F, 0.99F, 0.25F},
        ::glm::vec3 {-0.25F, 0.99F, -0.25F}
    ));

    // triangle - yellow
    const Material yellowMat {::glm::vec3 {0.9F, 0.9F, 0.0F}};
    scene.triangles_.emplace_back(Triangle {
            ::glm::vec3 {0.5F, -0.5F, 0.99F},
            ::glm::vec3 {0.5F, 0.5F, 1.001F},
            ::glm::vec3 {-0.5F, -0.5F, 0.99F}, static_cast<::std::int32_t> (scene.materials_.size())});
    scene.materials_.emplace_back(yellowMat);

    // triangle - green
    const Material greenMat {::glm::vec3 {0.0F, 0.9F, 0.0F}};
    scene.triangles_.emplace_back(Triangle {
            ::glm::vec3 {-0.5F, 0.5F, 0.99F},
            ::glm::vec3 {-0.5F, -0.5F, 0.99F},
            ::glm::vec3 {0.5F, 0.5F, 0.99F}, static_cast<::std::int32_t> (scene.materials_.size())});
    scene.materials_.emplace_back(greenMat);

    // sphere - mirror
    const Material mirrorMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                                       ::glm::vec3 {0.9F, 0.9F, 0.9F}};
    scene.spheres_.emplace_back(Sphere {
            ::glm::vec3 {0.45F, -0.65F, 0.4F}, 0.35F,
            static_cast<::std::int32_t> (scene.materials_.size())});
    scene.materials_.emplace_back(mirrorMat);

    // sphere - transmission
    const Material transmissionMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                                             ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                             ::glm::vec3 {0.9F, 0.9F, 0.9F}, 1.9F};
    scene.spheres_.emplace_back(Sphere {
            ::glm::vec3 {-0.4F, -0.3F, 0.0F}, 0.35F,
            static_cast<::std::int32_t> (scene.materials_.size())});
    scene.materials_.emplace_back(transmissionMat);

    // back wall - white
    const Material lightGrayMat {::glm::vec3 {0.7F, 0.7F, 0.7F}};
    scene.planes_.emplace_back(Plane {
        ::glm::vec3 {0.0F, 0.0F, 1.0F}, ::glm::vec3 {0.0F, 0.0F, -1.0F},
        static_cast<::std::int32_t> (scene.materials_.size())
    });
    scene.materials_.emplace_back(lightGrayMat);

    // front wall - light blue
    const Material lightBlueMat {::glm::vec3 {0.0F, 0.9F, 0.9F}};
    scene.planes_.emplace_back(Plane {
        ::glm::vec3 {0.0F, 0.0F, -4.0F}, ::glm::vec3 {0.0F, 0.0F, 1.0F},
        static_cast<::std::int32_t> (scene.materials_.size())
    });
    scene.materials_.emplace_back(lightBlueMat);

    // floor - white
    scene.planes_.emplace_back(Plane {
        ::glm::vec3 {0.0F, -1.0F, 0.0F}, ::glm::vec3 {0.0F, 1.0F, 0.0F},
        static_cast<::std::int32_t> (scene.materials_.size())
    });
    scene.materials_.emplace_back(lightGrayMat);
    // ceiling - white
    scene.planes_.emplace_back(Plane {
        ::glm::vec3 {0.0F, 1.0F, 0.0F}, ::glm::vec3 {0.0F, -1.0F, 0.0F},
        static_cast<::std::int32_t> (scene.materials_.size())
    });
    scene.materials_.emplace_back(lightGrayMat);
    // left wall - red
    const Material redMat {::glm::vec3 {0.9F, 0.0F, 0.0F}};
    scene.planes_.emplace_back(Plane {
        ::glm::vec3 {-1.0F, 0.0F, 0.0F}, ::glm::vec3 {1.0F, 0.0F, 0.0F},
        static_cast<::std::int32_t> (scene.materials_.size())
    });
    scene.materials_.emplace_back(redMat);

    // right wall - blue
    const Material blueMat {::glm::vec3 {0.0F, 0.0F, 0.9F}};
    scene.planes_.emplace_back(Plane {
        ::glm::vec3 {1.0F, 0.0F, 0.0F}, ::glm::vec3 {-1.0F, 0.0F, 0.0F},
        static_cast<::std::int32_t> (scene.materials_.size())
    });
    scene.materials_.emplace_back(blueMat);

    return scene;
}

Scene spheresScene(Scene scene) noexcept {
    // create diffuse Materials
    const Material sandMat {::glm::vec3 {0.914F, 0.723F, 0.531F}};
    const Material redMat {::glm::vec3 {0.9F, 0.0F, 0.0F}};

    // create one sphere
    scene.spheres_.emplace_back(Sphere {
        ::glm::vec3 {4.0F, 4.0F, 4.0F}, 4.0F,
        static_cast<::std::int32_t> (scene.materials_.size())
    });
    scene.materials_.emplace_back(redMat);
    scene.triangles_.emplace_back (Triangle {
            ::glm::vec3 {0.0F, 10.0F, 10.0F},
            ::glm::vec3 {0.0F, 0.0F, 10.0F},
            ::glm::vec3 {10.0F, 0.0F, 10.0F},
            static_cast<::std::int32_t> (scene.materials_.size())});
    scene.materials_.emplace_back(sandMat);
    return scene;
}

Scene spheresScene2(Scene scene) noexcept {
    // create one light source
    const Material lightMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      1.0F,
                                      ::glm::vec3 {0.9F, 0.9F, 0.9F}};

    scene.lights_.emplace_back(::std::make_unique<PointLight> (lightMat, ::glm::vec3 {0.0F, 15.0F, 4.0F}));

    // create diffuse Materials
    const Material sandMat {::glm::vec3 {0.914F, 0.723F, 0.531F}};
    const Material redMat {::glm::vec3 {0.9F, 0.0F, 0.0F}};
    const Material blueMat {::glm::vec3 {0.0F, 0.0F, 0.9F}};
    const Material yellowMat {::glm::vec3 {0.9F, 0.9F, 0.0F}, ::glm::vec3 {0.8F, 0.8F, 0.4F}};
    const Material mirrorMat {::glm::vec3 {0.2F, 0.2F, 0.2F}, ::glm::vec3 {0.9F, 0.9F, 0.9F}};
    const Material greenMat {::glm::vec3 {0.0F, 0.9F, 0.0F}};

    // create one sphere
    scene.spheres_.emplace_back(Sphere {::glm::vec3 {-1.0F, 1.0F, 6.0F}, 1.0F,
                                                  static_cast<::std::int32_t> (scene.materials_.size())});
    scene.materials_.emplace_back(redMat);
    scene.spheres_.emplace_back(Sphere {::glm::vec3 {-0.5F, 2.0F, 5.0F}, 0.3F,
                                                  static_cast<::std::int32_t> (scene.materials_.size())});
    scene.materials_.emplace_back(blueMat);
    scene.spheres_.emplace_back(Sphere {::glm::vec3 {0.0F, 2.0F, 7.0F}, 1.0F,
                                                  static_cast<::std::int32_t> (scene.materials_.size())});
    scene.materials_.emplace_back(mirrorMat);
    scene.spheres_.emplace_back(Sphere {::glm::vec3 {0.5F, 0.5F, 5.0F}, 0.2F,
                                                  static_cast<::std::int32_t> (scene.materials_.size())});
    scene.materials_.emplace_back(yellowMat);
    scene.spheres_.emplace_back(Sphere {::glm::vec3 {1.0F, 0.5F, 4.5F}, 0.5F,
                                                  static_cast<::std::int32_t> (scene.materials_.size())});
    scene.materials_.emplace_back(greenMat);
    scene.planes_.emplace_back(Plane {::glm::vec3 {0.0F, 0.0F, 0.0F}, ::glm::vec3 {0.0F, 1.0F, 0.0F},
                                                static_cast<::std::int32_t> (scene.materials_.size())});
    scene.materials_.emplace_back(sandMat);

    return scene;
}
