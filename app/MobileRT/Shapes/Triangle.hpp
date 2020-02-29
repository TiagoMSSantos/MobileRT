#ifndef MOBILERT_SHAPES_TRIANGLE_HPP
#define MOBILERT_SHAPES_TRIANGLE_HPP

#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Intersection.hpp"
#include "MobileRT/Ray.hpp"
#include <glm/glm.hpp>

namespace MobileRT {
    /**
     * A class which represents a triangle in the scene.
     */
    class Triangle final {
    public:
        ::glm::vec3 AC_ {};
        ::glm::vec3 AB_ {};
        ::glm::vec3 pointA_ {};
        ::glm::vec3 normalA_ {};
        ::glm::vec3 normalB_ {};
        ::glm::vec3 normalC_ {};
        ::glm::vec2 texCoordA_ {-1};
        ::glm::vec2 texCoordB_ {-1};
        ::glm::vec2 texCoordC_ {-1};
        ::std::int32_t materialIndex_ {-1};

    public:
        explicit Triangle () = delete;

        explicit Triangle(
            const ::glm::vec3 &pointA,
            const ::glm::vec3 &pointB,
            const ::glm::vec3 &pointC,
            ::std::int32_t materialIndex
        );

        explicit Triangle(
            const ::glm::vec3 &pointA,
            const ::glm::vec3 &pointB,
            const ::glm::vec3 &pointC,
            const ::glm::vec3 &normalA,
            const ::glm::vec3 &normalB,
            const ::glm::vec3 &normalC,
            ::std::int32_t materialIndex
        );

        explicit Triangle(
            const ::glm::vec3 &pointA,
            const ::glm::vec3 &pointB,
            const ::glm::vec3 &pointC,
            const ::glm::vec2 &texCoordA,
            const ::glm::vec2 &texCoordB,
            const ::glm::vec2 &texCoordC,
            ::std::int32_t materialIndex
        );

        explicit Triangle(
            const ::glm::vec3 &pointA,
            const ::glm::vec3 &pointB,
            const ::glm::vec3 &pointC,
            const ::glm::vec3 &normalA,
            const ::glm::vec3 &normalB,
            const ::glm::vec3 &normalC,
            const ::glm::vec2 &texCoordA,
            const ::glm::vec2 &texCoordB,
            const ::glm::vec2 &texCoordC,
            ::std::int32_t materialIndex
        );

        Triangle(const Triangle &triangle) = default;

        Triangle(Triangle &&triangle) noexcept = default;

        ~Triangle() = default;

        Triangle &operator=(const Triangle &triangle) = default;

        Triangle &operator=(Triangle &&triangle) noexcept = default;

        Intersection intersect(const Intersection &intersection, const Ray &ray) const;

        AABB getAABB() const;

        bool intersect(const AABB &box) const;
    };
}//namespace MobileRT

#endif //MOBILERT_SHAPES_TRIANGLE_HPP
