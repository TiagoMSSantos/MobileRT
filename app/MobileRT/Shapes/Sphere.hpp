#ifndef MOBILERT_SHAPES_SPHERE_HPP
#define MOBILERT_SHAPES_SPHERE_HPP

#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Intersection.hpp"
#include "MobileRT/Ray.hpp"
#include <glm/glm.hpp>

namespace MobileRT {
    /**
     * A class which represents a sphere in the scene.
     */
    class Sphere final {
    private:
        ::glm::vec3 center_ {};
        float sqRadius_ {};
        ::std::int32_t materialIndex_ {-1};

    private:
        void checkArguments() const;

    public:
        explicit Sphere () = delete;

        explicit Sphere(const ::glm::vec3 &center, float radius, ::std::int32_t materialIndex);

        Sphere(const Sphere &sphere) = default;

        Sphere(Sphere &&sphere) noexcept = default;

        ~Sphere() = default;

        Sphere &operator=(const Sphere &sphere) = default;

        Sphere &operator=(Sphere &&sphere) noexcept = default;

        Intersection intersect(Intersection intersection) const;

        AABB getAABB() const;

        bool intersect(const AABB &box) const;
    };
}//namespace MobileRT

#endif //MOBILERT_SHAPES_SPHERE_HPP
