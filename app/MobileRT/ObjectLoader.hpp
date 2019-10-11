#ifndef MOBILERT_OBJECTLOADER_HPP
#define MOBILERT_OBJECTLOADER_HPP

#include "MobileRT/Sampler.hpp"
#include "MobileRT/Scene.hpp"
#include "MobileRT/Shapes/Triangle.hpp"
#include <functional>
#include <memory>
#include <string>

namespace MobileRT {
    class ObjectLoader {
    protected:
        bool isProcessed_{false};
        ::std::int32_t numberTriangles_{-1};

    public:
        explicit ObjectLoader() noexcept = default;

        ObjectLoader(const ObjectLoader &objectLoader) noexcept = delete;

        ObjectLoader(ObjectLoader &&objectLoader) noexcept = delete;

        virtual ~ObjectLoader() noexcept;

        ObjectLoader &operator=(const ObjectLoader &objectLoader) noexcept = delete;

        ObjectLoader &operator=(ObjectLoader &&objectLoader) noexcept = delete;

        virtual ::std::int32_t process() noexcept = 0;

        bool isProcessed() const noexcept;

        virtual bool fillScene(Scene *scene,
                               ::std::function<::std::unique_ptr<Sampler>()> lambda) noexcept = 0;
    };
}//namespace MobileRT

#endif //MOBILERT_OBJECTLOADER_HPP
