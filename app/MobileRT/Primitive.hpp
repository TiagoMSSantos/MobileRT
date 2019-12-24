#ifndef MOBILERT_PRIMITIVE_HPP
#define MOBILERT_PRIMITIVE_HPP

#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Material.hpp"
#include "MobileRT/Ray.hpp"
#include "MobileRT/Shapes/Plane.hpp"
#include "MobileRT/Shapes/Sphere.hpp"
#include "MobileRT/Shapes/Triangle.hpp"

namespace MobileRT {

    template<typename T>
    class Primitive final {
    public:
        T shape_{};
        Material material_{};
        ::std::int32_t lastRayID_{};

    public:
        explicit Primitive() noexcept = delete;

        explicit Primitive(const T &shape, const Material &material) noexcept;

        Primitive(const Primitive &primitive) noexcept = default;

        Primitive(Primitive &&primitive) noexcept = default;

        ~Primitive() noexcept = default;

        Primitive &operator=(const Primitive &primitive) noexcept = default;

        Primitive &operator=(Primitive &&primitive) noexcept = default;

        AABB getAABB() const noexcept;

        Intersection intersect(Intersection intersection, const Ray &ray) noexcept;
    };



    template<typename T>
    Primitive<T>::Primitive(const T &shape, const Material &material) noexcept :
                    shape_{shape},
                    material_{material} {
    }

    template<typename T>
    AABB Primitive<T>::getAABB() const noexcept {
        const AABB &res {this->shape_.getAABB()};
        return res;
    }


    template<typename T>
    Intersection Primitive<T>::intersect(Intersection intersection, const Ray &ray) noexcept {
        if (this->lastRayID_ != ray.id_) {
            const float lastDist {intersection.length_};
            intersection = this->shape_.intersect(intersection, ray);
            intersection.material_ = intersection.length_ < lastDist?
                &this->material_ : intersection.material_;
        }
        lastRayID_ = ray.id_;
        return intersection;
    }



    template<typename T>
    bool intersect(const Primitive<T> &primitive, const AABB &box) noexcept {
        const bool res{primitive.shape_.intersect(box)};
        return res;
    }


}//namespace MobileRT

#endif //MOBILERT_PRIMITIVE_HPP
