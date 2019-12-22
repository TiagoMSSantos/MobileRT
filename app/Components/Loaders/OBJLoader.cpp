#include "Components/Loaders/OBJLoader.hpp"
#include "Components/Lights/AreaLight.hpp"
#include <cstring>
#include <fstream>
#include <tinyobjloader/tiny_obj_loader.h>

using ::Components::AreaLight;
using ::Components::OBJLoader;
using ::MobileRT::Material;
using ::MobileRT::Scene;

OBJLoader::OBJLoader(::std::string objFilePath, ::std::string matFilePath) noexcept :
        objFilePath_{::std::move(objFilePath)},
        mtlFilePath_{::std::move(matFilePath)} {
}

::std::int32_t OBJLoader::process() noexcept {
    ::std::ifstream objStream {this->objFilePath_};
    objStream.exceptions(::std::ifstream::goodbit | ::std::ifstream::badbit);
    ::std::ifstream matStream {this->mtlFilePath_};
    matStream.exceptions(::std::ifstream::goodbit | ::std::ifstream::badbit);
    ::tinyobj::MaterialStreamReader matStreamReader {matStream};
    ::tinyobj::MaterialStreamReader *const matStreamReaderPtr {!mtlFilePath_.empty()? &matStreamReader : nullptr};
    ::std::string errors {};
    ::std::string warnings {};
    errno = 0;

    LOG("Going to call tinyobj::LoadObj");
    const bool ret {
            ::tinyobj::LoadObj(&attrib_, &shapes_, &materials_, &warnings, &errors, &objStream,
                               matStreamReaderPtr, true)};
    LOG("Called tinyobj::LoadObj");

    if (!errors.empty()) {
        LOG("Error: ", errors);
    }

    if (!warnings.empty()) {
        LOG("Warning: ", warnings);
    }

    if (errno) {
        const ::std::string &strError {::std::strerror(errno)};
        LOG("Error (errno): ", strError);
    }

    if (ret) {
        this->isProcessed_ = true;
        this->numberTriangles_ = 0;
        for (const auto &shape : shapes_) {
            for (const auto numFaceVertice : shape.mesh.num_face_vertices) {
                const ::std::size_t triangles{static_cast<::std::size_t>(numFaceVertice / 3)};
                this->numberTriangles_ += triangles;
            }
        }
    } else {
        this->isProcessed_ = false;
        this->numberTriangles_ = -1;
    }

    return this->numberTriangles_;
}

