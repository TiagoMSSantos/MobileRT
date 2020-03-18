#include "MobileRT/Light.hpp"

using ::MobileRT::Light;

/**
 * The constructor.
 *
 * @param radiance The material of the light.
 */
Light::Light(Material radiance) :
    radiance_ {::std::move(radiance)} {
}

/**
 * The destructor.
 */
Light::~Light() {
}
