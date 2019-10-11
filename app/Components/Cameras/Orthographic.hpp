#ifndef COMPONENTS_CAMERAS_ORTHOGRAPHIC_HPP
#define COMPONENTS_CAMERAS_ORTHOGRAPHIC_HPP

#include "MobileRT/Camera.hpp"

namespace Components {

    class Orthographic final : public ::MobileRT::Camera {
    private:
        float sizeH_ {};
        float sizeV_ {};

    public:
        explicit Orthographic () noexcept = delete;

        explicit Orthographic(const ::glm::vec3 &position,
                              const ::glm::vec3 &lookAt, const ::glm::vec3 &up,
                              float sizeH, float sizeV) noexcept;

        Orthographic(const Orthographic &orthographic) noexcept = default;

        Orthographic(Orthographic &&orthographic) noexcept = delete;

        ~Orthographic() noexcept final = default;

        Orthographic &operator=(const Orthographic &orthographic) noexcept = delete;

        Orthographic &operator=(Orthographic &&orthographic) noexcept = delete;

        ::MobileRT::Ray generateRay(float u, float v,
                        float deviationU,
                        float deviationV) const noexcept final;

        ::MobileRT::AABB getAABB() const noexcept final;

        float getSizeH() const noexcept;

        float getSizeV() const noexcept;
    };
}//namespace Components

#endif //COMPONENTS_CAMERAS_ORTHOGRAPHIC_HPP
