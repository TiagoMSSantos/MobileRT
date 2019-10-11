#ifndef MOBILERT_SHADER_HPP
#define MOBILERT_SHADER_HPP

#include "MobileRT/Accelerators/BVH.hpp"
#include "MobileRT/Accelerators/RegularGrid.hpp"
#include "MobileRT/Camera.hpp"
#include "MobileRT/Intersection.hpp"
#include "MobileRT/Ray.hpp"
#include "MobileRT/Sampler.hpp"
#include "MobileRT/Scene.hpp"

namespace MobileRT {
    class Shader {
    public:
        Scene scene_ {};
        RegularGrid regularGrid_ {};

        BVH<Plane> bvhPlanes_{};
        BVH<Sphere> bvhSpheres_{};
        BVH<Triangle> bvhTriangles_{};

        enum Accelerator {
            ACC_NAIVE = 0,
            ACC_REGULAR_GRID,
            ACC_BVH
        };

    private:
        const Accelerator accelerator_{};

    protected:
        const ::std::uint32_t samplesLight_{};

    protected:
        virtual bool shade(
            ::glm::vec3 *rgb, const Intersection &intersection, const Ray &ray) noexcept = 0;

        ::glm::vec3 getCosineSampleHemisphere(const ::glm::vec3 &normal) const noexcept;

    public:
        void initializeAccelerators(Camera *camera) noexcept;

    public:
        explicit Shader () noexcept = delete;

        explicit Shader(
            Scene scene,
            ::std::uint32_t samplesLight,
            Accelerator accelerator) noexcept;

        Shader(const Shader &shader) noexcept = delete;

        Shader(Shader &&shader) noexcept = default;

        virtual ~Shader() noexcept;

        Shader &operator=(const Shader &shader) noexcept = delete;

        Shader &operator=(Shader &&shader) noexcept = delete;

        bool rayTrace(::glm::vec3 *rgb, const Ray &ray) noexcept;

        bool shadowTrace(Intersection intersection, const Ray &ray) noexcept;

        Intersection traceTouch(Intersection intersection, const Ray &ray) noexcept;

        virtual void resetSampling() noexcept;

        ::std::uint32_t getLightIndex ();
    };
}//namespace MobileRT

#endif //MOBILERT_SHADER_HPP
