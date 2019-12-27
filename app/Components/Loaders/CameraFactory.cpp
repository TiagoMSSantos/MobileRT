#include "Components/Loaders/CameraFactory.hpp"
#include "Components/Loaders/PerspectiveLoader.hpp"

using ::Components::CameraFactory;

::std::unique_ptr<::MobileRT::Camera> CameraFactory::loadFromFile (
        const ::std::string &filePath, const float aspectRatio) const {
    ::std::unique_ptr<::MobileRT::Camera> camera {};

    ::std::ifstream infile {filePath};
    ::std::string line {};
    while (::std::getline(infile, line)) {
        const auto key {line[0]};
        line.erase(0, 1);
        switch (key) {
            case 't':
                if (line.find("perspective") != ::std::string::npos) {
                    camera = ::Components::PerspectiveLoader().loadFromStream(::std::move(infile), aspectRatio);
                }
                break;

            default:
                break;
        }
    }

    return camera;
}
