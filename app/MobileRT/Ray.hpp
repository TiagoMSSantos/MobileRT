#ifndef MOBILERT_RAY_HPP
#define MOBILERT_RAY_HPP

#include "MobileRT/Utils.hpp"
#include <glm/glm.hpp>

namespace MobileRT {
    class Ray final {
    public:
        const ::glm::vec3 origin_ {};
        const ::glm::vec3 direction_  {};
        const ::std::int32_t depth_{};
        const ::std::int32_t id_ {};
        const void *const primitive_ {};

    public:
        explicit Ray () = delete;

        explicit Ray(const ::glm::vec3 &dir, const ::glm::vec3 &origin,
                     ::std::int32_t depth, const void *primitive = nullptr);

        Ray(const Ray &ray) = default;

        Ray(Ray &&ray) noexcept = default;

        ~Ray() = default;

        Ray &operator=(const Ray &ray) = delete;

        Ray &operator=(Ray &&ray) noexcept = delete;
    };
}//namespace MobileRT

#endif //MOBILERT_RAY_HPP
