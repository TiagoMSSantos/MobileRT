#include "MobileRT/Texture.hpp"
#include "Utils.hpp"

#define STB_IMAGE_IMPLEMENTATION
#include <stb_image.h>

using ::MobileRT::Texture;

Texture::Texture(
    ::std::uint8_t *data,
    ::std::int32_t width,
    ::std::int32_t height,
    ::std::int32_t channels
) :
    image_ {data},
    width_ {width},
    height_ {height},
    channels_ {channels} {
}

Texture::Texture(
        ::std::shared_ptr<::std::uint8_t> pointer,
        ::std::int32_t width,
        ::std::int32_t height,
        ::std::int32_t channels
) :
        pointer_ {pointer},
        image_ {pointer_.get()},
        width_ {width},
        height_ {height},
        channels_ {channels} {
}

::glm::vec3 Texture::loadColor(const ::glm::vec2 &texCoords) const {
    const auto u {static_cast<::std::int32_t> (texCoords[0] * this->width_)};
    const auto v {static_cast<::std::int32_t> (texCoords[1] * this->height_)};
    const auto index {static_cast<::std::uint32_t> (v * this->width_* this->channels_ + u * this->channels_)};

    const ::glm::vec3 vec {
        this->image_[index + 0] / 255.0F,
        this->image_[index + 1] / 255.0F,
        this->image_[index + 2] / 255.0F
    };
    return vec;
}

Texture Texture::createTexture(const char *const textureFilePath) {
    ::std::int32_t width {};
    ::std::int32_t height {};
    ::std::int32_t channels {};
    const auto info {stbi_info(textureFilePath, &width, &height, &channels)};
    ::std::uint8_t *data {stbi_load(textureFilePath, &width, &height, &channels, 0)};
    LOG("new Texture: ", width, "x", height, ", c: ", channels, ", info: ", info , ", file:", textureFilePath);
    if (data == nullptr) {
        const auto &error {stbi_failure_reason()};
        LOG("Error reading texture: ", error);
    }
    ::std::shared_ptr<::std::uint8_t> pointer {data, [] (::std::uint8_t *const internalData) {
        stbi_image_free(internalData);
        LOG("Deleted texture");
    }};
    Texture texture {pointer, width, height, channels};
    return texture;
}

bool Texture::operator==(const Texture &texture) const {
    const auto sameWidth {this->width_ == texture.width_};
    const auto sameHeight {this->height_ == texture.height_};
    const auto sameChannels {this->channels_ == texture.channels_};
    const auto samePointer {this->image_ == texture.image_};
    const auto same {sameWidth & sameHeight & sameChannels & samePointer};
    return same;
}

bool Texture::isValid() const {
    return this->width_ > 0 && this->height_> 0 && this->channels_ > 0 && this->image_;
}
