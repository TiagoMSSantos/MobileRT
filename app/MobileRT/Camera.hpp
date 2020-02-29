#ifndef MOBILERT_CAMERA_HPP
#define MOBILERT_CAMERA_HPP

#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Ray.hpp"
#include <algorithm>
#include <atomic>
#include <glm/glm.hpp>
#include <random>

namespace MobileRT {

    /**
     * A class which represents a camera in the scene.
     */
    class Camera {
    protected:
        float degToRad(float deg) const;

        float radToDeg(float rad) const;

    public:
        ::glm::vec3 position_ {};
        ::glm::vec3 direction_ {};
        ::glm::vec3 right_ {};
        ::glm::vec3 up_ {};

    public:
        explicit Camera(const ::glm::vec3 &position,
                        const ::glm::vec3 &lookAt, const ::glm::vec3 &up);

        Camera(const Camera &camera);

        Camera(Camera &&camera) noexcept = default;

        virtual ~Camera();

        Camera &operator=(const Camera &camera) = default;

        Camera &operator=(Camera &&camera) noexcept = default;

        virtual Ray generateRay(float u, float v,
                                float deviationU,
                                float deviationV) const = 0;

        virtual AABB getAABB() const;
    };
}//namespace MobileRT

#endif //MOBILERT_CAMERA_HPP
