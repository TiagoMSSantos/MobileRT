#ifndef COMPONENTS_OBJECTLOADERS_OBJLOADER_HPP
#define COMPONENTS_OBJECTLOADERS_OBJLOADER_HPP

#include "MobileRT/ObjectLoader.hpp"
#include "MobileRT/Sampler.hpp"
#include <tinyobjloader/tiny_obj_loader.h>

namespace Components {
    class OBJLoader final : public ::MobileRT::ObjectLoader {
    private:
        ::std::string objFilePath_ {};
        ::std::string mtlFilePath_ {};
        ::tinyobj::attrib_t attrib_ {};
        ::std::vector<::tinyobj::shape_t> shapes_ {};
        ::std::vector<::tinyobj::material_t> materials_ {};

    public:
        explicit OBJLoader () noexcept = delete;

        explicit OBJLoader(::std::string obj, ::std::string materials) noexcept;

        OBJLoader(const OBJLoader &objLoader) noexcept = delete;

        OBJLoader(OBJLoader &&objLoader) noexcept = delete;

        ~OBJLoader() noexcept final;

        OBJLoader &operator=(const OBJLoader &objLoader) noexcept = delete;

        OBJLoader &operator=(OBJLoader &&objLoader) noexcept = delete;

        ::std::int32_t process() noexcept final;

        bool fillScene(::MobileRT::Scene *scene,
                       ::std::function<::std::unique_ptr<::MobileRT::Sampler>()> lambda) noexcept final;
    };
}//namespace Components

#endif //COMPONENTS_OBJECTLOADERS_OBJLOADER_HPP
