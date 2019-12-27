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
        const Material *material_ {};
        float length_ {RayLengthMax};
        const void *primitive_ {};
        ::std::int32_t materialIndex_ {};

    public:
        explicit Intersection () noexcept = delete;

        explicit Intersection(float dist, const void *primitive) noexcept;

        explicit Intersection(
                const ::glm::vec3 &intPoint,
                float dist,
                const ::glm::vec3 &normal,
                const void *primitive,
                ::std::int32_t materialIndex) noexcept;

        Intersection(const Intersection &intersection) noexcept = default;

        Intersection(Intersection &&intersection) noexcept = default;

        ~Intersection() noexcept = default;

        Intersection &operator=(const Intersection &intersection) noexcept = delete;

        Intersection &operator=(Intersection &&intersection) noexcept = default;
    };
}//namespace MobileRT

#endif //MOBILERT_INTERSECTION_HPP
