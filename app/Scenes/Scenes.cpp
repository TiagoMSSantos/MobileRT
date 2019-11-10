#include "Components/Lights/AreaLight.hpp"
#include "Components/Lights/PointLight.hpp"
#include "Components/Samplers/MersenneTwister.hpp"
#include "Components/Samplers/StaticHaltonSeq.hpp"
#include "Scenes/Scenes.hpp"
#include <glm/glm.hpp>

MobileRT::Scene cornellBoxScene(MobileRT::Scene scene) noexcept {
    // point light - white
    const ::MobileRT::Material lightMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      1.0F,
                                      ::glm::vec3 {0.9F, 0.9F, 0.9F}};
    scene.lights_.emplace_back(::std::make_unique<Components::PointLight>(lightMat,
                                                                        ::glm::vec3 {0.0F,
                                                                                           0.99F,
                                                                                           0.0F}));

    // triangle - yellow
    const ::MobileRT::Material yellowMat {::glm::vec3 {0.9F, 0.9F, 0.0F}};
    scene.triangles_.emplace_back(MobileRT::Triangle {
            ::glm::vec3 {0.5F, -0.5F, 0.99F},
            ::glm::vec3 {0.5F, 0.5F, 1.001F},
            ::glm::vec3 {-0.5F, -0.5F, 0.99F}}, yellowMat);

    // sphere - mirror
    const ::MobileRT::Material MirrorMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                                       ::glm::vec3 {0.9F, 0.9F, 0.9F}};
    scene.spheres_.emplace_back(MobileRT::Sphere {
            ::glm::vec3 {0.45F, -0.65F, 0.4F}, 0.35F}, MirrorMat);

    // sphere - green
    const ::MobileRT::Material GreenMat {::glm::vec3 {0.0F, 0.9F, 0.0F},
                                      ::glm::vec3 {0.0F, 0.2F, 0.0F}};
    scene.spheres_.emplace_back(MobileRT::Sphere {
            ::glm::vec3 {-0.45F, -0.1F, 0.0F}, 0.35F}, GreenMat);

    // back wall - white
    const ::MobileRT::Material lightGrayMat {::glm::vec3 {0.7F, 0.7F, 0.7F}};
    scene.planes_.emplace_back(MobileRT::Plane {
                                       ::glm::vec3 {0.0F, 0.0F, 1.0F}, ::glm::vec3 {0.0F, 0.0F, -1.0F}},
                               lightGrayMat);

    // front wall - light blue
    /*const ::MobileRT::Material &lightBlueMat {::glm::vec3 {0.0F, 0.9F, 0.9F}};
     scene.planes_.emplace_back (MobileRT::Plane {
       ::glm::vec3 {0.0F, 0.0F, -3.5F}, ::glm::vec3 {0.0F, 0.0F, 1.0F}}, lightBlueMat);*/

    // floor - white
    scene.planes_.emplace_back(MobileRT::Plane {
                                       ::glm::vec3 {0.0F, -1.0F, 0.0F}, ::glm::vec3 {0.0F, 1.0F, 0.0F}},
                               lightGrayMat);

    // ceiling - white
    scene.planes_.emplace_back(MobileRT::Plane {
                                       ::glm::vec3 {0.0F, 1.0F, 0.0F}, ::glm::vec3 {0.0F, -1.0F, 0.0F}},
                               lightGrayMat);

    // left wall - red
    const ::MobileRT::Material redMat {::glm::vec3 {0.9F, 0.0F, 0.0F}};
    scene.planes_.emplace_back(MobileRT::Plane {
            ::glm::vec3 {-1.0F, 0.0F, 0.0F}, ::glm::vec3 {1.0F, 0.0F, 0.0F}}, redMat);

    // right wall - blue
    const ::MobileRT::Material blueMat {::glm::vec3 {0.0F, 0.0F, 0.9F}};
    scene.planes_.emplace_back(MobileRT::Plane {
            ::glm::vec3 {1.0F, 0.0F, 0.0F}, ::glm::vec3 {-1.0F, 0.0F, 0.0F}}, blueMat);

    return scene;
}

