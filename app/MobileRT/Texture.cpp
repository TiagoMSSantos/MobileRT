#include "MobileRT/Texture.hpp"
#include "MobileRT/Utils/Utils.hpp"

#define STB_IMAGE_IMPLEMENTATION

#include <stb_image.h>

using ::MobileRT::Texture;

/**
 * The constructor.
 *
 * @param pointer  A shared_ptr to the texture data.
 * @param width    The width of the texture.
 * @param height   The height of the texture.
 * @param channels The number of channels in the texture.
 */
Texture::Texture(
    ::std::shared_ptr<::std::uint8_t> pointer,
    ::std::int32_t width,
    ::std::int32_t height,
    ::std::int32_t channels
) :
    pointer_ {::std::move(pointer)},
    image_ {pointer_.get()},
    width_ {width},
    height_ {height},
    channels_ {channels} {
}

/**
 * Gets the color of a point in the texture.
 *
 * @param texCoords The texture coordinates.
 * @return The color of the point.
 */
::glm::vec3 Texture::loadColor(const ::glm::vec2 &texCoords) const {
    const ::std::int32_t u {static_cast<::std::int32_t> (texCoords[0] * this->width_)};
    const ::std::int32_t v {static_cast<::std::int32_t> (texCoords[1] * this->height_)};
    const ::std::uint32_t index {static_cast<::std::uint32_t> (v * this->width_ * this->channels_ + u * this->channels_)};

    const ::glm::vec3 vec {
        static_cast<float> (this->image_[index + 0]) / 255.0F,
        static_cast<float> (this->image_[index + 1]) / 255.0F,
        static_cast<float> (this->image_[index + 2]) / 255.0F
    };
    return vec;
}

/**
 * A factory which loads a texture from memory in binary format and creates a new Texture.
 *
 * @param textureBinary The texture loaded in memory.
 * @param size          The size of the texture in bytes.
 * @return A new texture.
 */
Texture Texture::createTexture(::std::string &&textureBinary, const long size) {
    ::std::int32_t width {};
    ::std::int32_t height {};
    ::std::int32_t channels {};
    const int info {stbi_info_from_memory(reinterpret_cast<unsigned char const *> (textureBinary.c_str()), static_cast<int> (size), &width, &height, &channels)};
    if (info <= 0 || size <= 0) {
        const char *error {stbi_failure_reason()};
        LOG_ERROR("Error reading texture: ", error);
        throw ::std::runtime_error {error};
    }
    ::std::uint8_t *const data {stbi_load_from_memory(reinterpret_cast<unsigned char const *> (textureBinary.c_str()), static_cast<int> (size), &width, &height, &channels, 0)};
    if (data == nullptr || width <= 0 || height <= 0 || channels <= 0) {
        const char *error {stbi_failure_reason()};
        LOG_ERROR("Error reading texture: ", error);
        throw ::std::runtime_error {error};
    }
    ::std::shared_ptr<::std::uint8_t> pointer {data, [](::std::uint8_t *const internalData) {
        stbi_image_free(internalData);
        LOG_DEBUG("Deleted texture");
    }};
    Texture texture {pointer, width, height, channels};
    ::MobileRT::checkSystemError("Created Texture.");
    return texture;
}

/**
 * A factory which loads a texture file and creates a new Texture.
 *
 * @param texturePath The path to the texture file.
 * @return A new texture.
 */
Texture Texture::createTexture(const ::std::string &texturePath) {
    ::std::int32_t width {};
    ::std::int32_t height {};
    ::std::int32_t channels {};
    ::MobileRT::checkSystemError(("Loading Texture from: " + texturePath).c_str());
    const int info {stbi_info(texturePath.c_str(), &width, &height, &channels)};
    if (info <= 0) {
        const char *error {stbi_failure_reason()};
        LOG_ERROR(("Error reading texture '" + texturePath + "': " + error).c_str());
        throw ::std::runtime_error {("Error reading texture '" + texturePath + "': " + error)};
    }
    ::std::uint8_t *const data {stbi_load(texturePath.c_str(), &width, &height, &channels, 0)};
    if (data == nullptr || width <= 0 || height <= 0 || channels <= 0) {
        const char *error {stbi_failure_reason()};
        LOG_ERROR("Error reading texture: ", error);
        throw ::std::runtime_error {error};
    }
    ::std::shared_ptr<::std::uint8_t> pointer {data, [](::std::uint8_t *const internalData) {
        stbi_image_free(internalData);
    }};
    Texture texture {pointer, width, height, channels};
    ::MobileRT::checkSystemError(("Created Texture: " + texturePath).c_str());
    return texture;
}

/**
 * The operator equals.
 *
 * @param texture A texture.
 * @return Whether both textures are equal.
 */
bool Texture::operator==(const Texture &texture) const {
    const bool sameWidth {this->width_ == texture.width_};
    const bool sameHeight {this->height_ == texture.height_};
    const bool sameChannels {this->channels_ == texture.channels_};
    const bool samePointer {this->image_ == texture.image_};
    const bool same {sameWidth && sameHeight && sameChannels && samePointer};
    return same;
}

/**
 * Checks if the texture is valid or not.
 *
 * @return Whether the texture is a valid one or not.
 */
bool Texture::isValid() const {
    return this->width_ > 0 && this->height_ > 0 && this->channels_ > 0 && this->image_ != nullptr;
}
