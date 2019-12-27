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
        ::std::vector<::MobileRT::Primitive<Triangle>> triangles_ {};
        ::std::vector<::MobileRT::Primitive<Sphere>> spheres_ {};
        ::std::vector<::MobileRT::Primitive<Plane>> planes_ {};
        ::std::vector<::std::unique_ptr<Light>> lights_ {};

    private:
        static ::MobileRT::AABB getAABBbounds(const AABB &box1, const AABB &box2);

    public:
        explicit Scene() = default;

        Scene(const Scene &scene) noexcept = delete;

        Scene(Scene &&scene) noexcept = default;

        ~Scene() noexcept;

        Scene &operator=(const Scene &scene) noexcept = delete;

        Scene &operator=(Scene &&scene) noexcept = default;

        template<typename T>
        static ::MobileRT::AABB getBounds(const ::std::vector<T> &primitives) {
            ::MobileRT::AABB bounds {::glm::vec3 {RayLengthMax}, ::glm::vec3 {-RayLengthMax}};
            for (const auto &primitive : primitives) {
                bounds = getAABBbounds(primitive.getAABB(), bounds);
            }
            return bounds;
        }
    };
}//namespace MobileRT

#endif //MOBILERT_SCENE_HPP