MobileRT::Scene cornellBoxScene2(MobileRT::Scene scene) noexcept {
    const ::MobileRT::Material lightMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      1.0F,
                                      ::glm::vec3 {0.9F, 0.9F, 0.9F}};
    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
            ::std::make_unique<Components::StaticHaltonSeq>()};
    //::std::unique_ptr<MobileRT::Sampler> samplerPoint1 {::std::make_unique<Components::MersenneTwister> ()};
    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
            ::std::make_unique<Components::StaticHaltonSeq>()};
    //::std::unique_ptr<MobileRT::Sampler> samplerPoint2 {::std::make_unique<Components::MersenneTwister> ()};

    scene.lights_.emplace_back(::std::make_unique<Components::AreaLight>(lightMat,
            /*scene.lights_.emplace_back(
::std::make_unique<Components::PointLight> (lightMat,
   ::glm::vec3 {0.70F, 0.99F, 0.0F}));*/
                                                                         ::std::move(samplerPoint1),
                                                                         ::glm::vec3 {-0.25F,
                                                                                            0.99F,
                                                                                            -0.25F},
                                                                         ::glm::vec3 {0.25F,
                                                                                            0.99F,
                                                                                            -0.25F},
                                                                         ::glm::vec3 {0.25F,
                                                                                            0.99F,
                                                                                            0.25F}));

    scene.lights_.emplace_back(::std::make_unique<Components::AreaLight>(lightMat,
                                                                         ::std::move(samplerPoint2),
                                                                         ::glm::vec3 {0.25F,
                                                                                            0.99F,
                                                                                            0.25F},
                                                                         ::glm::vec3 {-0.25F,
                                                                                            0.99F,
                                                                                            0.25F},
                                                                         ::glm::vec3 {-0.25F,
                                                                                            0.99F,
                                                                                            -0.25F}));

    // triangle - yellow
    const ::MobileRT::Material yellowMat {::glm::vec3 {0.9F, 0.9F, 0.0F}};
    scene.triangles_.emplace_back(MobileRT::Triangle {
            ::glm::vec3 {0.5F, -0.5F, 0.99F},
            ::glm::vec3 {0.5F, 0.5F, 1.001F},
            ::glm::vec3 {-0.5F, -0.5F, 0.99F}}, yellowMat);

    // triangle - green
    const ::MobileRT::Material greenMat {::glm::vec3 {0.0F, 0.9F, 0.0F}};
    scene.triangles_.emplace_back(MobileRT::Triangle {
            ::glm::vec3 {-0.5F, 0.5F, 0.99F},
            ::glm::vec3 {-0.5F, -0.5F, 0.99F},
            ::glm::vec3 {0.5F, 0.5F, 0.99F}}, greenMat);

    // sphere - mirror
    const ::MobileRT::Material MirrorMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                                       ::glm::vec3 {0.9F, 0.9F, 0.9F}};
    scene.spheres_.emplace_back(MobileRT::Sphere {
            ::glm::vec3 {0.45F, -0.65F, 0.4F}, 0.35F}, MirrorMat);

    // sphere - transmission
    const ::MobileRT::Material TransmissionMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                                             ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                             ::glm::vec3 {0.9F, 0.9F, 0.9F}, 1.9F};
    scene.spheres_.emplace_back(MobileRT::Sphere {
            ::glm::vec3 {-0.4F, -0.3F, 0.0F}, 0.35F}, TransmissionMat);

    // back wall - white
    const ::MobileRT::Material lightGrayMat {::glm::vec3 {0.7F, 0.7F, 0.7F}};
    scene.planes_.emplace_back(MobileRT::Plane {
                                       ::glm::vec3 {0.0F, 0.0F, 1.0F}, ::glm::vec3 {0.0F, 0.0F, -1.0F}},
                               lightGrayMat);

    // front wall - light blue
    const ::MobileRT::Material lightBlueMat {::glm::vec3 {0.0F, 0.9F, 0.9F}};
    scene.planes_.emplace_back(MobileRT::Plane {
                                       ::glm::vec3 {0.0F, 0.0F, -4.0F}, ::glm::vec3 {0.0F, 0.0F, 1.0F}},
                               lightBlueMat);

    // floor - white
    scene.planes_.emplace_back(MobileRT::Plane {
                                       ::glm::vec3 {0.0F, -1.0F, 0.0F}, ::glm::vec3 {0.0F, 1.0F, 0.0F}},
                               lightGrayMat);
    // ceiling - white
    scene.planes_.emplace_back(MobileRT::Plane {
                                       ::glm::vec3 {0.0F, 1.0F, 0.0F}, ::glm::vec3 {0.0F, -1.0F, 0.0F}},
                               lightGrayMat);
    // left wall - red
    const ::MobileRT::Material redMat {::glm::vec3 {0.9F, 0.0F, 0.0F}};
    scene.planes_.emplace_back(MobileRT::Plane {
            ::glm::vec3 {-1.0F, 0.0F, 0.0F}, ::glm::vec3 {1.0F, 0.0F, 0.0F}}, redMat);

    // right wall - blue
    const ::MobileRT::Material blueMat {::glm::vec3 {0.0F, 0.0F, 0.9F}};
    scene.planes_.emplace_back(MobileRT::Plane {
            ::glm::vec3 {1.0F, 0.0F, 0.0F}, ::glm::vec3 {-1.0F, 0.0F, 0.0F}}, blueMat);

    return scene;
}

