#ifndef COMPONENTS_LOADERS_OBJLOADER_HPP
#define COMPONENTS_LOADERS_OBJLOADER_HPP

#include "MobileRT/ObjectLoader.hpp"
#include "MobileRT/Sampler.hpp"
#include "MobileRT/Scene.hpp"
#include "MobileRT/Texture.hpp"

#include <mutex>
#include <unordered_map>
#include <tinyobjloader/tiny_obj_loader.h>

namespace Components {

    /**
     * A class which loads a scene from an OBJ file and fills the scene with the loaded geometry.
     */
    class OBJLoader final : public ::MobileRT::ObjectLoader {
    private:
        template<typename T1, typename T2, typename T3>
        using triple = ::std::tuple<T1, T2, T3>;

    private:
        ::tinyobj::attrib_t attrib_ {};
        ::std::vector<::tinyobj::shape_t> shapes_ {};
        ::std::vector<::tinyobj::material_t> materials_ {};

    public:
        explicit OBJLoader() = delete;

        explicit OBJLoader(::std::istream&& isObj, ::std::istream&& isMtl);

        OBJLoader(const OBJLoader &objLoader) = delete;

        OBJLoader(OBJLoader &&objLoader) noexcept = delete;

        ~OBJLoader() noexcept final;

        OBJLoader &operator=(const OBJLoader &objLoader) = delete;

        OBJLoader &operator=(OBJLoader &&objLoader) noexcept = delete;

        bool fillScene(::MobileRT::Scene *scene,
                       ::std::function<::std::unique_ptr<::MobileRT::Sampler>()> createSamplerLambda,
                       ::std::string filePath,
                       ::std::unordered_map<::std::string, ::MobileRT::Texture> *const texturesCache) final;

    private:
        triple<::glm::vec3, ::glm::vec3, ::glm::vec3> loadNormal(
            const ::tinyobj::shape_t &index,
            ::std::int32_t indexOffset,
            const triple<::glm::vec3, ::glm::vec3, ::glm::vec3> &vertex) const;

        triple<::glm::vec3, ::glm::vec3, ::glm::vec3> loadVertices(
            const ::tinyobj::shape_t &shape,
            ::std::int32_t indexOffset) const;

        /**
         * Helper method that gets a Texture from a cache passed by a parameter.
         * If the cache does not have the texture, then it will create one and add it in it.
         *
         * @param texturesCache The cache for the textures.
         * @param mutexCache    Mutex for the texture cache.
         * @param filePath      The path to the directory of the texture file.
         * @param texPath       The texture file name.
         * @return The texture loaded.
         */
        static ::MobileRT::Texture getTextureFromCache(
            ::std::unordered_map<::std::string, ::MobileRT::Texture> *const texturesCache,
            ::std::mutex *const mutexCache,
            const ::std::string &filePath,
            const ::std::string &texPath
        );

       /**
        * Fill the scene with the loaded triangles.
        * <p>
        * This method is called by the fillScene method with multiple threads.
        *
        * @param threadId            The thread ID.
        * @param numberOfThreads     Total number of threads.
        * @param scene               The scene to fill.
        * @param mutexSceneTriangles Mutex for the triangles in the scene.
        * @param mutexSceneMaterials Mutex for the materials in the scene.
        * @param mutexSceneLights    Mutex for the lights in the scene.
        * @param createSamplerLambda A function to create a Sampler.
        * @param filePath            The path to the scene files.
        * @param texturesCache       The cache for the textures.
        * @param mutexCache          Mutex for the texture cache.
        */
        void fillSceneThreadWork(::std::uint32_t threadId,
                                 ::std::uint32_t numberOfThreads,
                                 ::MobileRT::Scene *const scene,
                                 ::std::mutex *const mutexSceneTriangles,
                                 ::std::mutex *const mutexSceneMaterials,
                                 ::std::mutex *const mutexSceneLights,
                                 const ::std::function<::std::unique_ptr<::MobileRT::Sampler>()> &createSamplerLambda,
                                 const ::std::string &filePath,
                                 ::std::unordered_map<::std::string, ::MobileRT::Texture> *const texturesCache,
                                 ::std::mutex *const mutexCache);

    public:
        static ::MobileRT::Texture getTextureFromCache(
            ::std::unordered_map<::std::string, ::MobileRT::Texture> *const texturesCache,
            ::std::string &&textureBinary,
            long size,
            const ::std::string &texPath
        );

    private:
        static triple<::glm::vec2, ::glm::vec2, ::glm::vec2> normalizeTexCoord(
            const ::std::tuple<::glm::vec2, ::glm::vec2, ::glm::vec2> &texCoord
        );
    };
}//namespace Components

#endif //COMPONENTS_LOADERS_OBJLOADER_HPP
