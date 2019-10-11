#include "Components/Shaders/DepthMap.hpp"

using ::Components::DepthMap;
using ::MobileRT::Intersection;
using ::MobileRT::Ray;
using ::MobileRT::Scene;

DepthMap::DepthMap(
    Scene scene, const ::glm::vec3 &maxPoint, const Accelerator accelerator) noexcept
        :
        Shader{::std::move(scene), 0, accelerator},
        maxPoint_{maxPoint} {
}

bool DepthMap::shade(
    ::glm::vec3 *const rgb, const Intersection &intersection, const Ray &ray) noexcept {

    const float maxDist {::glm::length(maxPoint_ - ray.origin_) * 1.1f};
    const float depth {::std::max((maxDist - intersection.length_) / maxDist, 0.0f)};
    *rgb = {depth, depth, depth};
    return false;
}
