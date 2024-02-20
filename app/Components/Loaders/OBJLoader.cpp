#include "Components/Loaders/OBJLoader.hpp"
#include "Components/Lights/AreaLight.hpp"
#include <cstring>
#include <fstream>
#include <map>
#include <tuple>
#include <utility>

using ::Components::AreaLight;
using ::Components::OBJLoader;
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

    LOG_DEBUG("Going to call tinyobj::LoadObj");
    MobileRT::checkSystemError("Before LoadObj.");
    const auto ret {
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
        for (const auto &shape : this->shapes_) {
            for (const auto numFaceVertices : shape.mesh.num_face_vertices) {
                const auto triangles {static_cast<::std::uint32_t>(numFaceVertices / 3)};
                this->numberTriangles_ += triangles;
            }
        }
        this->isProcessed_ = true;
    }
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
static const Texture& getTextureFromCache(
    ::std::map<::std::string, Texture> *const texturesCache,
    const ::std::string &filePath,
    const ::std::string &texPath
) {
    const auto itTexture {texturesCache->find(texPath)};

    if (itTexture == texturesCache->cend()) {// If the texture is not in the cache.
        const auto texturePath {filePath + texPath};
        LOG_DEBUG("Loading texture: ", texturePath);
        auto &&texture {Texture::createTexture(texturePath)};
        auto &&pair {::std::make_pair(texPath, ::std::move(texture))};
        const auto res {::std::get<0> (texturesCache->emplace(::std::move(pair)))};// Add it to the cache.
        LOG_DEBUG("Texture loaded: ", texturePath, ", is valid: ", res->second.isValid()? "true" : "false");
        return res->second;
    }

    return texturesCache->find(texPath)->second;// Get texture from cache.
}

