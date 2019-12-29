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
            explicit Texture() noexcept = default;

            explicit Texture(
                ::std::vector<::std::uint8_t> image,
                ::std::int32_t width,
                ::std::int32_t height,
                ::std::int32_t channels
            ) noexcept;

            Texture(const Texture &texture) = default;

            Texture(Texture &&texture) noexcept = default;

            ~Texture() noexcept = default;

            Texture &operator=(const Texture &texture) = default;

            Texture &operator=(Texture &&texture) noexcept = default;

            ::glm::vec3 loadColor(const ::glm::vec2 &texCoords) const noexcept;

            bool operator==(const Texture &texture) noexcept;

            static Texture createTexture(const char *textureFilePath) noexcept;
    };
}//namespace MobileRT

#endif //MOBILERT_TEXTURE_HPP
