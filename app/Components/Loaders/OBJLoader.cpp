#include "Components/Loaders/OBJLoader.hpp"
#include "Components/Lights/AreaLight.hpp"
#include "MobileRT/Texture.hpp"
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

template<typename T1, typename T2, typename T3>
using triple = ::std::tuple<T1, T2, T3>;

OBJLoader::OBJLoader(::std::string objFilePath, ::std::string matFilePath) :
    objFilePath_ {::std::move(objFilePath)} {

    MobileRT::checkSystemError("Error before read OBJ.");
    LOG("Will read OBJ path: ", this->objFilePath_);
    ::std::ifstream objStream {this->objFilePath_, ::std::ios::binary};
    MobileRT::checkSystemError(::std::string("Error after read OBJ `" + this->objFilePath_ + "`.\n").c_str());
    objStream.exceptions(
        objStream.exceptions() | ::std::ifstream::goodbit | ::std::ifstream::badbit |
        ::std::ifstream::failbit
    );
    LOG("Will read MAT path: ", matFilePath);
    ::std::ifstream matStream {matFilePath, ::std::ios::binary};
    MobileRT::checkSystemError(::std::string("Error after read MAT `" + matFilePath + "`.").c_str());
    matStream.exceptions(
        matStream.exceptions() | ::std::ifstream::goodbit | ::std::ifstream::badbit |
        ::std::ifstream::failbit
    );
    ::tinyobj::MaterialStreamReader matStreamReader {matStream};
    ::tinyobj::MaterialStreamReader
        *const matStreamReaderPtr {!matFilePath.empty() ? &matStreamReader : nullptr};
    ::std::string errors {};
    ::std::string warnings {};

    LOG("Going to call tinyobj::LoadObj");
    LOG("OBJ file path: ", this->objFilePath_);
    LOG("MTL file path: ", matFilePath);

    MobileRT::checkSystemError("Error before LoadObj.");
    const auto ret {
        ::tinyobj::LoadObj(
            &this->attrib_, &this->shapes_, &this->materials_,
            &warnings, &errors, &objStream, matStreamReaderPtr, true, true
        )
    };

    if (errno != 0) {
        ::std::perror("Error after LoadObj: ");
        LOG("errno after LoadObj (", errno, "): ", ::std::strerror(errno));

        // Necessary reset of `errno` because there is an error in Android when
        // allocating more than 20k bytes of memory (random values):
        // EINVAL (Invalid argument) - errno (22): Invalid argument
        errno = 0;// reset the error code
    }
    LOG("Called tinyobj::LoadObj");

    if (!errors.empty()) {
        LOG("Error: ", errors);
    }

    if (!warnings.empty()) {
        LOG("Warning: ", warnings);
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

bool OBJLoader::fillScene(Scene *const scene,
                          ::std::function<::std::unique_ptr<Sampler>()> lambda) {
    LOG("FILLING SCENE");
    scene->triangles_.reserve(static_cast<::std::uint32_t> (this->numberTriangles_));
    const ::std::string delimiter {"/"};
    const ::std::string
        filePath {this->objFilePath_.substr(0, this->objFilePath_.find_last_of(delimiter)) + "/"};
    ::std::map<::std::string, Texture> texturesCache {};

    // Loop over shapes.
    for (const auto &shape : this->shapes_) {
        // Loop over faces(polygon).
        ::std::uint32_t indexOffset {};
        // The number of vertices per face.
        for (::std::uint32_t face {}; face < shape.mesh.num_face_vertices.size(); ++face) {
            const auto
                it {shape.mesh.num_face_vertices.cbegin() + static_cast<::std::int32_t> (face)};
            const ::std::uint32_t faceVertices {*it};

            if (faceVertices % 3 != 0) {
                LOG("num_face_vertices [", face, "] = ", faceVertices);
                continue;
            }

            // Loop over vertices in the face.
            for (::std::uint32_t vertex {}; vertex < faceVertices; vertex += 3) {
                const auto itIdx {shape.mesh.indices.cbegin() +
                                  static_cast<::std::int32_t> (indexOffset + vertex)};

                const auto idx1 {*(itIdx + 0)};
                const auto itVertex1 {this->attrib_.vertices.cbegin() + 3 * idx1.vertex_index};
                const auto vx1 {*(itVertex1 + 0)};
                const auto vy1 {*(itVertex1 + 1)};
                const auto vz1 {*(itVertex1 + 2)};

                const auto idx2 {*(itIdx + 1)};
                const auto itVertex2 {this->attrib_.vertices.cbegin() + 3 * idx2.vertex_index};
                const auto vx2 {*(itVertex2 + 0)};
                const auto vy2 {*(itVertex2 + 1)};
                const auto vz2 {*(itVertex2 + 2)};

                const auto idx3 {*(itIdx + 2)};
                const auto itVertex3 {this->attrib_.vertices.cbegin() + 3 * idx3.vertex_index};
                const auto vx3 {*(itVertex3 + 0)};
                const auto vy3 {*(itVertex3 + 1)};
                const auto vz3 {*(itVertex3 + 2)};

                const ::glm::vec3 &vertex1 {-vx1, vy1, vz1};
                const ::glm::vec3 &vertex2 {-vx2, vy2, vz2};
                const ::glm::vec3 &vertex3 {-vx3, vy3, vz3};

                triple<::glm::vec3, ::glm::vec3, ::glm::vec3> normal
                    {::std::make_tuple(::glm::vec3 {-1}, ::glm::vec3 {-1}, ::glm::vec3 {-1})};

                // If it has normals.
                if (!this->attrib_.normals.empty()) {
                    const auto itNormal1 {this->attrib_.normals.cbegin() + 3 * idx1.normal_index};
                    const auto itNormal2 {this->attrib_.normals.cbegin() + 3 * idx2.normal_index};
                    const auto itNormal3 {this->attrib_.normals.cbegin() + 3 * idx3.normal_index};

                    normal = triple<::glm::vec3, ::glm::vec3, ::glm::vec3> {
                        ::glm::vec3 {-*(itNormal1 + 0), *(itNormal1 + 1), *(itNormal1 + 2)},
                        ::glm::vec3 {-*(itNormal2 + 0), *(itNormal2 + 1), *(itNormal2 + 2)},
                        ::glm::vec3 {-*(itNormal3 + 0), *(itNormal3 + 1), *(itNormal3 + 2)}
                    };
                } else {
                    // If it doesn't have normals.

                    const auto AB {vertex2 - vertex1};
                    const auto AC {vertex3 - vertex1};

                    const auto normalDir {::glm::normalize(::glm::cross(AC, AB))};
                    normal = triple<::glm::vec3, ::glm::vec3, ::glm::vec3> {
                        normalDir, normalDir, normalDir
                    };
                }

                // per-face material.
                const auto itMaterialShape
                    {shape.mesh.material_ids.cbegin() + static_cast<::std::int32_t> (face)};
                const auto materialId {*itMaterialShape};

                // If it contains material.
                if (materialId >= 0) {
                    const auto itMaterial
                        {this->materials_.cbegin() + static_cast<::std::int32_t> (materialId)};
                    const auto &mat {*itMaterial};
                    const ::glm::vec3 &diffuse {::MobileRT::toVec3(mat.diffuse)};
                    const ::glm::vec3 &specular {::MobileRT::toVec3(mat.specular)};
                    const ::glm::vec3 &transmittance
                        {::MobileRT::toVec3(mat.transmittance) * (1.0F - mat.dissolve)};
                    const ::glm::vec3
                        &emission {::MobileRT::normalize(::MobileRT::toVec3(mat.emission))};
                    const auto indexRefraction {mat.ior};

                    const auto hasTexture {!mat.diffuse_texname.empty()};
                    const auto hasCoordTex {!this->attrib_.texcoords.empty()};
                    Texture texture {};
                    triple<::glm::vec2, ::glm::vec2, ::glm::vec2> texCoord
                        {::std::make_tuple(::glm::vec3 {-1}, ::glm::vec3 {-1}, ::glm::vec3 {-1})};

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

                        const auto texPath {mat.diffuse_texname};
                        const auto itTexture {texturesCache.find(texPath)};

                        // If the texture is not in the cache.
                        if (itTexture == texturesCache.cend()) {
                            const auto texturePath {filePath + texPath};
                            texture = Texture::createTexture(texturePath.c_str());
                            auto &&pair {::std::make_pair(texPath, ::std::move(texture))};
                            texturesCache.emplace(::std::move(pair));
                        }
                        texture = texturesCache.find(texPath)->second;
                    }

                    // If the texture is not valid.
                    if (!texture.isValid()) {
                        texCoord = triple<::glm::vec2, ::glm::vec2, ::glm::vec2> {
                            ::glm::vec2 {-1}, ::glm::vec2 {-1}, ::glm::vec2 {-1}
                        };
                    } else {
                        texCoord = triple<::glm::vec2, ::glm::vec2, ::glm::vec2> {
                            ::MobileRT::normalize(::std::get<0>(texCoord)),
                            ::MobileRT::normalize(::std::get<1>(texCoord)),
                            ::MobileRT::normalize(::std::get<2>(texCoord))
                        };
                    }

                    Material material
                        {diffuse, specular, transmittance, indexRefraction, emission, texture};

                    // If the primitive is a light source.
                    if (::MobileRT::hasPositiveValue(emission)) {
                        const auto &triangle {
                            Triangle::Builder(vertex1, vertex2, vertex3)
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
                        scene->lights_.emplace_back(
                            ::MobileRT::std::make_unique<AreaLight>(material, lambda(), triangle));
                        const auto lightPos {scene->lights_.back()->getPosition()};
                        LOG("Light position at: x:", lightPos[0], ", y:", lightPos[1], ", z:",
                            lightPos[2]);
                    } else {
                        // If it is a primitive.

                        Triangle::Builder builder {
                            Triangle::Builder(vertex1, vertex2, vertex3)
                                .withNormals(
                                    ::std::get<0>(normal),
                                    ::std::get<1>(normal),
                                    ::std::get<2>(normal))
                                .withTexCoords(
                                    ::std::get<0>(texCoord),
                                    ::std::get<1>(texCoord),
                                    ::std::get<2>(texCoord))
                        };

                        const auto itFoundMat
                            {::std::find(scene->materials_.begin(), scene->materials_.end(),
                                         material)};

                        // If the material is already in the scene.
                        if (itFoundMat != scene->materials_.cend()) {
                            const auto materialIndex {static_cast<::std::int32_t> (
                                                          itFoundMat - scene->materials_.cbegin()
                                                      )};
                            const auto &triangle {builder.withMaterialIndex(materialIndex).build()};
                            scene->triangles_.emplace_back(triangle);
                        } else {
                            // If the scene doesn't have the material yet.
                            const auto materialIndex
                                {static_cast<::std::int32_t> (scene->materials_.size())};
                            const auto &triangle {builder.withMaterialIndex(materialIndex).build()};
                            scene->triangles_.emplace_back(triangle);
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
                    const auto itFoundMat
                        {::std::find(scene->materials_.begin(), scene->materials_.end(), material)};
                    Triangle::Builder builder {
                        Triangle::Builder(vertex1, vertex2, vertex3)
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
                        const auto
                            materialIndex {static_cast<::std::int32_t> (scene->materials_.size())};
                        const auto &triangle {builder.withMaterialIndex(materialIndex).build()};
                        scene->triangles_.emplace_back(triangle);
                        scene->materials_.emplace_back(::std::move(material));
                    }
                }
            }// Loop over vertices in the face.
            indexOffset += faceVertices;
        }// The number of vertices per face.
    }// Loop over shapes.

    return true;
}

OBJLoader::~OBJLoader() {
    this->objFilePath_.clear();
    this->attrib_.normals.clear();
    this->attrib_.texcoords.clear();
    this->attrib_.vertices.clear();
    this->shapes_.clear();
    this->materials_.clear();

    this->objFilePath_.erase();

    this->objFilePath_.shrink_to_fit();
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

    LOG("OBJLOADER DELETED");
}
