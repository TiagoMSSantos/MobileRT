#ifndef MOBILERT_SHAPES_TRIANGLE_HPP
#define MOBILERT_SHAPES_TRIANGLE_HPP

#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Intersection.hpp"
#include "MobileRT/Ray.hpp"
#include <glm/glm.hpp>

namespace MobileRT {
    class Triangle final {
    public:
        ::glm::vec3 AC_ {};
        ::glm::vec3 AB_ {};
        ::glm::vec3 pointA_ {};
        ::std::int32_t materialIndex_ {-1};

    public:
        explicit Triangle () noexcept = delete;

        explicit Triangle(
            const ::glm::vec3 &pointA,
            const ::glm::vec3 &pointB,
            const ::glm::vec3 &pointC,
            ::std::int32_t materialIndex
        ) noexcept;

        Triangle(const Triangle &triangle) noexcept = default;

        Triangle(Triangle &&triangle) noexcept = default;

        ~Triangle() noexcept = default;

        Triangle &operator=(const Triangle &triangle) noexcept = default;

        Triangle &operator=(Triangle &&triangle) noexcept = default;

        Intersection intersect(const Intersection &intersection, const Ray &ray) const noexcept;

        AABB getAABB() const noexcept;

        bool intersect(const AABB &box) const noexcept;
    };
}//namespace MobileRT

#endif //MOBILERT_SHAPES_TRIANGLE_HPP
