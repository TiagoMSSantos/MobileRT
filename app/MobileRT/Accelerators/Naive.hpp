#ifndef MOBILERT_ACCELERATORS_NAIVE_HPP
#define MOBILERT_ACCELERATORS_NAIVE_HPP

#include "MobileRT/Intersection.hpp"
#include "MobileRT/Ray.hpp"
#include <vector>

namespace MobileRT {

    template<typename T>
    class Naive final {
        private:
            ::std::vector<T> primitives_ {};

        private:
            Intersection intersect(Intersection intersection, const Ray &ray, bool shadowTrace = false);

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

    template<typename T>
    Naive<T>::Naive(::std::vector<T> &&primitives) :
        primitives_ {::std::move(primitives)} {

    }

    template<typename T>
    Naive<T>::~Naive() {
        this->primitives_.clear();
        ::std::vector<T> {}.swap(this->primitives_);
    }

    template<typename T>
    Intersection Naive<T>::intersect(Intersection intersection, const Ray &ray, const bool shadowTrace) {
        const auto lastDist {intersection.length_};
        for (auto &primitive : this->primitives_) {
            intersection = primitive.intersect(intersection, ray);
            if (shadowTrace && intersection.length_ < lastDist) {
                return intersection;
            }
        }
        return intersection;
    }

    template<typename T>
    Intersection Naive<T>::trace(Intersection intersection, const Ray &ray) {
        intersection = intersect(intersection, ray);
        return intersection;
    }

    template<typename T>
    Intersection Naive<T>::shadowTrace(Intersection intersection, const Ray &ray) {
        intersection = intersect(intersection, ray, true);
        return intersection;
    }

    template<typename T>
    const ::std::vector<T>& Naive<T>::getPrimitives() const {
        return this->primitives_;
    }

}//namespace MobileRT

#endif //MOBILERT_ACCELERATORS_NAIVE_HPP
