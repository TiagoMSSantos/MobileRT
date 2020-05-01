#ifndef MOBILERT_INTERSECTION_HPP
#define MOBILERT_INTERSECTION_HPP

#include "MobileRT/Material.hpp"
#include "MobileRT/Utils.hpp"
#include <glm/glm.hpp>

namespace MobileRT {
    /**
     * A class which represents an intersection of a ray with a primitive.
     */
    class Intersection final {
    public:
        ::glm::vec3 point_ {0.0F, 0.0F, 0.0F};
        ::glm::vec3 normal_ {0.0F, 1.0F, 0.0F};
        Material *material_ {nullptr};
        float length_ {RayLengthMax};
        const void *primitive_ {nullptr};
        ::std::int32_t materialIndex_ {-1};
        ::glm::vec2 texCoords_ {-1.0F, -1.0F};

    private:
        void checkArguments() const;

    public:
        explicit Intersection() = default;

        explicit Intersection(float dist);

        explicit Intersection(
            const ::glm::vec3 &intPoint,
            float dist,
            const ::glm::vec3 &normal,
            const void *primitive,
            ::std::int32_t materialIndex,
            const ::glm::vec2 &texCoords = ::glm::vec2 {-1});

        Intersection(const Intersection &intersection) = default;

        Intersection(Intersection &&intersection) noexcept = default;

        ~Intersection() = default;

        Intersection &operator=(const Intersection &intersection) = delete;

        Intersection &operator=(Intersection &&intersection) noexcept = default;
    };
}//namespace MobileRT

#endif //MOBILERT_INTERSECTION_HPP
