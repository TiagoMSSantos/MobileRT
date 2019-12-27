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
        ::glm::vec3 getRightVector() const noexcept;

    public:
        explicit Plane () noexcept = delete;

        explicit Plane(const ::glm::vec3 &point, const ::glm::vec3 &normal, ::std::int32_t materialIndex) noexcept;

        Plane(const Plane &plane) noexcept = default;

        Plane(Plane &&plane) noexcept = default;

        ~Plane() noexcept = default;

        Plane &operator=(const Plane &plane) noexcept = default;

        Plane &operator=(Plane &&plane) noexcept = default;

        Intersection intersect(const Intersection &intersection, const Ray &ray) const noexcept;

        AABB getAABB() const noexcept;

        float distance(const ::glm::vec3 &point) const noexcept;

        bool intersect(const AABB &box) const noexcept;
    };
}//namespace MobileRT

#endif //MOBILERT_SHAPES_PLANE_HPP
