#ifndef MOBILERT_SCENE_HPP
#define MOBILERT_SCENE_HPP

#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Intersection.hpp"
#include "MobileRT/Light.hpp"
#include "MobileRT/Material.hpp"
#include "MobileRT/Ray.hpp"
#include "MobileRT/Shapes/Plane.hpp"
#include "MobileRT/Shapes/Sphere.hpp"
#include "MobileRT/Shapes/Triangle.hpp"
#include "MobileRT/Utils.hpp"
#include <glm/glm.hpp>
#include <vector>

namespace MobileRT {
    class Scene final {
    public:
        ::std::vector<Triangle> triangles_ {};
        ::std::vector<Sphere> spheres_ {};
        ::std::vector<Plane> planes_ {};
        ::std::vector<::std::unique_ptr<Light>> lights_ {};
        ::std::vector<Material> materials_ {};

    private:
        static ::MobileRT::AABB getBoxBounds(const AABB &box1, const AABB &box2);

    public:
        explicit Scene() = default;

        Scene(const Scene &scene) = delete;

        Scene(Scene &&scene) noexcept = default;

        ~Scene();

        Scene &operator=(const Scene &scene) = delete;

        Scene &operator=(Scene &&scene) noexcept = default;

        template<typename T>
        static ::MobileRT::AABB getBounds(const ::std::vector<T> &primitives) {
            ::MobileRT::AABB bounds {::glm::vec3 {RayLengthMax}, ::glm::vec3 {-RayLengthMax}};
            for (const auto &primitive : primitives) {
                bounds = getBoxBounds(primitive.getAABB(), bounds);
            }
            bounds.pointMin_ -= ::glm::vec3 {1.0e-07f};
            bounds.pointMax_ += ::glm::vec3 {1.0e-07f};
            return bounds;
        }
    };
}//namespace MobileRT

#endif //MOBILERT_SCENE_HPP
