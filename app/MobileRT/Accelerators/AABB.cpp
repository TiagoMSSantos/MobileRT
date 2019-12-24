#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Utils.hpp"

using ::MobileRT::AABB;
using ::MobileRT::Ray;

AABB::AABB(const ::glm::vec3 &pointMin, const ::glm::vec3 &pointMax) noexcept :
        pointMin_{pointMin}, pointMax_{pointMax} {
}

bool ::MobileRT::intersect(const AABB &box, const Ray &ray) noexcept {
    const float invDirX {1.0F / ray.direction_[0]};
    const float rayOrgX {ray.origin_[0]};
    const float t1X {(box.pointMin_[0] - rayOrgX) * invDirX};
    const float t2X {(box.pointMax_[0] - rayOrgX) * invDirX};
    float tmin {::std::min(t1X, t2X)};
    float tmax {::std::max(t1X, t2X)};

    for (::std::int32_t axis {1}; axis < 3; ++axis) {
        const float invDir {1.0F / ray.direction_[axis]};
        const float rayOrg {ray.origin_[axis]};
        const float t1 {(box.pointMin_[axis] - rayOrg) * invDir};
        const float t2 {(box.pointMax_[axis] - rayOrg) * invDir};

        tmin = ::std::max(tmin, ::std::min(t1, t2));
        tmax = ::std::min(tmax, ::std::max(t1, t2));
    }

    const bool intersected {tmax >= ::std::max(tmin, 0.0F)};
    return intersected;
}

float AABB::getSurfaceArea() const noexcept {
    const float lengthX {this->pointMax_[0] - this->pointMin_[0]};
    const float lengthY {this->pointMax_[1] - this->pointMin_[1]};
    const float lengthZ {this->pointMax_[2] - this->pointMin_[2]};

    const float bottomTopArea {2 * lengthX * lengthZ};
    const float sideAreaXY {2 * lengthX * lengthY};
    const float sideAreaZY {2 * lengthZ * lengthY};

    const float surfaceArea {bottomTopArea + sideAreaXY + sideAreaZY};

    return surfaceArea;
}

::glm::vec3 AABB::getMidPoint() const noexcept {
    const ::glm::vec3 length {this->pointMax_ - this->pointMin_};

    return this->pointMin_ + length / 2.0f;
}

namespace MobileRT {
    AABB surroundingBox(const AABB &box1, const AABB &box2) noexcept {
        const ::glm::vec3 &min {
                ::glm::min(box1.pointMin_, box2.pointMin_)};
        const ::glm::vec3 &max {
                ::glm::max(box1.pointMax_, box2.pointMax_)};
        const AABB res {min, max};

        return res;
    }
}//namespace MobileRT
