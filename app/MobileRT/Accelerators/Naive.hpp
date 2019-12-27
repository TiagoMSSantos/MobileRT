#ifndef MOBILERT_ACCELERATORS_NAIVE_HPP
#define MOBILERT_ACCELERATORS_NAIVE_HPP

#include "MobileRT/Primitive.hpp"
#include "MobileRT/Ray.hpp"
#include <vector>

namespace MobileRT {

    template<typename T>
    class Naive final {
        private:
            ::std::vector<::MobileRT::Primitive<T>> primitives_ {};

        private:
            Intersection intersect(Intersection intersection, const Ray &ray, bool shadowTrace = false) noexcept;

        public:
            explicit Naive() noexcept = default;

            explicit Naive(::std::vector<::MobileRT::Primitive<T>> &&primitives) noexcept;

            Naive(const Naive &naive) noexcept = delete;

            Naive(Naive &&naive) noexcept = default;

            ~Naive() noexcept;

            Naive &operator=(const Naive &naive) noexcept = delete;

            Naive &operator=(Naive &&naive) noexcept = default;

            Intersection trace(Intersection intersection, const Ray &ray) noexcept;

            Intersection shadowTrace(Intersection intersection, const Ray &ray) noexcept;

            const ::std::vector<::MobileRT::Primitive<T>>& getPrimitives() const noexcept;
    };

    template<typename T>
    Naive<T>::Naive(::std::vector<::MobileRT::Primitive<T>> &&primitives) noexcept :
        primitives_ {::std::move(primitives)} {

    }

    template<typename T>
    Naive<T>::~Naive() noexcept {
        this->primitives_.clear();
        ::std::vector<::MobileRT::Primitive<T>> {}.swap(this->primitives_);
    }

    template<typename T>
    Intersection Naive<T>::intersect(Intersection intersection, const Ray &ray, const bool shadowTrace) noexcept {
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
    Intersection Naive<T>::trace(Intersection intersection, const Ray &ray) noexcept {
        intersection = intersect(intersection, ray);
        return intersection;
    }

    template<typename T>
    Intersection Naive<T>::shadowTrace(Intersection intersection, const Ray &ray) noexcept {
        intersection = intersect(intersection, ray, true);
        return intersection;
    }

    template<typename T>
    const ::std::vector<::MobileRT::Primitive<T>>& Naive<T>::getPrimitives() const noexcept {
        return this->primitives_;
    }

}//namespace MobileRT

#endif //MOBILERT_ACCELERATORS_NAIVE_HPP
