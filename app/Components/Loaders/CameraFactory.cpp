#include "Components/Loaders/CameraFactory.hpp"
#include "Components/Loaders/PerspectiveLoader.hpp"

using ::Components::CameraFactory;

::std::unique_ptr<::MobileRT::Camera> CameraFactory::loadFromFile (
        const ::std::string &filePath, const float aspectRatio) const {

    if (errno != 0) {
        ::std::perror("Error before read CAM: ");
        LOG("errno before read CAM (", errno, "): ", ::std::strerror(errno));
        errno = 0;
    }
    ::std::ifstream infile {filePath, ::std::ios::binary};
    if (!infile.is_open() || infile.fail() || errno != 0) {
        const auto errorMessage {::std::string("Error after read CAM `" + filePath + "`.\n") +
                                 ::std::string("errno (") + ::std::to_string(errno) + "): " +
                                 ::std::strerror(errno)
        };
        throw ::std::runtime_error {errorMessage};
    }

    ::std::unique_ptr<::MobileRT::Camera> camera {};
    ::std::string line {};
    while (::std::getline(infile, line)) {
        const auto key {line[0]};
        line.erase(0, 1);
        if (key == 't') {
            if (line.find("perspective") != ::std::string::npos) {
                camera = ::Components::PerspectiveLoader().loadFromStream(::std::move(infile), aspectRatio);
            }
        }
    }

    return camera;
}
