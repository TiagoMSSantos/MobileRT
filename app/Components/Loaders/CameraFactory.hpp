#ifndef COMPONENTS_LOADERS_CAMERAFACTORY_HPP
#define COMPONENTS_LOADERS_CAMERAFACTORY_HPP

#include "MobileRT/Camera.hpp"

namespace Components {
    class CameraFactory {
        public:
            ::std::unique_ptr<::MobileRT::Camera> loadFromFile(const ::std::string &filePath) const;
    };
}//namespace Components

#endif //COMPONENTS_LOADERS_CAMERAFACTORY_HPP
