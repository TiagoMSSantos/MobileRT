#ifndef COMPONENTS_CAMERAS_PERSPECTIVE_HPP
#define COMPONENTS_CAMERAS_PERSPECTIVE_HPP

#include "MobileRT/Camera.hpp"

namespace Components {

    class Perspective final : public ::MobileRT::Camera {
    private:
        float hFov_ {};
        float vFov_ {};

    private:
        static float fastArcTan(float value);

    public:
        explicit Perspective() = delete;

        explicit Perspective(const ::glm::vec3 &position,
                             const ::glm::vec3 &lookAt, const ::glm::vec3 &up,
                             float hFov, float vFov);

        Perspective(const Perspective &perspective) = default;

        Perspective(Perspective &&perspective) noexcept = delete;

        ~Perspective() final = default;

        Perspective &operator=(const Perspective &perspective) = delete;

        Perspective &operator=(Perspective &&perspective) noexcept = delete;

        ::MobileRT::Ray generateRay(float u, float v,
                        float deviationU, float deviationV) const final;

        float getHFov() const;

        float getVFov() const;
    };
}//namespace Components

#endif //COMPONENTS_CAMERAS_PERSPECTIVE_HPP
