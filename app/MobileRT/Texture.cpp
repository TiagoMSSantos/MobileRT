#include "MobileRT/Texture.hpp"

#define STB_IMAGE_IMPLEMENTATION
#include <stb_image.h>

using ::MobileRT::Texture;

Texture::Texture(
    ::std::vector<::std::uint8_t> image,
    ::std::int32_t width,
    ::std::int32_t height,
    ::std::int32_t channels
) noexcept :
    image_ {image},
    width_ {width},
    height_ {height},
    channels_ {channels} {

}

::glm::vec3 Texture::loadColor(const ::glm::vec2 &texCoords) const noexcept {
    const auto u {static_cast<::std::int32_t> (texCoords[0] * this->width_)};
    const auto v {static_cast<::std::int32_t> (texCoords[1] * this->height_)};
    const auto index {v * this->width_* this->channels_ + u * this->channels_};

    const ::glm::vec3 vec {
        this->image_[index + 0] / 255.0F,
        this->image_[index + 1] / 255.0F,
        this->image_[index + 2] / 255.0F
    };
    return vec;
}

Texture Texture::createTexture(const char *const textureFilePath) noexcept {
    ::std::int32_t width {};
    ::std::int32_t height {};
    ::std::int32_t channels {};
    ::std::uint8_t *data {stbi_load(textureFilePath, &width, &height, &channels, 3)};
    ::std::vector<::std::uint8_t> image (data, data + (width * height * channels));
    Texture texture {image, width, height, channels};
    return texture;
}

bool Texture::operator==(const Texture &texture) noexcept {
    const auto sameWidth {this->width_ == texture.width_};
    const auto sameHeight {this->height_ == texture.height_};
    const auto sameChannels {this->channels_ == texture.channels_};
    const auto sameSize {this->image_.size() == texture.image_.size()};
    const auto same {sameWidth && sameHeight && sameChannels && sameSize};
    return same;
}
