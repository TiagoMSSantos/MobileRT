#include "Components/Loaders/OBJLoader.hpp"
#include "Components/Lights/AreaLight.hpp"
#include <cstring>
#include <fstream>
#include <thread>
#include <tuple>
#include <utility>

using ::Components::AreaLight;
using ::Components::OBJLoader;
using ::MobileRT::Light;
using ::MobileRT::Material;
using ::MobileRT::Scene;
using ::MobileRT::Texture;
using ::MobileRT::Triangle;
using ::MobileRT::Sampler;

OBJLoader::OBJLoader(::std::istream& isObj, ::std::istream& isMtl) {
    isObj.exceptions(
        isObj.exceptions() | ::std::ifstream::goodbit | ::std::ifstream::badbit |
        ::std::ifstream::failbit
    );
    isMtl.exceptions(
        isMtl.exceptions() | ::std::ifstream::goodbit | ::std::ifstream::badbit |
        ::std::ifstream::failbit
    );
    ::tinyobj::MaterialStreamReader matStreamReader {isMtl};

    ::tinyobj::MaterialStreamReader *matStreamReaderPtr {&matStreamReader};
    if (isMtl.peek() == ::std::char_traits<char>::eof()) {
        matStreamReaderPtr = nullptr;
    }

    ::std::string errors {};
    ::std::string warnings {};

    LOG_INFO("Going to call tinyobj::LoadObj");
    MobileRT::checkSystemError("Before LoadObj.");
    const bool ret {
        ::tinyobj::LoadObj(
            &this->attrib_, &this->shapes_, &this->materials_,
            &warnings, &errors, &isObj, matStreamReaderPtr, true, true
        )
    };
    // For some reason in Gentoo Linux, the `LoadObj` method fails
    // in release with error code ENOENT: No such file or directory.
    errno = 0;
    MobileRT::checkSystemError("After LoadObj.");

    LOG_DEBUG("Called tinyobj::LoadObj");

    if (!errors.empty()) {
        LOG_ERROR("Error: '", errors, "'");
    }

    if (!warnings.empty()) {
        LOG_WARN("Warning: '", warnings, "'");
    }

    if (ret) {
        this->numberTriangles_ = 0;
        for (const ::tinyobj::shape_t &shape : this->shapes_) {
            for (const unsigned char numFaceVertices : shape.mesh.num_face_vertices) {
                const ::std::uint32_t triangles {static_cast<::std::uint32_t>(numFaceVertices / 3)};
                this->numberTriangles_ += triangles;
            }
        }
        this->isProcessed_ = true;
    }

    LOG_INFO("Called tinyobj::LoadObj and loaded ", this->numberTriangles_, " triangles");
}

bool OBJLoader::fillScene(Scene *const scene,
                          const ::std::function<::std::unique_ptr<Sampler>()> lambda,
                          ::std::string filePath,
                          ::std::unordered_map<::std::string, ::MobileRT::Texture> texturesCache) {
    ::MobileRT::checkSystemError("Starting to fill scene.");
    LOG_INFO("FILLING SCENE with ", this->numberTriangles_, " triangles in ", this->shapes_.size(), " shapes & ", this->materials_.size(), " materials");
    filePath = filePath.substr(0, filePath.find_last_of('/')) + '/';
    ::std::mutex mutex {};

    const ::std::uint32_t numChildren {::std::thread::hardware_concurrency()};
    if (numChildren <= 0) {
        LOG_ERROR("Number of available CPU cores is ", numChildren);
        return false;
    }
    ::std::vector<::std::thread> threads {};
    threads.reserve(numChildren);
    LOG_INFO("Created mutex and it will fill the scene using ", numChildren + 1, " threads");
    for (::std::uint32_t i {}; i < numChildren; ++i) {
        threads.emplace_back(&OBJLoader::fillSceneThreadWork, this, i, numChildren + 1, scene, lambda, filePath, &texturesCache, &mutex);
    }
    fillSceneThreadWork(numChildren, numChildren + 1, scene, lambda, filePath, &texturesCache, &mutex);
    for (::std::thread &thread : threads) {
        thread.join();
    }

    ASSERT(static_cast<::std::int32_t> (scene->triangles_.size()), this->numberTriangles_, "Number of triangles in the scene is not correct.");
    ::MobileRT::checkSystemError("Filled Scene");

    LOG_INFO("Total triangles loaded: ", scene->triangles_.size());
    LOG_INFO("Total lights loaded:  ", scene->lights_.size());
    LOG_INFO("Total materials loaded:  ", scene->materials_.size());

    return true;
}

