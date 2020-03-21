#ifndef MOBILERT_RAY_HPP
#define MOBILERT_RAY_HPP

#include "MobileRT/Utils.hpp"
#include <glm/glm.hpp>

namespace MobileRT {
    /**
     * A class which represents a ray casted into the scene.
     * <br>
     * A ray consists of an origin and a direction of a vector.
     */
    class Ray final {
    public:
        const ::glm::vec3 origin_ {0};
        const ::glm::vec3 direction_  {0};
        const ::std::int32_t depth_{-1};
        const ::std::int32_t id_ {-1};
        const void *const primitive_ {nullptr};

    private:
        void checkArguments() const;

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
