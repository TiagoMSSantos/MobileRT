//
// Created by Tiago on 16-10-2016.
//

#include "MobileRT/Intersection.hpp"

using ::MobileRT::Intersection;

Intersection::Intersection(const float dist, const void *const primitive) noexcept :
        length_ {dist},
        primitive_{primitive} {
}

Intersection::Intersection(
        const ::glm::vec3 &intPoint,
        const float dist,
        const ::glm::vec3 &normal,
        const void *const primitive) noexcept :
        point_(intPoint),
        normal_(normal),
        length_(dist),
        primitive_(primitive) {
}