bool OBJLoader::fillScene(Scene *const scene,
                          ::std::function<::std::unique_ptr<Sampler>()> lambda,
                          ::std::string filePath,
                          ::std::map<::std::string, ::MobileRT::Texture> texturesCache) {
    LOG_DEBUG("FILLING SCENE");
    scene->triangles_.reserve(static_cast<::std::uint32_t> (this->numberTriangles_));
    filePath = filePath.substr(0, filePath.find_last_of('/')) + '/';

    // Loop over shapes.
    for (const auto &shape : this->shapes_) {
        // Loop over faces(polygon).
        ::std::uint32_t indexOffset {};
        // The number of vertices per face.
        for (::std::uint32_t face {}; face < shape.mesh.num_face_vertices.size(); ++face) {
            const auto it {shape.mesh.num_face_vertices.cbegin() + static_cast<::std::int32_t> (face)};
            const ::std::uint32_t faceVertices {*it};

            if (faceVertices % 3 != 0) {// If the number of vertices in the face is not multiple of 3,
                                        // then it does not make a triangle.
                LOG_DEBUG("num_face_vertices [", face, "] = '", faceVertices, "'");
                continue;
            }

            // Loop over vertices in the face.
            for (::std::uint32_t vertex {}; vertex < faceVertices; vertex += 3) {
                const auto vertices {loadVertices(shape, static_cast<::std::int32_t> (indexOffset + vertex))};
                const auto normal {loadNormal(shape, static_cast<::std::int32_t> (indexOffset + vertex), vertices)};

                // per-face material.
                const auto itMaterialShape {shape.mesh.material_ids.cbegin() + static_cast<::std::int32_t> (face)};
                const auto materialId {*itMaterialShape};

                const auto itIdx {shape.mesh.indices.cbegin() + static_cast<::std::int32_t> (indexOffset + vertex)};
                const auto idx1 {*(itIdx + 0)};
                const auto idx2 {*(itIdx + 1)};
                const auto idx3 {*(itIdx + 2)};

                // If it contains material.
                if (materialId >= 0) {
                    const auto itMaterial {this->materials_.cbegin() + static_cast<::std::int32_t> (materialId)};
                    const auto &mat {*itMaterial};
                    const ::glm::vec3 &diffuse {::MobileRT::toVec3(mat.diffuse)};
                    const ::glm::vec3 &specular {::MobileRT::toVec3(mat.specular)};
                    const ::glm::vec3 &transmittance {::MobileRT::toVec3(mat.transmittance) * (1.0F - mat.dissolve)};
                    const ::glm::vec3 &emission {::MobileRT::normalize(::MobileRT::toVec3(mat.emission))};
                    const auto indexRefraction {mat.ior};

                    const auto hasTexture {!mat.diffuse_texname.empty()};
                    const auto hasCoordTex {!this->attrib_.texcoords.empty()};
                    Texture texture {};
                    auto texCoord {::std::make_tuple(::glm::vec2 {-1}, ::glm::vec2 {-1}, ::glm::vec2 {-1})};
                    if (hasTexture && hasCoordTex) {
                        const auto itTexCoords1 {
                            this->attrib_.texcoords.cbegin() +
                            2 * static_cast<::std::int32_t> (idx1.texcoord_index)
                        };
                        const auto tx1 {*(itTexCoords1 + 0)};
                        const auto ty1 {*(itTexCoords1 + 1)};

                        const auto itTexCoords2 {
                            this->attrib_.texcoords.cbegin() +
                            2 * static_cast<::std::int32_t> (idx2.texcoord_index)
                        };
                        const auto tx2 {*(itTexCoords2 + 0)};
                        const auto ty2 {*(itTexCoords2 + 1)};

                        const auto itTexCoords3 {
                            this->attrib_.texcoords.cbegin() +
                            2 * static_cast<::std::int32_t> (idx3.texcoord_index)
                        };
                        const auto tx3 {*(itTexCoords3 + 0)};
                        const auto ty3 {*(itTexCoords3 + 1)};

                        texCoord = triple<::glm::vec2, ::glm::vec2, ::glm::vec2> {
                            ::glm::vec2 {tx1, ty1}, ::glm::vec2 {tx2, ty2}, ::glm::vec2 {tx3, ty3}
                        };

                        texture = ::getTextureFromCache(&texturesCache, filePath, mat.diffuse_texname);
                        texCoord = normalizeTexCoord(texture, texCoord);
                    }

                    Material material {diffuse, specular, transmittance, indexRefraction, emission, texture};
                    if (::MobileRT::hasPositiveValue(emission)) {
                        // If the primitive is a light source.
                        const auto &triangle {
                        Triangle::Builder(
                                ::std::get<0> (vertices), ::std::get<1> (vertices), ::std::get<2> (vertices)
                            )
                            .withNormals(
                                ::std::get<0>(normal),
                                ::std::get<1>(normal),
                                ::std::get<2>(normal))
                            .withTexCoords(
                                ::std::get<0>(texCoord),
                                ::std::get<1>(texCoord),
                                ::std::get<2>(texCoord))
                            .build()
                        };
                        scene->lights_.emplace_back(AreaLight {material, lambda(), triangle});
                    } else {
                        // If it is a primitive.
                        Triangle::Builder builder {
                            Triangle::Builder(
                                ::std::get<0> (vertices), ::std::get<1> (vertices), ::std::get<2> (vertices)
                            )
                                .withNormals(
                                    ::std::get<0>(normal),
                                    ::std::get<1>(normal),
                                    ::std::get<2>(normal))
                                .withTexCoords(
                                    ::std::get<0>(texCoord),
                                    ::std::get<1>(texCoord),
                                    ::std::get<2>(texCoord))
                        };

                        const auto itFoundMat {::std::find(scene->materials_.begin(), scene->materials_.end(), material)};
                        if (itFoundMat != scene->materials_.cend()) {
                            // If the material is already in the scene.
                            const auto materialIndex {static_cast<::std::int32_t> (
                                                          itFoundMat - scene->materials_.cbegin()
                                                      )};
                            scene->triangles_.emplace_back(builder.withMaterialIndex(materialIndex).build());
                        } else {
                            // If the scene doesn't have the material yet.
                            const auto materialIndex {static_cast<::std::int32_t> (scene->materials_.size())};
                            scene->triangles_.emplace_back(builder.withMaterialIndex(materialIndex).build());
                            scene->materials_.emplace_back(::std::move(material));
                        }
                    }
                } else {
                    // If it doesn't contain material.
                    const auto itColor {this->attrib_.colors.cbegin() + 3 * idx1.vertex_index};
                    const auto red {*(itColor + 0)};
                    const auto green {*(itColor + 1)};
                    const auto blue {*(itColor + 2)};

                    const ::glm::vec3 &diffuse {red, green, blue};
                    const ::glm::vec3 &specular {0.0F, 0.0F, 0.0F};
                    const ::glm::vec3 &transmittance {0.0F, 0.0F, 0.0F};
                    const auto indexRefraction {1.0F};
                    const ::glm::vec3 &emission {0.0F, 0.0F, 0.0F};
                    Material material {diffuse, specular, transmittance, indexRefraction, emission};
                    const auto itFoundMat {::std::find(scene->materials_.begin(), scene->materials_.end(), material)};
                    Triangle::Builder builder {
                        Triangle::Builder(
                            ::std::get<0> (vertices), ::std::get<1> (vertices), ::std::get<2> (vertices)
                        )
                            .withNormals(
                                ::std::get<0>(normal),
                                ::std::get<1>(normal),
                                ::std::get<2>(normal))
                    };
                    // If the scene has already the material.
                    if (itFoundMat != scene->materials_.cend()) {
                        const auto materialIndex {static_cast<::std::int32_t> (
                                                      itFoundMat - scene->materials_.begin()
                                                  )};
                        const auto &triangle {builder.withMaterialIndex(materialIndex).build()};
                        scene->triangles_.emplace_back(triangle);
                    } else {
                        // If the scene doesn't have material yet.
                        const auto materialIndex {static_cast<::std::int32_t> (scene->materials_.size())};
                        const auto &triangle {builder.withMaterialIndex(materialIndex).build()};
                        scene->triangles_.emplace_back(triangle);
                        scene->materials_.emplace_back(::std::move(material));
                    }
                }
            }// Loop over vertices in the face.
            indexOffset += faceVertices;

            if (scene->triangles_.size() > 0 && scene->triangles_.size() % 10000 == 0) {
                const auto& triangle {scene->triangles_.back()};
                LOG_DEBUG("Triangle ", scene->triangles_.size(), " position at ", triangle);
                MobileRT::printFreeMemory();
            } else if (scene->lights_.size() > 0 && scene->lights_.size() % 1000 == 0) {
                const auto& lightPos {::boost::get<::Components::AreaLight>(scene->lights_.back()).getPosition()};
                LOG_DEBUG("Light ", scene->lights_.size(), " position at: x: ", lightPos[0], ", y: ", lightPos[1], ", z: ", lightPos[2]);
                MobileRT::printFreeMemory();
            }
        }// The number of vertices per face.
    }// Loop over shapes.

    ::MobileRT::checkSystemError("Filled Scene");

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

    const auto idx1 {*(itIdx + 0)};
    const auto idx2 {*(itIdx + 1)};
    const auto idx3 {*(itIdx + 2)};

    const auto itVertex1 {this->attrib_.vertices.cbegin() + 3 * idx1.vertex_index};
    const auto vx1 {*(itVertex1 + 0)};
    const auto vy1 {*(itVertex1 + 1)};
    const auto vz1 {*(itVertex1 + 2)};
    const auto itVertex2 {this->attrib_.vertices.cbegin() + 3 * idx2.vertex_index};
    const auto vx2 {*(itVertex2 + 0)};
    const auto vy2 {*(itVertex2 + 1)};
    const auto vz2 {*(itVertex2 + 2)};
    const auto itVertex3 {this->attrib_.vertices.cbegin() + 3 * idx3.vertex_index};
    const auto vx3 {*(itVertex3 + 0)};
    const auto vy3 {*(itVertex3 + 1)};
    const auto vz3 {*(itVertex3 + 2)};

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
    const auto idx1 {*(itIdx + 0)};
    const auto idx2 {*(itIdx + 1)};
    const auto idx3 {*(itIdx + 2)};

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
        const auto AB {::std::get<1>(vertex) - ::std::get<0>(vertex)};
        const auto AC {::std::get<2>(vertex) - ::std::get<0>(vertex)};

        const auto normalDir {::glm::normalize(::glm::cross(AC, AB))};
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
        return triple<::glm::vec2, ::glm::vec2, ::glm::vec2> {
            ::glm::vec2 {-1}, ::glm::vec2 {-1}, ::glm::vec2 {-1}
        };
    } else {
        // Normalize the texture coordinates to be between [0, 1]
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
    ::std::map<::std::string, Texture> *const texturesCache,
    ::std::string &&textureBinary,
    const long size,
    const ::std::string &texPath
) {
    const auto itTexture {texturesCache->find(texPath)};

    if (itTexture == texturesCache->cend()) {// If the texture is not in the cache.
        LOG_DEBUG("Loading texture: ", texPath);
        auto &&texture {Texture::createTexture(::std::move(textureBinary), size)};
        auto &&pair {::std::make_pair(texPath, ::std::move(texture))};
        const auto res {::std::get<0> (texturesCache->emplace(::std::move(pair)))};// Add it to the cache.
        LOG_DEBUG("Texture loaded: ", texPath, ", is valid: ", res->second.isValid()? "true" : "false");
        return res->second;
    }

    return texturesCache->find(texPath)->second;// Get texture from cache.
}

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