MobileRT::Scene spheresScene(MobileRT::Scene scene) noexcept {
    // create one light source
    /*const ::MobileRT::Material &lightMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                                       ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                       ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                       1.0F,
                                       ::glm::vec3 {0.9F, 0.9F, 0.9F}};
    scene.lights_.emplace_back (::std::make_unique<Components::PointLight> (lightMat,
                                                                ::glm::vec3 {0.0F, 15.0F,
                                                                                   4.0F}));*/

    // create diffuse Materials
    const ::MobileRT::Material sandMat{::glm::vec3 {0.914F, 0.723F, 0.531F}};
    const ::MobileRT::Material redMat {::glm::vec3 {0.9F, 0.0F, 0.0F}};
    /*const ::MobileRT::Material &mirrorMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                                        ::glm::vec3 {0.9F, 0.9F, 0.9F}};*/
    //const ::MobileRT::Material &greenMat {::glm::vec3 {0.0F, 0.9F, 0.0F}};
    // create one sphere
    scene.spheres_.emplace_back(MobileRT::Sphere {
            ::glm::vec3 {4.0F, 4.0F, 4.0F}, 4.0F}, redMat);
    scene.triangles_.emplace_back (MobileRT::Triangle {
            ::glm::vec3 {0.0F, 10.0F, 10.0F},
            ::glm::vec3 {0.0F, 0.0F, 10.0F},
            ::glm::vec3 {10.0F, 0.0F, 10.0F}}, sandMat);
    /*scene.triangles_.emplace_back (MobileRT::Triangle {
      ::glm::vec3 {10.0F, 10.0F, 10.0F},
      ::glm::vec3 {10.0F, 0.0F, 10.0F},
      ::glm::vec3 {0.0F, 10.0F, 10.0F}}, greenMat);*/
    /*scene.spheres_.emplace_back (MobileRT::Sphere {
      ::glm::vec3 {-1.0F, 1.0F, 6.0F}, 1.0F}, redMat);*/
    /*scene.spheres_.emplace_back (MobileRT::Sphere {
      ::glm::vec3 {1.5F, 2.0F, 7.0F}, 1.0F}, mirrorMat);
    scene.spheres_.emplace_back (MobileRT::Sphere {
     ::glm::vec3 {0.0F, 0.5F, 4.5F}, 0.5F}, greenMat);

    scene.planes_.emplace_back (MobileRT::Plane {
     ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                    ::glm::vec3 {0.0F, 1.0F, 0.0F}}, sandMat);*/
    return scene;
}

MobileRT::Scene spheresScene2(MobileRT::Scene scene) noexcept {
    // create one light source
    const ::MobileRT::Material lightMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                      1.0F,
                                      ::glm::vec3 {0.9F, 0.9F, 0.9F}};
    scene.lights_.emplace_back(::std::make_unique<Components::PointLight>(lightMat,
                                                                          ::glm::vec3 {0.0F,
                                                                                            15.0F,
                                                                                            4.0F}));

    // create diffuse Materials
    const ::MobileRT::Material sandMat {::glm::vec3 {0.914F, 0.723F, 0.531F}};
    const ::MobileRT::Material redMat {::glm::vec3 {0.9F, 0.0F, 0.0F}};
    const ::MobileRT::Material blueMat {::glm::vec3 {0.0F, 0.0F, 0.9F}};
    const ::MobileRT::Material yellowMat {::glm::vec3 {0.9F, 0.9F, 0.0F},
                                       ::glm::vec3 {0.8F, 0.8F, 0.4F}};
    const ::MobileRT::Material mirrorMat {::glm::vec3 {0.2F, 0.2F, 0.2F},
                                       ::glm::vec3 {0.9F, 0.9F, 0.9F}};
    const ::MobileRT::Material greenMat {::glm::vec3 {0.0F, 0.9F, 0.0F}};
    // create one sphere
    scene.spheres_.emplace_back(MobileRT::Sphere {
            ::glm::vec3 {-1.0F, 1.0F, 6.0F}, 1.0F}, redMat);
    scene.spheres_.emplace_back(MobileRT::Sphere {
            ::glm::vec3 {-0.5F, 2.0F, 5.0F}, 0.3F}, blueMat);
    scene.spheres_.emplace_back(MobileRT::Sphere {
            ::glm::vec3 {0.0F, 2.0F, 7.0F}, 1.0F}, mirrorMat);
    scene.spheres_.emplace_back(MobileRT::Sphere {
            ::glm::vec3 {0.5F, 0.5F, 5.0F}, 0.2F}, yellowMat);
    scene.spheres_.emplace_back(MobileRT::Sphere {
            ::glm::vec3 {1.0F, 0.5F, 4.5F}, 0.5F}, greenMat);
    scene.planes_.emplace_back(MobileRT::Plane {
            ::glm::vec3 {0.0F, 0.0F, 0.0F}, ::glm::vec3 {0.0F, 1.0F, 0.0F}}, sandMat);
    return scene;
}
