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
        static float degToRad(float deg);

        static float radToDeg(float rad);

    public:
        /**
         * The position of the camera (in axis X, Y, Z).
         */
        ::glm::vec3 position_ {};

        /**
         * The direction of the camera (in axis X, Y, Z).
         */
        ::glm::vec3 direction_ {};

        /**
         * The right vector of the camera (in axis X, Y, Z).
         */
        ::glm::vec3 right_ {};

        /**
         * The up vector of the camera (in axis X, Y, Z).
         */
        ::glm::vec3 up_ {};

    public:
        explicit Camera(const ::glm::vec3 &position,
                        const ::glm::vec3 &lookAt, const ::glm::vec3 &up);

        Camera(const Camera &camera);

        Camera(Camera &&camera) noexcept = default;

        virtual ~Camera() = default;

        Camera &operator=(const Camera &camera) = default;

        Camera &operator=(Camera &&camera) noexcept = default;

        /**
         * Generates a ray with the origin in the camera.
         *
         * @param u          u = x / width
         * @param v          v = y / height
         * @param deviationU deviationU = [-0.5F / width, 0.5F / width]
         * @param deviationV deviationV = [-0.5F / height, 0.5F / height]
         * @return The new generated ray.
         */
        virtual Ray generateRay(float u, float v,
                                float deviationU,
                                float deviationV) const = 0;

        virtual AABB getAABB() const;
    };
}//namespace MobileRT

#endif //MOBILERT_CAMERA_HPP
