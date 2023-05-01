#ifndef COMPONENTS_LOADERS_PERSPECTIVELOADER_HPP
#define COMPONENTS_LOADERS_PERSPECTIVELOADER_HPP

#include "MobileRT/CameraLoader.hpp"

#include "Components/Cameras/Perspective.hpp"
#include <fstream>
#include <string>

namespace Components {

    /**
     * A class which loads a perspective camera from a file stream.
     */
    class PerspectiveLoader final : public ::MobileRT::CameraLoader {
        public:
            ::std::unique_ptr<::MobileRT::Camera> loadFromStream(
                    ::std::istream &&cameraDefinition, float aspectRatio) const final;
    };
}//namespace Components

#endif //COMPONENTS_LOADERS_PERSPECTIVELOADER_HPP
