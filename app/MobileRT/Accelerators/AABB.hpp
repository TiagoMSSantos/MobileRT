#ifndef MOBILERT_ACCELERATORS_AABB_HPP
#define MOBILERT_ACCELERATORS_AABB_HPP

#include "MobileRT/Intersection.hpp"
#include "MobileRT/Material.hpp"
#include "MobileRT/Ray.hpp"
#include <glm/glm.hpp>
#include <vector>

namespace MobileRT {
    /**
     * A class which represents an Axis Aligned Bounding Box.
     * <br>
     * This type of bounding box consists of a box where all the edges are aligned with the axis of the scene.
     */
    class AABB final {
    private:
        ::glm::vec3 pointMin_ {};
        ::glm::vec3 pointMax_ {};

    private:
        void checkArguments() const;

    public:
        explicit AABB() = default;

        explicit AABB(const ::glm::vec3 &pointMin, const ::glm::vec3 &pointMax);

        AABB(const AABB &aabb) = default;

        AABB(AABB &&aabb) noexcept = default;

        ~AABB() = default;

        AABB &operator=(const AABB &aabb) = default;

        AABB &operator=(AABB &&aabb) noexcept = default;

        float getSurfaceArea() const;

        ::glm::vec3 getCentroid() const;

        bool intersect(const Ray &ray) const;

        ::glm::vec3 getPointMin() const;

        ::glm::vec3 getPointMax() const;
    };

    AABB surroundingBox(const AABB &box1, const AABB &box2);
}//namespace MobileRT

#endif //MOBILERT_ACCELERATORS_AABB_HPP
