#ifndef COMPONENTS_CAMERAS_ORTHOGRAPHIC_HPP
#define COMPONENTS_CAMERAS_ORTHOGRAPHIC_HPP

#include "MobileRT/Camera.hpp"

namespace Components {

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