/**
 * Helper method that loads the vertices' values.
 *
 * @param shape       The shape structure from the tinyobj library.
 * @param indexOffset The indices of the normals in the tinyobjloader structure.
 * @return The loaded vertices.
 */
OBJLoader::triple<::glm::vec3, ::glm::vec3, ::glm::vec3> OBJLoader::loadVertices(
    const ::tinyobj::shape_t &shape,
    const ::std::int32_t indexOffset
) const {
    const auto itIdx {shape.mesh.indices.cbegin() + indexOffset};

    const ::tinyobj::index_t idx1 {*(itIdx + 0)};
    const ::tinyobj::index_t idx2 {*(itIdx + 1)};
    const ::tinyobj::index_t idx3 {*(itIdx + 2)};

    const auto itVertex1 {this->attrib_.vertices.cbegin() + 3 * idx1.vertex_index};
    const float vx1 {*(itVertex1 + 0)};
    const float vy1 {*(itVertex1 + 1)};
    const float vz1 {*(itVertex1 + 2)};
    const auto itVertex2 {this->attrib_.vertices.cbegin() + 3 * idx2.vertex_index};
    const float vx2 {*(itVertex2 + 0)};
    const float vy2 {*(itVertex2 + 1)};
    const float vz2 {*(itVertex2 + 2)};
    const auto itVertex3 {this->attrib_.vertices.cbegin() + 3 * idx3.vertex_index};
    const float vx3 {*(itVertex3 + 0)};
    const float vy3 {*(itVertex3 + 1)};
    const float vz3 {*(itVertex3 + 2)};

    const ::glm::vec3 &vertex1 {-vx1, vy1, vz1};
    const ::glm::vec3 &vertex2 {-vx2, vy2, vz2};
    const ::glm::vec3 &vertex3 {-vx3, vy3, vz3};

    return triple<::glm::vec3, ::glm::vec3, ::glm::vec3> {vertex1, vertex2, vertex3};
}

/**
 * Helper method that loads a normal from the tinyobjloader library structure.
 *
 * @param shape       The shape structure from the tinyobj library.
 * @param indexOffset The indices of the normals in the tinyobjloader structure.
 * @param vertex      The vertices' values of the triangle.
 * @return The loaded normal.
 */
OBJLoader::triple<::glm::vec3, ::glm::vec3, ::glm::vec3> OBJLoader::loadNormal(
    const ::tinyobj::shape_t &shape,
    const ::std::int32_t indexOffset,
    const triple<::glm::vec3, ::glm::vec3, ::glm::vec3> &vertex
) const {
    const auto itIdx {shape.mesh.indices.cbegin() + indexOffset};
    const ::tinyobj::index_t idx1 {*(itIdx + 0)};
    const ::tinyobj::index_t idx2 {*(itIdx + 1)};
    const ::tinyobj::index_t idx3 {*(itIdx + 2)};

    if (!this->attrib_.normals.empty()) {// If it has normals.
        const auto itNormal1 {this->attrib_.normals.cbegin() + 3 * idx1.normal_index};
        const auto itNormal2 {this->attrib_.normals.cbegin() + 3 * idx2.normal_index};
        const auto itNormal3 {this->attrib_.normals.cbegin() + 3 * idx3.normal_index};

        return triple<::glm::vec3, ::glm::vec3, ::glm::vec3> {
            glm::vec3 {-*(itNormal1 + 0), *(itNormal1 + 1), *(itNormal1 + 2)},
            glm::vec3 {-*(itNormal2 + 0), *(itNormal2 + 1), *(itNormal2 + 2)},
            glm::vec3 {-*(itNormal3 + 0), *(itNormal3 + 1), *(itNormal3 + 2)}
        };
    } else {
        // If it doesn't have normals, we have to calculate a normal.
        const ::glm::vec3 AB {::std::get<1>(vertex) - ::std::get<0>(vertex)};
        const ::glm::vec3 AC {::std::get<2>(vertex) - ::std::get<0>(vertex)};

        const ::glm::vec3 normalDir {::glm::normalize(::glm::cross(AC, AB))};
        return triple<::glm::vec3, ::glm::vec3, ::glm::vec3> {
            normalDir, normalDir, normalDir
        };
    }
}

