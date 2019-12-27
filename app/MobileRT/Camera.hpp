#ifndef MOBILERT_CAMERA_HPP
#define MOBILERT_CAMERA_HPP

#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Ray.hpp"
#include <algorithm>
#include <atomic>
#include <glm/glm.hpp>
#include <random>

namespace MobileRT {

    class Camera {
    protected:
        float degToRad(float deg) const noexcept;

        float radToDeg(float rad) const noexcept;

    public:
        ::glm::vec3 position_ {};
        ::glm::vec3 direction_ {};
        ::glm::vec3 right_ {};
        ::glm::vec3 up_ {};

    public:
        explicit Camera(const ::glm::vec3 &position,
                        const ::glm::vec3 &lookAt, const ::glm::vec3 &up) noexcept;

        Camera(const Camera &camera) noexcept;

        Camera(Camera &&camera) noexcept = default;

        virtual ~Camera() noexcept = default;

        Camera &operator=(const Camera &camera) noexcept = default;

        Camera &operator=(Camera &&camera) noexcept = default;

        virtual Ray generateRay(float u, float v,
                                float deviationU,
                                float deviationV) const noexcept = 0;

        virtual AABB getAABB() const noexcept;
    };
}//namespace MobileRT

#endif //MOBILERT_CAMERA_HPP
