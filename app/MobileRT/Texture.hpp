#ifndef MOBILERT_TEXTURE_HPP
#define MOBILERT_TEXTURE_HPP

#include <glm/glm.hpp>
#include <vector>

namespace MobileRT {
    class Texture {
        private:
            ::std::vector<::std::uint8_t> image_ {};
            ::std::int32_t width_ {};
            ::std::int32_t height_ {};
            ::std::int32_t channels_ {};

        public:
            explicit Texture() = default;

            explicit Texture(
                ::std::vector<::std::uint8_t> image,
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
