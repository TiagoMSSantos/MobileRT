#include "Components/Shaders/DepthMap.hpp"

using ::Components::DepthMap;
using ::MobileRT::Intersection;
using ::MobileRT::Scene;

DepthMap::DepthMap(Scene scene, const ::glm::vec3 &maxPoint, const Accelerator accelerator) :
    Shader {::std::move(scene), 0, accelerator},
    maxPoint_ {maxPoint} {
}

bool DepthMap::shade(::glm::vec3 *const rgb, const Intersection &intersection) {
    const float maxDist {::glm::length(this->maxPoint_ - intersection.ray_.origin_) * 1.1F};
    const float depth {::std::max((maxDist - intersection.length_) / maxDist, 0.0F)};
    *rgb = {depth, depth, depth};
    return false;
}
