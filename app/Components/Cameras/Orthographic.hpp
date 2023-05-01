#ifndef COMPONENTS_CAMERAS_ORTHOGRAPHIC_HPP
#define COMPONENTS_CAMERAS_ORTHOGRAPHIC_HPP

#include "MobileRT/Camera.hpp"

namespace Components {

    /**
     * A class which represents an Orthographic camera in the scene.
     *
     * This type of camera is similar to the isometric view of some games.
     * Different from normal perspective cameras, an object's apparent scale isn't affected by its
     * distance from the camera.
     * So, with this camera, an object's size in the rendered image stays constant regardless of
     * its distance from the camera.
     */
    class Orthographic final : public ::MobileRT::Camera {
    private:
        float sizeH_ {};
        float sizeV_ {};

    public:
        explicit Orthographic() = delete;

        explicit Orthographic(const ::glm::vec3 &position,
                              const ::glm::vec3 &lookAt, const ::glm::vec3 &up,
                              float sizeH, float sizeV);

        Orthographic(const Orthographic &orthographic) = default;

        Orthographic(Orthographic &&orthographic) noexcept = delete;

        ~Orthographic() final = default;

        Orthographic &operator=(const Orthographic &orthographic) = delete;

        Orthographic &operator=(Orthographic &&orthographic) noexcept = delete;

        ::MobileRT::Ray generateRay(float u, float v,
                        float deviationU,
                        float deviationV) const final;

        ::MobileRT::AABB getAABB() const final;

        float getSizeH() const;

        float getSizeV() const;
    };
}//namespace Components

#endif //COMPONENTS_CAMERAS_ORTHOGRAPHIC_HPP
