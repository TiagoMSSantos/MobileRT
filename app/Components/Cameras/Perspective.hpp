#ifndef COMPONENTS_CAMERAS_PERSPECTIVE_HPP
#define COMPONENTS_CAMERAS_PERSPECTIVE_HPP

#include "MobileRT/Camera.hpp"

namespace Components {

    class Perspective final : public ::MobileRT::Camera {
    private:
        float hFov_{};
        float vFov_{};

    private:
        float fastArcTan(float value) const noexcept;

    public:
        explicit Perspective () noexcept = delete;

        explicit Perspective(const ::glm::vec3 &position,
                             const ::glm::vec3 &lookAt, const ::glm::vec3 &up,
                             float hFov, float vFov) noexcept;

        Perspective(const Perspective &perspective) noexcept = default;

        Perspective(Perspective &&perspective) noexcept = delete;

        ~Perspective() noexcept final = default;

        Perspective &operator=(const Perspective &perspective) noexcept = delete;

        Perspective &operator=(Perspective &&perspective) noexcept = delete;

        ::MobileRT::Ray generateRay(float u, float v,
                        float deviationU, float deviationV) const noexcept final;

        float getHFov() const noexcept;

        float getVFov() const noexcept;
    };
}//namespace Components

#endif //COMPONENTS_CAMERAS_PERSPECTIVE_HPP
