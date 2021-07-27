#ifndef COMPONENTS_LOADERS_CAMERAFACTORY_HPP
#define COMPONENTS_LOADERS_CAMERAFACTORY_HPP

#include "MobileRT/Camera.hpp"

namespace Components {

    /**
     * A factory to create cameras.
     */
    class CameraFactory {
        public:
            ::std::unique_ptr<::MobileRT::Camera> loadFromFile(const ::std::string &filePath, float aspectRatio) const;
    };
}//namespace Components

#endif //COMPONENTS_LOADERS_CAMERAFACTORY_HPP