/**
 * Helper method that normalizes the texture coordinates.
 *
 * @param texture  The texture of the texture coordinates.
 * @param texCoord The texture coordinates to normalize.
 * @return The normalized texture coordinates.
 */
OBJLoader::triple<::glm::vec2, ::glm::vec2, ::glm::vec2> OBJLoader::normalizeTexCoord(
    const Texture &texture,
    const triple<::glm::vec2, ::glm::vec2, ::glm::vec2> &texCoord
) {
    if (!texture.isValid()) {// If the texture is not valid.
        // Reset texture coordinates to -1.
        LOG_DEBUG("Resetting texture coordinates to: -1");
        return triple<::glm::vec2, ::glm::vec2, ::glm::vec2> {
            ::glm::vec2 {-1}, ::glm::vec2 {-1}, ::glm::vec2 {-1}
        };
    } else {
        // Normalize the texture coordinates to be between [0, 1]
        LOG_DEBUG("Normalizing texture coordinates: ", ::std::get<0>(texCoord), ", ", ::std::get<1>(texCoord), ", ", ::std::get<2>(texCoord));
        return triple<::glm::vec2, ::glm::vec2, ::glm::vec2> {
            ::MobileRT::normalize(::std::get<0>(texCoord)),
            ::MobileRT::normalize(::std::get<1>(texCoord)),
            ::MobileRT::normalize(::std::get<2>(texCoord))
        };
    }
}

/**
 * Helper method that gets a Texture from a cache passed by a parameter.
 * If the cache, does not have the texture, then it will create one and add it in it.
 *
 * @param texturesCache The cache for the textures.
 * @param textureBinary The texture in binary format.
 * @param size          The size of the texture in bytes.
 * @param texPath       The texture file name.
 * @return The texture loaded.
 */
const Texture& OBJLoader::getTextureFromCache(
    ::std::unordered_map<::std::string, Texture> *const texturesCache,
    ::std::string &&textureBinary,
    const long size,
    const ::std::string &texPath
) {
    const ::std::unordered_map<::std::string, Texture>::iterator itTexture {texturesCache->find(texPath)};

    if (itTexture == texturesCache->cend()) { // If the texture is not in the cache.
        Texture &&texture {Texture::createTexture(::std::move(textureBinary), size)};
        ::std::pair<::std::string, Texture> &&pair {::std::make_pair(texPath, ::std::move(texture))};
        const Texture *res {nullptr};
        const ::std::pair<::std::unordered_map<::std::string, Texture>::iterator, bool> pairResult {texturesCache->emplace(::std::move(pair))}; // Add it to the cache.
        res = &(::std::get<0>(pairResult)->second);
        return *res;
    }

    return texturesCache->find(texPath)->second;// Get texture from cache.
}

/**
 * Helper method that gets a Texture from a cache passed by a parameter.
 * If the cache, does not have the texture, then it will create one and add it in it.
 *
 * @param texturesCache The cache for the textures.
 * @param filePath      The path to the directory of the texture file.
 * @param texPath       The texture file name.
 * @return The texture loaded.
 */
const Texture& OBJLoader::getTextureFromCache(
    ::std::unordered_map<::std::string, Texture> *const texturesCache,
    const ::std::string &filePath,
    const ::std::string &texPath
) {
    const ::std::unordered_map<::std::string, Texture>::iterator itTexture {texturesCache->find(texPath)};

    if (itTexture == texturesCache->cend()) {// If the texture is not in the cache.
        const ::std::string texturePath {filePath + texPath};
        Texture &&texture {Texture::createTexture(texturePath)};
        ::std::pair<::std::string, Texture> &&pair {::std::make_pair(texPath, ::std::move(texture))};
        const Texture *res {nullptr};
        const ::std::pair<::std::unordered_map<::std::string, Texture>::iterator, bool> pairResult {texturesCache->emplace(::std::move(pair))}; // Add it to the cache.
        res = &(::std::get<0>(pairResult)->second);
        return *res;
    }

    return texturesCache->find(texPath)->second;// Get texture from cache.
}

