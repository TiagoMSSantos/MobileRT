#include "MobileRT/Light.hpp"

using ::MobileRT::Light;

Light::Light(const Material &radiance) noexcept :
        radiance_{radiance} {
}

Light::~Light() noexcept {
    LOG("LIGHT DELETED");
}