bool OBJLoader::fillScene(Scene *const scene,
                          ::std::function<::std::unique_ptr<MobileRT::Sampler>()> lambda) noexcept {
    scene->triangles_.reserve(static_cast<::std::size_t> (numberTriangles_));

    for (const auto &shape : this->shapes_) {
        // Loop over faces(polygon)
        ::std::size_t indexOffset {};
        for (::std::size_t face {}; face < shape.mesh.num_face_vertices.size(); ++face) {
            const auto it {shape.mesh.num_face_vertices.begin() + static_cast<::std::int32_t> (face)};
            const ::std::size_t faceVertices {*it};

            if (faceVertices % 3 != 0) {
                LOG("num_face_vertices [", face, "] = ", faceVertices);
                continue;
            }

            // Loop over vertices in the face.
            for (::std::size_t vertex {}; vertex < faceVertices; vertex += 3) {
                const auto itIdx {shape.mesh.indices.begin() + static_cast<::std::int32_t> (indexOffset + vertex)};

                const ::tinyobj::index_t idx1 {*(itIdx + 0)};
                const auto itVertex1 {this->attrib_.vertices.begin() + 3 * idx1.vertex_index};
                const ::tinyobj::real_t vx1 {*(itVertex1 + 0)};
                const ::tinyobj::real_t vy1 {*(itVertex1 + 1)};
                const ::tinyobj::real_t vz1 {*(itVertex1 + 2)};

                const auto itColor {this->attrib_.colors.begin() + 3 * static_cast<::std::int32_t> (idx1.vertex_index)};
                const ::tinyobj::real_t red {*(itColor + 0)};
                const ::tinyobj::real_t green {*(itColor + 1)};
                const ::tinyobj::real_t blue {*(itColor + 2)};

                /*if (!attrib_.texcoords.empty()) {
                    const auto itTexCoords {
                        attrib_.texcoords.begin() + 2 * static_cast<::std::int32_t> (idx1.texcoord_index)
                    };
                    ::tinyobj::real_t tx {*(itTexCoords + 0)};
                    ::tinyobj::real_t ty {*(itTexCoords + 1)};
                    LOG(tx, ty);
                }*/

                const ::tinyobj::index_t idx2 {*(itIdx + 1)};
                const auto itVertex2 {this->attrib_.vertices.begin() + 3 * idx2.vertex_index};
                const ::tinyobj::real_t vx2 {*(itVertex2 + 0)};
                const ::tinyobj::real_t vy2 {*(itVertex2 + 1)};
                const ::tinyobj::real_t vz2 {*(itVertex2 + 2)};

                const ::tinyobj::index_t idx3 {*(itIdx + 2)};
                const auto itVertex3 {this->attrib_.vertices.begin() + 3 * idx3.vertex_index};
                const ::tinyobj::real_t vx3 {*(itVertex3 + 0)};
                const ::tinyobj::real_t vy3 {*(itVertex3 + 1)};
                const ::tinyobj::real_t vz3 {*(itVertex3 + 2)};

                const ::glm::vec3 &vertex1 {-vx1, vy1, vz1};
                const ::glm::vec3 &vertex2 {-vx2, vy2, vz2};
                const ::glm::vec3 &vertex3 {-vx3, vy3, vz3};
                const ::MobileRT::Triangle triangle {vertex1, vertex2, vertex3};

                // per-face material
                const auto itMaterialShape {shape.mesh.material_ids.begin() + static_cast<::std::int32_t> (face)};
                const ::std::int32_t materialId {*itMaterialShape};
                if (materialId >= 0) {
                    const auto itMaterial {this->materials_.begin() + static_cast<::std::int32_t> (materialId)};
                    const ::tinyobj::material_t &mat {*itMaterial};
                    const float d1 {mat.diffuse[0]};
                    const float d2 {mat.diffuse[1]};
                    const float d3 {mat.diffuse[2]};
                    const ::glm::vec3 &diffuse {d1, d2, d3};
                    const float s1 {mat.specular[0]};
                    const float s2 {mat.specular[1]};
                    const float s3 {mat.specular[2]};
                    const ::glm::vec3 &specular {s1 / 2.0f, s2 / 2.0f, s3 / 2.0f};
                    const float t1 {mat.transmittance[0] * (1.0f - mat.dissolve)};
                    const float t2 {mat.transmittance[1] * (1.0f - mat.dissolve)};
                    const float t3 {mat.transmittance[2] * (1.0f - mat.dissolve)};
                    const ::glm::vec3 &transmittance {t1, t2, t3};
                    float e1 {mat.emission[0]};
                    float e2 {mat.emission[1]};
                    float e3 {mat.emission[2]};
                    const float max {::std::max(::std::max(e1, e2), e3)};
                    if (max > 1.0f) {
                        e1 /= max;
                        e2 /= max;
                        e3 /= max;
                    }
                    const ::glm::vec3 &emission {e1, e2, e3};
                    const float indexRefraction {mat.ior};
                    const Material material {diffuse, specular, transmittance, indexRefraction, emission};
                    if (e1 > 0.0f || e2 > 0.0f || e3 > 0.0f) {
                        const ::glm::vec3 &p1 {vx1, vy1, vz1};
                        const ::glm::vec3 &p2 {vx2, vy2, vz2};
                        const ::glm::vec3 &p3 {vx3, vy3, vz3};
                        scene->lights_.emplace_back(::std::make_unique<AreaLight>(material, lambda(), p1, p2, p3));
                    } else {
                        scene->triangles_.emplace_back(triangle, material);
                    }
                } else {
                    const ::glm::vec3 &diffuse {red, green, blue};
                    const ::glm::vec3 &specular {0.0f, 0.0f, 0.0f};
                    const ::glm::vec3 &transmittance {0.0f, 0.0f, 0.0f};
                    const float indexRefraction {1.0f};
                    const ::glm::vec3 &emission {0.0f, 0.0f, 0.0f};
                    const Material material {diffuse, specular, transmittance, indexRefraction, emission};
                    scene->triangles_.emplace_back(triangle, material);
                }
            }
            indexOffset += faceVertices;
        }
    }

    return true;
}

OBJLoader::~OBJLoader() noexcept {
    this->objFilePath_.clear();
    this->mtlFilePath_.clear();
    this->attrib_.normals.clear();
    this->attrib_.texcoords.clear();
    this->attrib_.vertices.clear();
    this->shapes_.clear();
    this->materials_.clear();

    this->objFilePath_.shrink_to_fit();
    this->mtlFilePath_.shrink_to_fit();
    this->attrib_.normals.shrink_to_fit();
    this->attrib_.texcoords.shrink_to_fit();
    this->attrib_.vertices.shrink_to_fit();
    this->shapes_.shrink_to_fit();
    this->materials_.shrink_to_fit();

    ::std::vector<::tinyobj::shape_t>{}.swap(this->shapes_);
    ::std::vector<::tinyobj::material_t>{}.swap(this->materials_);

    LOG("OBJLOADER DELETED");
}
