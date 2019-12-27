#ifndef MOBILERT_ACCELERATORS_AABB_HPP
#define MOBILERT_ACCELERATORS_AABB_HPP

#include "MobileRT/Intersection.hpp"
#include "MobileRT/Material.hpp"
#include "MobileRT/Ray.hpp"
#include <glm/glm.hpp>
#include <vector>

namespace MobileRT {
    class AABB final {
    public:
        ::glm::vec3 pointMin_ {};
        ::glm::vec3 pointMax_ {};

    public:
        explicit AABB() noexcept = default;

        explicit AABB(const ::glm::vec3 &pointMin, const ::glm::vec3 &pointMax) noexcept;

        AABB(const AABB &aabb) noexcept = default;

        AABB(AABB &&aabb) noexcept = default;

        ~AABB() noexcept = default;

        AABB &operator=(const AABB &aabb) noexcept = default;

        AABB &operator=(AABB &&aabb) noexcept = default;

        float getSurfaceArea() const noexcept;

        ::glm::vec3 getMidPoint() const noexcept;

        bool intersect(const Ray &ray) const noexcept;
    };

    AABB surroundingBox(const AABB &box1, const AABB &box2) noexcept;
}//namespace MobileRT

#endif //MOBILERT_ACCELERATORS_AABB_HPP
