#ifndef MOBILERT_TEXTURE_HPP
#define MOBILERT_TEXTURE_HPP

#include <glm/glm.hpp>
#include <memory>
#include <vector>

namespace MobileRT {
    /**
     * A texture of a material.
     * <br>
     * A texture is an image where each cell in the image represents the
     * reflection of light in the object on an intersection point.
     */
    class Texture {
    private:
        ::std::shared_ptr<::std::uint8_t> pointer_ {};
        ::std::uint8_t *image_ {};
        ::std::int32_t width_ {};
        ::std::int32_t height_ {};
        ::std::int32_t channels_ {};

    public:
        explicit Texture() = default;

        explicit Texture(
            ::std::shared_ptr<::std::uint8_t> pointer,
            ::std::int32_t width,
            ::std::int32_t height,
            ::std::int32_t channels
        );

        Texture(const Texture &texture) = default;

        Texture(Texture &&texture) noexcept = default;

        ~Texture() = default;

        Texture &operator=(const Texture &texture) = default;

        Texture &operator=(Texture &&texture) noexcept = default;

        ::glm::vec3 loadColor(const ::glm::vec2 &texCoords) const;

        bool isValid() const;

        bool operator==(const Texture &texture) const;

        static Texture createTexture(const char *textureFilePath);
    };
}//namespace MobileRT

#endif //MOBILERT_TEXTURE_HPP
