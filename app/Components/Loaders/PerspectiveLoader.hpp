#ifndef COMPONENTS_LOADERS_PERSPECTIVELOADER_HPP
#define COMPONENTS_LOADERS_PERSPECTIVELOADER_HPP

#include "MobileRT/CameraLoader.hpp"

#include "Components/Cameras/Perspective.hpp"
#include <fstream>
#include <string>

namespace Components {
    class PerspectiveLoader final : public ::MobileRT::CameraLoader {
        public:
            ::std::unique_ptr<::MobileRT::Camera> loadFromStream(::std::istream &&cameraDefinition) const final;
    };
}//namespace Components

#endif //COMPONENTS_LOADERS_PERSPECTIVELOADER_HPP
