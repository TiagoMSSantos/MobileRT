#ifndef COMPONENTS_SHADERS_DEPTHMAP_HPP
#define COMPONENTS_SHADERS_DEPTHMAP_HPP

#include "MobileRT/Shader.hpp"

namespace Components {

    class DepthMap final : public ::MobileRT::Shader {
    private:
        ::glm::vec3 maxPoint_ {};

    private:
        bool shade(::glm::vec3 *rgb, const ::MobileRT::Intersection &intersection) final;

    public:
        explicit DepthMap() = delete;

        explicit DepthMap(
            ::MobileRT::Scene scene,
            const ::glm::vec3 &maxPoint,
            ::MobileRT::Shader::Accelerator accelerator);

        DepthMap(const DepthMap &depthMap) = delete;

        DepthMap(DepthMap &&depthMap) noexcept = delete;

        ~DepthMap() final = default;

        DepthMap &operator=(const DepthMap &depthMap) = delete;

        DepthMap &operator=(DepthMap &&depthMap) noexcept = delete;
    };
}//namespace Components

#endif //COMPONENTS_SHADERS_DEPTHMAP_HPP