/**
 * Fill the scene with the loaded triangles.
 */
void OBJLoader::fillSceneThreadWork(const ::std::uint32_t threadId,
                                    const ::std::uint32_t numberOfThreads,
                                    Scene *const scene,
                                    const ::std::function<::std::unique_ptr<Sampler>()> &lambda,
                                    const ::std::string &filePath,
                                    ::std::unordered_map<::std::string, ::MobileRT::Texture> *const texturesCache,
                                    ::std::mutex *const mutex) {
    ::std::vector<Triangle> triangles {};
    ::std::vector<::std::unique_ptr<Light>> lights {};
    const ::std::uint32_t shapesSize {static_cast<::std::uint32_t> (this->shapes_.size())};

    // Loop over shapes.
    for (::std::uint32_t shapeIndex {threadId}; shapeIndex < shapesSize; shapeIndex += numberOfThreads) {
        LOG_INFO("Thread ", threadId, " (", numberOfThreads, ") filling scene.");
        const auto itShape {this->shapes_.cbegin() + static_cast<::std::int32_t> (shapeIndex)};
        const ::tinyobj::shape_t &shape {*itShape};

        // Loop over faces in polygon.
        ::std::int32_t indexOffset {0};
        // The number of vertices per face.
        const ::std::int32_t faces {static_cast<::std::int32_t> (shape.mesh.num_face_vertices.size())};
        LOG_DEBUG("Loading shape: ", shapeIndex);
        for (::std::int32_t face = 0; face < faces; ++face) {
            const auto itFace {shape.mesh.num_face_vertices.cbegin() + face};
            const ::std::int32_t faceVertices {static_cast<::std::int32_t>(*itFace)};

            if (faceVertices % 3 != 0) {// If the number of vertices in the face is not multiple of 3,
                                        // then it does not make a triangle.
                LOG_DEBUG("num_face_vertices [", face, "] = '", faceVertices, "'");
                continue;
            }

            // Loop over vertices in the face.
            for (::std::int32_t vertex = 0; vertex < faceVertices; vertex += 3) {
                const OBJLoader::triple<::glm::vec3, ::glm::vec3, ::glm::vec3> vertices {loadVertices(shape, indexOffset + vertex)};
                const OBJLoader::triple<::glm::vec3, ::glm::vec3, ::glm::vec3> normal {loadNormal(shape, indexOffset + vertex, vertices)};

                // per-face material.
                const auto itMaterialShape {shape.mesh.material_ids.cbegin() + face};
                const int materialId {*itMaterialShape};

                const auto itIdx {shape.mesh.indices.cbegin() + indexOffset + vertex};
                const ::tinyobj::index_t idx1 {*(itIdx + 0)};
                const ::tinyobj::index_t idx2 {*(itIdx + 1)};
                const ::tinyobj::index_t idx3 {*(itIdx + 2)};

                // If it contains material.
                if (materialId >= 0) {
                    const auto itMaterial {this->materials_.cbegin() + static_cast<::std::int32_t> (materialId)};
                    const ::tinyobj::material_t &mat {*itMaterial};
                    const ::glm::vec3 &diffuse {::MobileRT::toVec3(mat.diffuse)};
                    const ::glm::vec3 &specular {::MobileRT::toVec3(mat.specular)};
                    const ::glm::vec3 &transmittance {::MobileRT::toVec3(mat.transmittance) * (1.0F - mat.dissolve)};
                    const ::glm::vec3 &emission {::MobileRT::normalize(::MobileRT::toVec3(mat.emission))};
                    const float indexRefraction {mat.ior};

                    const bool hasTexture {!mat.diffuse_texname.empty()};
                    const bool hasCoordTex {!this->attrib_.texcoords.empty()};
                    Texture texture {};
                    ::std::tuple<::glm::vec2, ::glm::vec2, ::glm::vec2> texCoord {::std::make_tuple(::glm::vec2 {-1}, ::glm::vec2 {-1}, ::glm::vec2 {-1})};
                    if (hasTexture && hasCoordTex) {
                        const auto itTexCoords1 {
                            this->attrib_.texcoords.cbegin() +
                            2 * static_cast<::std::int32_t> (idx1.texcoord_index)
                        };
                        const float tx1 {*(itTexCoords1 + 0)};
                        const float ty1 {*(itTexCoords1 + 1)};

                        const auto itTexCoords2 {
                            this->attrib_.texcoords.cbegin() +
                            2 * static_cast<::std::int32_t> (idx2.texcoord_index)
                        };
                        const float tx2 {*(itTexCoords2 + 0)};
                        const float ty2 {*(itTexCoords2 + 1)};

                        const auto itTexCoords3 {
                            this->attrib_.texcoords.cbegin() +
                            2 * static_cast<::std::int32_t> (idx3.texcoord_index)
                        };
                        const float tx3 {*(itTexCoords3 + 0)};
                        const float ty3 {*(itTexCoords3 + 1)};

                        texCoord = triple<::glm::vec2, ::glm::vec2, ::glm::vec2> {
                            ::glm::vec2 {tx1, ty1}, ::glm::vec2 {tx2, ty2}, ::glm::vec2 {tx3, ty3}
                        };

                        texture = getTextureFromCache(texturesCache, filePath, mat.diffuse_texname);
                        texCoord = normalizeTexCoord(texture, texCoord);
                    }

                    Material material {diffuse, specular, transmittance, indexRefraction, emission, texture};
                    if (::MobileRT::hasPositiveValue(emission)) {
                        // If the primitive is a light source.
                        const Triangle &triangle {
                            Triangle::Builder(
                                ::std::get<0> (vertices), ::std::get<1> (vertices), ::std::get<2> (vertices)
                            )
                            .withNormals(
                                ::std::get<0>(normal),
                                ::std::get<1>(normal),
                                ::std::get<2>(normal)
                            )
                            .withTexCoords(
                                ::std::get<0>(texCoord),
                                ::std::get<1>(texCoord),
                                ::std::get<2>(texCoord)
                            )
                            .build()
                        };
                        {
                            lights.emplace_back(::MobileRT::std::make_unique<AreaLight>(material, lambda(), triangle));
                        }
                    } else {
                        // If it is a primitive.
                        Triangle::Builder builder {
                            Triangle::Builder(
                                ::std::get<0> (vertices), ::std::get<1> (vertices), ::std::get<2> (vertices)
                            )
                            .withNormals(
                                ::std::get<0>(normal),
                                ::std::get<1>(normal),
                                ::std::get<2>(normal)
                            )
                            .withTexCoords(
                                ::std::get<0>(texCoord),
                                ::std::get<1>(texCoord),
                                ::std::get<2>(texCoord)
                            )
                        };

                        ::std::int32_t materialIndex {-1};
                        {
                            const ::std::lock_guard<::std::mutex> lock {*mutex};
                            const auto itFoundMat {::std::find(scene->materials_.begin(), scene->materials_.end(), material)};
                            if (itFoundMat != scene->materials_.cend()) {
                                // If the material is already in the scene.
                                materialIndex = static_cast<::std::int32_t> (itFoundMat - scene->materials_.cbegin());
                            } else {
                                // If the scene doesn't have material yet.
                                materialIndex = static_cast<::std::int32_t> (scene->materials_.size());
                                scene->materials_.emplace_back(::std::move(material));
                            }
                        }
                        triangles.emplace_back(builder.withMaterialIndex(materialIndex).build());
                    }
                } else {
                    // If it doesn't contain material.
                    const auto itColor {this->attrib_.colors.cbegin() + 3 * idx1.vertex_index};
                    const float red {*(itColor + 0)};
                    const float green {*(itColor + 1)};
                    const float blue {*(itColor + 2)};

                    const ::glm::vec3 &diffuse {red, green, blue};
                    const ::glm::vec3 &specular {0.0F, 0.0F, 0.0F};
                    const ::glm::vec3 &transmittance {0.0F, 0.0F, 0.0F};
                    const float indexRefraction {1.0F};
                    const ::glm::vec3 &emission {0.0F, 0.0F, 0.0F};
                    Material material {diffuse, specular, transmittance, indexRefraction, emission};
                    Triangle::Builder builder {
                        Triangle::Builder(
                            ::std::get<0> (vertices), ::std::get<1> (vertices), ::std::get<2> (vertices)
                        )
                        .withNormals(
                            ::std::get<0>(normal),
                            ::std::get<1>(normal),
                            ::std::get<2>(normal)
                        )
                    };
                    ::std::int32_t materialIndex {-1};
                    {
                        const ::std::lock_guard<::std::mutex> lock {*mutex};
                        const auto itFoundMat {::std::find(scene->materials_.begin(), scene->materials_.end(), material)};
                        if (itFoundMat != scene->materials_.cend()) {
                            // If the material is already in the scene.
                            materialIndex = static_cast<::std::int32_t> (itFoundMat - scene->materials_.cbegin());
                        } else {
                            // If the scene doesn't have material yet.
                            materialIndex = static_cast<::std::int32_t> (scene->materials_.size());
                            scene->materials_.emplace_back(::std::move(material));
                        }
                    }
                    triangles.emplace_back(builder.withMaterialIndex(materialIndex).build());
                }
            } // Loop over vertices in the face.

            indexOffset += faceVertices;

            if (triangles.size() > 0 && triangles.size() % 100000 == 0) {
                LOG_DEBUG("Triangle ", triangles.size(), " position at ", triangles.back());
            } else if (lights.size() > 0 && lights.size() % 1000 == 0) {
                LOG_DEBUG("Light ", lights.size(), " position at: ", lights.back()->getPosition(), ", radiance: ", lights.back()->radiance_.Le_);
            }
        } // The number of vertices per face.
    } // Loop over shapes.

    {
        const ::std::lock_guard<::std::mutex> lock {*mutex};
        if (triangles.size() > 0) {
            LOG_INFO("Local triangles: ", triangles.size(), ", total: ", scene->triangles_.size(), ", last triangle: ", triangles.back());
        }
        if (lights.size() > 0) {
            const ::std::unique_ptr<Light> &light {lights.back()};
            const ::glm::vec3 &lightPos {light->getPosition()};
            const ::glm::vec3 &lightRadiance {light->radiance_.Le_};
            LOG_INFO("Local lights:  ", lights.size(), ", total: ", scene->lights_.size(), ", last light: ", lightPos, ", radiance: ", lightRadiance);
        }

        scene->triangles_.reserve(scene->triangles_.size() + triangles.size());
        ::std::move(::std::begin(triangles), ::std::end(triangles), ::std::back_inserter(scene->triangles_));

        scene->lights_.reserve(scene->lights_.size() + lights.size());
        ::std::move(::std::begin(lights), ::std::end(lights), ::std::back_inserter(scene->lights_));
    }

    triangles.clear();
    triangles.shrink_to_fit();
    ::std::vector<Triangle> {}.swap(triangles);

    lights.clear();
    lights.shrink_to_fit();
    ::std::vector<::std::unique_ptr<Light>> {}.swap(lights);
}

/**
 * The destructor.
 */
OBJLoader::~OBJLoader() {
    this->attrib_.normals.clear();
    this->attrib_.texcoords.clear();
    this->attrib_.vertices.clear();
    this->shapes_.clear();
    this->materials_.clear();

    this->attrib_.normals.shrink_to_fit();
    this->attrib_.texcoords.shrink_to_fit();
    this->attrib_.vertices.shrink_to_fit();
    this->shapes_.shrink_to_fit();
    this->materials_.shrink_to_fit();

    ::std::vector<::tinyobj::shape_t> {}.swap(this->shapes_);
    ::std::vector<::tinyobj::material_t> {}.swap(this->materials_);
    ::std::vector<::tinyobj::real_t> {}.swap(this->attrib_.normals);
    ::std::vector<::tinyobj::real_t> {}.swap(this->attrib_.texcoords);
    ::std::vector<::tinyobj::real_t> {}.swap(this->attrib_.vertices);

    LOG_DEBUG("OBJLOADER DELETED");
}
