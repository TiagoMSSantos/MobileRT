#ifndef MOBILERT_SHADER_HPP
#define MOBILERT_SHADER_HPP

#include "MobileRT/Accelerators/BVH.hpp"
#include "MobileRT/Accelerators/Naive.hpp"
#include "MobileRT/Accelerators/RegularGrid.hpp"
#include "MobileRT/Camera.hpp"
#include "MobileRT/Intersection.hpp"
#include "MobileRT/Ray.hpp"
#include "MobileRT/Sampler.hpp"
#include "MobileRT/Scene.hpp"

namespace MobileRT {
    class Shader {
    public:
        enum Accelerator {
            ACC_NONE = 0,
            ACC_NAIVE,
            ACC_REGULAR_GRID,
            ACC_BVH,
        };

    private:
        Naive<Plane> naivePlanes_ {};
        Naive<Sphere> naiveSpheres_ {};
        Naive<Triangle> naiveTriangles_ {};

        RegularGrid<Plane> gridPlanes_ {};
        RegularGrid<Sphere> gridSpheres_ {};
        RegularGrid<Triangle> gridTriangles_ {};

        BVH<Plane> bvhPlanes_ {};
        BVH<Sphere> bvhSpheres_ {};
        BVH<Triangle> bvhTriangles_ {};

    private:
        const Accelerator accelerator_ {};

    protected:
        const ::std::int32_t samplesLight_ {};
        ::std::vector<::std::unique_ptr<Light>> lights_ {};

    private:
        Intersection traceLights(Intersection intersection, const Ray &ray) const noexcept;

    protected:
        virtual bool shade(::glm::vec3 *rgb, const Intersection &intersection, const Ray &ray) noexcept = 0;

        ::glm::vec3 getCosineSampleHemisphere(const ::glm::vec3 &normal) const noexcept;

        ::std::uint32_t getLightIndex ();

    public:
        void initializeAccelerators(Scene scene) noexcept;

    public:
        explicit Shader () noexcept = delete;

        explicit Shader(Scene scene, ::std::int32_t samplesLight, Accelerator accelerator) noexcept;

        Shader(const Shader &shader) noexcept = delete;

        Shader(Shader &&shader) noexcept = default;

        virtual ~Shader() noexcept = default;

        Shader &operator=(const Shader &shader) noexcept = delete;

        Shader &operator=(Shader &&shader) noexcept = delete;

        bool rayTrace(::glm::vec3 *rgb, const Ray &ray) noexcept;

        bool shadowTrace(Intersection intersection, const Ray &ray) noexcept;

        virtual void resetSampling() noexcept;

        const ::std::vector<::MobileRT::Primitive<Plane>>& getPlanes() const noexcept;

        const ::std::vector<::MobileRT::Primitive<Sphere>>& getSpheres() const noexcept;

        const ::std::vector<::MobileRT::Primitive<Triangle>>& getTriangles() const noexcept;

        const ::std::vector<::std::unique_ptr<Light>>& getLights() const noexcept;
    };
}//namespace MobileRT

#endif //MOBILERT_SHADER_HPP
