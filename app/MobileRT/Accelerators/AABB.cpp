#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Utils.hpp"

using ::MobileRT::AABB;
using ::MobileRT::Ray;

AABB::AABB(const ::glm::vec3 &pointMin, const ::glm::vec3 &pointMax) noexcept :
    pointMin_ {pointMin},
    pointMax_ {pointMax} {
}

bool AABB::intersect(const Ray &ray) const noexcept {
    const auto invDirX {1.0F / ray.direction_[0]};
    const auto rayOrgX {ray.origin_[0]};
    const auto t1X {(this->pointMin_[0] - rayOrgX) * invDirX};
    const auto t2X {(this->pointMax_[0] - rayOrgX) * invDirX};
    auto tMin {::std::min(t1X, t2X)};
    auto tMax {::std::max(t1X, t2X)};

    for (auto axis {1}; axis < 3; ++axis) {
        const auto invDir {1.0F / ray.direction_[axis]};
        const auto rayOrg {ray.origin_[axis]};
        const auto t1 {(this->pointMin_[axis] - rayOrg) * invDir};
        const auto t2 {(this->pointMax_[axis] - rayOrg) * invDir};

        tMin = ::std::max(tMin, ::std::min(t1, t2));
        tMax = ::std::min(tMax, ::std::max(t1, t2));
    }

    const auto intersected {tMax >= ::std::max(tMin, 0.0F)};
    return intersected;
}

float AABB::getSurfaceArea() const noexcept {
    const auto length {this->pointMax_ - this->pointMin_};

    const auto bottomTopArea {2 * length[0] * length[2]};
    const auto sideAreaXY {2 * length[0] * length[1]};
    const auto sideAreaZY {2 * length[2] * length[1]};

    const auto surfaceArea {bottomTopArea + sideAreaXY + sideAreaZY};

    return surfaceArea;
}

::glm::vec3 AABB::getMidPoint() const noexcept {
    const auto &length {(this->pointMax_ - this->pointMin_) / 2.0F};
    const auto &res {this->pointMin_ + length};
    return res;
}

namespace MobileRT {
    AABB surroundingBox(const AABB &box1, const AABB &box2) noexcept {
        const auto &min {::glm::min(box1.pointMin_, box2.pointMin_)};
        const auto &max {::glm::max(box1.pointMax_, box2.pointMax_)};
        const AABB &res {min, max};

        return res;
    }
}//namespace MobileRT
