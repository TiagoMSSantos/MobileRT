#ifndef MOBILERT_CAMERALOADER_HPP
#define MOBILERT_CAMERALOADER_HPP

#include "MobileRT/Camera.hpp"

namespace MobileRT {
    class CameraLoader {
    public:
        explicit CameraLoader() noexcept = default;

        CameraLoader(const CameraLoader& cameraLoader) noexcept = default;

        CameraLoader(CameraLoader&& cameraLoader) noexcept = default;

        CameraLoader &operator=(const CameraLoader &cameraLoader) noexcept = default;

        CameraLoader &operator=(CameraLoader &&cameraLoader) noexcept = default;

        virtual ~CameraLoader() noexcept;

        virtual ::std::unique_ptr<Camera> loadFromStream(
                ::std::istream &&cameraDefinition, float aspectRatio) const = 0;
    };
}//namespace MobileRT

#endif //MOBILERT_CAMERALOADER_HPP
