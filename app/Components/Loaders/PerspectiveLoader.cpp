#include "Components/Loaders/PerspectiveLoader.hpp"
#include "Components/Cameras/Perspective.hpp"
#include <fstream>
#include <sstream>
#include <string>

using ::Components::PerspectiveLoader;

namespace {
    enum key {
        POSITION = 'p',
        LOOK_AT = 'l',
        UP = 'u',
        FOV = 'f'
    };
}//namespace

::std::unique_ptr<::MobileRT::Camera> PerspectiveLoader::loadFromStream(
        ::std::istream &&cameraDefinition, const float aspectRatio) const {
    ::glm::vec3 position {};
    ::glm::vec3 lookAt {};
    ::glm::vec3 up {};
    ::glm::vec2 fov {};

    ::std::string line {};
    while (::std::getline(cameraDefinition, line)) {
        const auto key {line[0]};
        line.erase(0, 1);
        const auto* const value {line.c_str()};
        switch (key) {
            case POSITION:
                position = ::MobileRT::toVec3(value);
                break;

            case LOOK_AT:
                lookAt = ::MobileRT::toVec3(value);
                break;

            case UP:
                up = ::MobileRT::toVec3(value);
                break;

            case FOV:
                fov = ::MobileRT::toVec2(value);
                break;

            default:
                break;
        }
    }
    // Invert X axis.
    position[0] = -position[0];

    ::std::unique_ptr<::MobileRT::Camera> camera {
            ::MobileRT::std::make_unique<::Components::Perspective> (
                position,
                lookAt,
                up,
                fov[0] * aspectRatio, fov[1]
        )
    };

    return camera;
}
