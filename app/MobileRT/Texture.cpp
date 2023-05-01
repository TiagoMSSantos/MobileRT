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
    const auto u {static_cast<::std::int32_t> (texCoords[0] * this->width_)};
    const auto v {static_cast<::std::int32_t> (texCoords[1] * this->height_)};
    const auto index
        {static_cast<::std::uint32_t> (v * this->width_ * this->channels_ + u * this->channels_)};

    const ::glm::vec3 vec {
        static_cast<float> (this->image_[index + 0]) / 255.0F,
        static_cast<float> (this->image_[index + 1]) / 255.0F,
        static_cast<float> (this->image_[index + 2]) / 255.0F
    };
    return vec;
}

/**
 * A factory which loads a texture file and creates a new Texture.
 *
 * @param textureFilePath The path to the texture file.
 * @return A new texture.
 */
Texture Texture::createTexture(const char *const textureFilePath) {
    ::std::int32_t width {};
    ::std::int32_t height {};
    ::std::int32_t channels {};
    const auto info {stbi_info(textureFilePath, &width, &height, &channels)};
    ::std::uint8_t *data {stbi_load(textureFilePath, &width, &height, &channels, 0)};
    LOG_DEBUG("new Texture: ", width, "x", height, ", c: ", channels, ", info: ", info, ", file:",
              textureFilePath);
    if (data == nullptr) {
        const auto &error {stbi_failure_reason()};
        LOG_ERROR("Error reading texture: ", error);
    }
    ::std::shared_ptr<::std::uint8_t> pointer {data, [](::std::uint8_t *const internalData) {
        stbi_image_free(internalData);
        LOG_DEBUG("Deleted texture");
    }};
    Texture texture {pointer, width, height, channels};
    return texture;
}

/**
 * The operator equals.
 *
 * @param texture A texture.
 * @return Whether both textures are equal.
 */
bool Texture::operator==(const Texture &texture) const {
    const auto sameWidth {this->width_ == texture.width_};
    const auto sameHeight {this->height_ == texture.height_};
    const auto sameChannels {this->channels_ == texture.channels_};
    const auto samePointer {this->image_ == texture.image_};
    const auto same {sameWidth && sameHeight && sameChannels && samePointer};
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
