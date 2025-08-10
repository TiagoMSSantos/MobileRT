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
#include "MobileRT/Utils/Constants.hpp"
#include <glm/glm.hpp>
#include <vector>

namespace MobileRT {
    /**
     * A class which represents a scene for the Ray Tracer engine to cast rays into.
     */
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

        /**
         * Calculates the bounding boxes which surrounds all the primitives in a vector.
         *
         * @tparam T The type of the primitives.
         * @param primitives The primitives to calculate the bounding box.
         * @return The bounding box which surrounds all the primitives.
         */
        template<typename T>
        static ::MobileRT::AABB getBounds(const ::std::vector<T> &primitives) {
            ::MobileRT::AABB bounds {::glm::vec3 {RayLengthMax}, ::glm::vec3 {-RayLengthMax}};
            for (const T &primitive : primitives) {
                bounds = getBoxBounds(primitive.getAABB(), bounds);
            }
            const ::MobileRT::AABB res {
                bounds.getPointMin() - ::glm::vec3 {Epsilon},
                bounds.getPointMax() + ::glm::vec3 {Epsilon}
            };
            return res;
        }
    };
}//namespace MobileRT

#endif //MOBILERT_SCENE_HPP
