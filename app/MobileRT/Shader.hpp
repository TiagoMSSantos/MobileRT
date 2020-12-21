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
    /**
     * A class which contains the scene geometry into an acceleration structure and takes care of casting rays into the
     * scene.
     */
    class Shader {
    public:
        enum Accelerator {
            ACC_NAIVE = 1,
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

        ::std::vector<Material> materials_ {};

    private:
        const Accelerator accelerator_ {};

    protected:
        const ::std::int32_t samplesLight_ {};
        ::std::vector<::std::unique_ptr<Light>> lights_ {};

    private:
        Intersection traceLights(Intersection intersection) const;

    protected:
        /**
         * Calculates the color of an intersection in the scene.
         * <br/>
         * This method should be implemented with the desired algorithm, like Path Tracing, Whitted,
         * etc.
         *
         * @param rgb          A pointer to store the color of the intersection.
         * @param intersection The intersection data, like point, normal, material, etc.
         * @return             Whether the ray intersected a light or not.
         */
        virtual bool shade(::glm::vec3 *rgb, const Intersection &intersection) = 0;

        ::glm::vec3 getCosineSampleHemisphere(const ::glm::vec3 &normal) const;

        ::std::uint32_t getLightIndex ();

    public:
        void initializeAccelerators(Scene scene);

    public:
        explicit Shader () = delete;

        explicit Shader(Scene scene, ::std::int32_t samplesLight, Accelerator accelerator);

        Shader(const Shader &shader) = delete;

        Shader(Shader &&shader) noexcept = default;

        virtual ~Shader() = default;

        Shader &operator=(const Shader &shader) = delete;

        Shader &operator=(Shader &&shader) noexcept = delete;

        bool rayTrace(::glm::vec3 *rgb, Ray &&ray);

        bool shadowTrace(float distance, Ray &&ray);

        virtual void resetSampling();

        const ::std::vector<Plane>& getPlanes() const;

        const ::std::vector<Sphere>& getSpheres() const;

        const ::std::vector<Triangle>& getTriangles() const;

        const ::std::vector<Material>& getMaterials() const;

        const ::std::vector<::std::unique_ptr<Light>>& getLights() const;
    };
}//namespace MobileRT

#endif //MOBILERT_SHADER_HPP
