#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Utils.hpp"

using ::MobileRT::AABB;
using ::MobileRT::Ray;

AABB::AABB(const ::glm::vec3 &pointMin, const ::glm::vec3 &pointMax) noexcept :
        pointMin_{pointMin},
        pointMax_{pointMax} {
}

bool AABB::intersect(const Ray &ray) const noexcept {
    const float invDirX {1.0F / ray.direction_[0]};
    const float rayOrgX {ray.origin_[0]};
    const float t1X {(this->pointMin_[0] - rayOrgX) * invDirX};
    const float t2X {(this->pointMax_[0] - rayOrgX) * invDirX};
    float tmin {::std::min(t1X, t2X)};
    float tmax {::std::max(t1X, t2X)};

    for (auto axis {1}; axis < 3; ++axis) {
        const float invDir {1.0F / ray.direction_[axis]};
        const float rayOrg {ray.origin_[axis]};
        const float t1 {(this->pointMin_[axis] - rayOrg) * invDir};
        const float t2 {(this->pointMax_[axis] - rayOrg) * invDir};

        tmin = ::std::max(tmin, ::std::min(t1, t2));
        tmax = ::std::min(tmax, ::std::max(t1, t2));
    }

    const bool intersected {tmax >= ::std::max(tmin, 0.0F)};
    return intersected;
}

float AABB::getSurfaceArea() const noexcept {
    const auto length {this->pointMax_ - this->pointMin_};

    const float bottomTopArea {2 * length[0] * length[2]};
    const float sideAreaXY {2 * length[0] * length[1]};
    const float sideAreaZY {2 * length[2] * length[1]};

    const float surfaceArea {bottomTopArea + sideAreaXY + sideAreaZY};

    return surfaceArea;
}

::glm::vec3 AABB::getMidPoint() const noexcept {
    const auto &length {this->pointMax_ - this->pointMin_};
    const auto &res {this->pointMin_ + length / 2.0f};
    return res;
}

namespace MobileRT {
    AABB surroundingBox(const AABB &box1, const AABB &box2) noexcept {
        const ::glm::vec3 &min {::glm::min(box1.pointMin_, box2.pointMin_)};
        const ::glm::vec3 &max {::glm::max(box1.pointMax_, box2.pointMax_)};
        const AABB &res {min, max};

        return res;
    }
}//namespace MobileRT
