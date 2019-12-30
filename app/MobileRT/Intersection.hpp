#ifndef MOBILERT_INTERSECTION_HPP
#define MOBILERT_INTERSECTION_HPP

#include "MobileRT/Material.hpp"
#include "MobileRT/Utils.hpp"
#include <glm/glm.hpp>

namespace MobileRT {
    class Intersection final {
    public:
        ::glm::vec3 point_ {};
        ::glm::vec3 normal_ {};
        Material *material_ {};
        float length_ {RayLengthMax};
        const void *primitive_ {};
        ::std::int32_t materialIndex_ {};
        ::glm::vec2 texCoords_ {-1};

    public:
        explicit Intersection() = delete;

        explicit Intersection(float dist, const void *primitive);

        explicit Intersection(
            const ::glm::vec3 &intPoint,
            float dist,
            const ::glm::vec3 &normal,
            const void *primitive,
            ::std::int32_t materialIndex,
            ::glm::vec2 texCoords = ::glm::vec2 {-1});

        Intersection(const Intersection &intersection) = default;

        Intersection(Intersection &&intersection) noexcept = default;

        ~Intersection() = default;

        Intersection &operator=(const Intersection &intersection) = delete;

        Intersection &operator=(Intersection &&intersection) noexcept = default;
    };
}//namespace MobileRT

#endif //MOBILERT_INTERSECTION_HPP
