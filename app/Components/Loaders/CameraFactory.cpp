#include "Components/Loaders/CameraFactory.hpp"
#include "Components/Loaders/PerspectiveLoader.hpp"

using ::Components::CameraFactory;

::std::unique_ptr<::MobileRT::Camera> CameraFactory::loadFromFile (const ::std::string &filePath) const {
    ::std::unique_ptr<::MobileRT::Camera> camera {};

    ::std::ifstream infile {filePath};
    ::std::string line {};
    while (::std::getline(infile, line)) {
        ::std::istringstream iss {line};
        const char key {line[0]};
        const ::std::string value {&line[2]};
        if (key == 't') {
            if (value == "perspective") {
                camera = ::Components::PerspectiveLoader().loadFromStream(::std::move(infile));
            }
        }
    }

    return camera;
}
