#include "MobileRT/Light.hpp"

using ::MobileRT::Light;
using ::MobileRT::Material;

/**
 * The constructor.
 *
 * @param radiance The material of the light.
 */
Light::Light(Material radiance) :
    radiance_ {::std::move(radiance)} {
}

Light::~Light() {

}
