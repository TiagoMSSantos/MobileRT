#ifndef MOBILERT_SCENE_HPP
#define MOBILERT_SCENE_HPP

#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Intersection.hpp"
#include "MobileRT/Light.hpp"
#include "MobileRT/Material.hpp"
#include "MobileRT/Primitive.hpp"
#include "MobileRT/Ray.hpp"
#include "MobileRT/Utils.hpp"
#include <glm/glm.hpp>
#include <vector>

namespace MobileRT {
    class Scene final {
    public:
        ::std::vector<Primitive<Triangle>> triangles_{};
        ::std::vector<Primitive<Sphere>> spheres_{};
        ::std::vector<Primitive<Plane>> planes_{};
        ::std::vector<::std::unique_ptr<Light>> lights_{};

    private:
        template<typename T>
        Intersection trace(::std::vector<T> &primitives, Intersection intersection, const Ray &ray) noexcept;

        template<typename T>
        Intersection shadowTrace(::std::vector<T> &primitives, Intersection intersection,
                         const Ray &ray) const noexcept;

        static void AABBbounds(const AABB &box, ::glm::vec3 *min, ::glm::vec3 *max);

    public:
        explicit Scene() = default;

        Scene(const Scene &scene) noexcept = delete;

        Scene(Scene &&scene) noexcept = default;

        ~Scene() noexcept;

        Scene &operator=(const Scene &scene) noexcept = delete;

        Scene &operator=(Scene &&scene) noexcept = default;

        Intersection trace(Intersection intersection, const Ray &ray) noexcept;

        Intersection shadowTrace(Intersection intersection, const Ray &ray) noexcept;

        Intersection traceLights(Intersection intersection, const Ray &ray) const noexcept;

        void resetSampling() noexcept;

        template<typename T>
        static void getBounds(
            const ::std::vector<T *> primitives, ::glm::vec3 *const min, ::glm::vec3 *const max) {
            for (const T *const primitive : primitives) {
                AABBbounds(primitive->getAABB(), min, max);
            }
        }
    };
}//namespace MobileRT

#endif //MOBILERT_SCENE_HPP
