#include "Components/Loaders/CameraFactory.hpp"
#include "Components/Loaders/PerspectiveLoader.hpp"

using ::Components::CameraFactory;

::std::unique_ptr<::MobileRT::Camera> CameraFactory::loadFromFile (
    ::std::istream &isCam, const float aspectRatio) const {

    ::std::unique_ptr<::MobileRT::Camera> camera {};
    ::std::string line {};
    while (::std::getline(isCam, line)) {
        const char key {line[0]};
        line.erase(0, 1);
        if (key == 't') {
            if (line.find("perspective") != ::std::string::npos) {
                camera = ::Components::PerspectiveLoader().loadFromStream(::std::move(isCam), aspectRatio);
            }
        }
    }

    return camera;
}
