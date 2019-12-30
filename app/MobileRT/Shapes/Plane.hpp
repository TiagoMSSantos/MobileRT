#ifndef MOBILERT_SHAPES_PLANE_HPP
#define MOBILERT_SHAPES_PLANE_HPP

#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Intersection.hpp"
#include "MobileRT/Ray.hpp"
#include <glm/glm.hpp>

namespace MobileRT {
    class Plane final {
    private:
        ::glm::vec3 normal_ {};
        ::glm::vec3 point_ {};
        ::std::int32_t materialIndex_ {-1};

    private:
        ::glm::vec3 getRightVector() const;

    public:
        explicit Plane () = delete;

        explicit Plane(const ::glm::vec3 &point, const ::glm::vec3 &normal, ::std::int32_t materialIndex);

        Plane(const Plane &plane) = default;

        Plane(Plane &&plane) noexcept = default;

        ~Plane() = default;

        Plane &operator=(const Plane &plane) = default;

        Plane &operator=(Plane &&plane) noexcept = default;

        Intersection intersect(const Intersection &intersection, const Ray &ray) const;

        AABB getAABB() const;

        float distance(const ::glm::vec3 &point) const;

        bool intersect(const AABB &box) const;
    };
}//namespace MobileRT

#endif //MOBILERT_SHAPES_PLANE_HPP
