#ifndef MOBILERT_OBJECTLOADER_HPP
#define MOBILERT_OBJECTLOADER_HPP

#include "MobileRT/Sampler.hpp"
#include "MobileRT/Scene.hpp"
#include "MobileRT/Shapes/Triangle.hpp"
#include <functional>
#include <unordered_map>
#include <memory>
#include <string>

namespace MobileRT {
    /**
     * A class which loads a scene from a file and fills the scene with the loaded geometry.
     */
    class ObjectLoader {
    protected:
        /**
         * Whether the object loader already loaded the scene geometry from the file.
         */
        bool isProcessed_ {false};

        /**
         * The number of triangles loaded in the scene.
         */
        ::std::int32_t numberTriangles_ {-1};

    public:
        explicit ObjectLoader() = default;

        ObjectLoader(const ObjectLoader &objectLoader) = delete;

        ObjectLoader(ObjectLoader &&objectLoader) noexcept = delete;

        virtual ~ObjectLoader();

        ObjectLoader &operator=(const ObjectLoader &objectLoader) = delete;

        ObjectLoader &operator=(ObjectLoader &&objectLoader) noexcept = delete;

        bool isProcessed() const;

        /**
         * Fills the scene with the triangles loaded from a geometry file, like .OBJ and .MTL.
         *
         * @param scene               The scene to fill with geometry.
         * @param createSamplerLambda A lambda which returns a sampler.
         * @param filePath            The file path to the main scene file.
         * @param texturesCache       The cache used for texture files. This is necessary if the textures need to be loaded and cached beforehand.
         *                            E.g.: For Android devices, the texture files need to be loaded with Java code due to permissions.
         * @return True if it succeeded to fill the scene or false otherwise.
         */
        virtual bool fillScene(Scene *scene,
                               ::std::function<::std::unique_ptr<Sampler>()> createSamplerLambda,
                               ::std::string filePath,
                               ::std::unordered_map<::std::string, ::MobileRT::Texture> *const texturesCache) = 0;
    };
}//namespace MobileRT

#endif //MOBILERT_OBJECTLOADER_HPP
