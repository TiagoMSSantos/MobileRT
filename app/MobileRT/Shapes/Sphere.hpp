#ifndef MOBILERT_SHAPES_SPHERE_HPP
#define MOBILERT_SHAPES_SPHERE_HPP

#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Intersection.hpp"
#include "MobileRT/Ray.hpp"
#include <glm/glm.hpp>

namespace MobileRT {
    class Sphere final {
    private:
        ::glm::vec3 center_ {};
        float sq_radius_ {};

    public:
        explicit Sphere () noexcept = delete;

        explicit Sphere(const ::glm::vec3 &center, float radius) noexcept;

        Sphere(const Sphere &sphere) noexcept = default;

        Sphere(Sphere &&sphere) noexcept = default;

        ~Sphere() noexcept = default;

        Sphere &operator=(const Sphere &sphere) noexcept = default;

        Sphere &operator=(Sphere &&sphere) noexcept = default;

        Intersection intersect(const Intersection &intersection, const Ray &ray) const noexcept;

        AABB getAABB() const noexcept;

        bool intersect(const AABB &box) const noexcept;
    };
}//namespace MobileRT

#endif //MOBILERT_SHAPES_SPHERE_HPP
