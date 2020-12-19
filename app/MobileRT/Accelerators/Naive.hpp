#ifndef MOBILERT_ACCELERATORS_NAIVE_HPP
#define MOBILERT_ACCELERATORS_NAIVE_HPP

#include "MobileRT/Intersection.hpp"
#include "MobileRT/Ray.hpp"
#include <vector>

namespace MobileRT {

    /**
     * A class which represents the Naive acceleration structure.
     * <br>
     * This is basically a structure where all the primitives are just stored in a vector without any specific order.
     * So, there is no acceleration structure and the intersection method must try to intersect the ray with all the
     * primitives in the vector.
     *
     * @tparam T The type of the primitives.
     */
    template<typename T>
    class Naive final {
        private:
            ::std::vector<T> primitives_ {};

        private:
            Intersection intersect(Intersection intersection, const Ray &ray);

        public:
            explicit Naive() = default;

            explicit Naive(::std::vector<T> &&primitives);

            Naive(const Naive &naive) = delete;

            Naive(Naive &&naive) noexcept = default;

            ~Naive();

            Naive &operator=(const Naive &naive) = delete;

            Naive &operator=(Naive &&naive) noexcept = default;

            Intersection trace(Intersection intersection, const Ray &ray);

            Intersection shadowTrace(Intersection intersection, const Ray &ray);

            const ::std::vector<T>& getPrimitives() const;
    };

    /**
     * The constructor.
     *
     * @tparam T The type of the primitives.
     * @param primitives The primitives.
     */
    template<typename T>
    Naive<T>::Naive(::std::vector<T> &&primitives) :
        primitives_ {::std::move(primitives)} {
        LOG_DEBUG(typeid(T).name());
    }

    /**
     * The destructor.
     *
     * @tparam T The type of the primitives.
     */
    template<typename T>
    Naive<T>::~Naive() {
        this->primitives_.clear();
        ::std::vector<T> {}.swap(this->primitives_);
    }

    /**
     * Helper method which calculates the intersection point from the origin of the ray.
     * <br>
     * This method supports two modes:<br>
     *  - trace the ray until finding the nearest intersection point from the origin of the ray<br>
     *  - trace the ray until finding any intersection point from the origin of the ray<br>
     *
     * @tparam T The type of the primitives.
     * @param intersection The previous intersection point of the ray (used to update its data in case it is found a
     * nearest intersection point.
     * @param ray          The casted ray.
     * @return The intersection point of the ray in the scene.
     */
    template<typename T>
    Intersection Naive<T>::intersect(Intersection intersection, const Ray &ray) {
        const auto lastDist {intersection.length_};
        for (auto &primitive : this->primitives_) {
            intersection = primitive.intersect(intersection, ray);
            if (ray.shadowTrace_ && intersection.length_ < lastDist) {
                return intersection;
            }
        }
        return intersection;
    }

    /**
     * This method casts a ray into the geometry and calculates the nearest intersection point from the origin of the
     * ray.
     *
     * @tparam T The type of the primitives.
     * @param intersection The current intersection of the ray with previous primitives.
     * @param ray          The ray to be casted.
     * @return The intersection of the ray with the geometry.
     */
    template<typename T>
    Intersection Naive<T>::trace(Intersection intersection, const Ray &ray) {
        intersection = intersect(intersection, ray);
        return intersection;
    }

    /**
     * This method casts a ray into the geometry and calculates a random intersection point.
     * The intersection point itself is not important, the important is to determine if the ray intersects some
     * primitive in the scene or not.
     *
     * @tparam T The type of the primitives.
     * @param intersection The current intersection of the ray with previous primitives.
     * @param ray          The ray to be casted.
     * @return The intersection of the ray with the geometry.
     */
    template<typename T>
    Intersection Naive<T>::shadowTrace(Intersection intersection, const Ray &ray) {
        intersection = intersect(intersection, ray);
        return intersection;
    }

    /**
     * Gets the primitives.
     *
     * @tparam T The type of the primitives.
     * @return The primitives.
     */
    template<typename T>
    const ::std::vector<T>& Naive<T>::getPrimitives() const {
        return this->primitives_;
    }

}//namespace MobileRT

#endif //MOBILERT_ACCELERATORS_NAIVE_HPP
