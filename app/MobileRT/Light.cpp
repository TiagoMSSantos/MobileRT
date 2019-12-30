#include "MobileRT/Light.hpp"

using ::MobileRT::Light;

Light::Light(Material radiance) :
    radiance_ {::std::move(radiance)} {
}

Light::~Light() {
    LOG("LIGHT DESTROYED!!!");
}
