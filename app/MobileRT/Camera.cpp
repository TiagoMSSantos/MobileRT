#include "MobileRT/Camera.hpp"
#include <array>
#include <glm/gtc/constants.hpp>

using ::MobileRT::AABB;
using ::MobileRT::Camera;
using ::MobileRT::NumberOfBlocks;

namespace {
    ::std::array<float, NumberOfBlocks> VALUES;

    bool FillThings() {
        for (auto it {VALUES.begin()}; it < VALUES.end(); std::advance(it, 1)) {
            const ::std::uint32_t index {static_cast<uint32_t>(::std::distance(VALUES.begin(), it))};
            *it = ::MobileRT::haltonSequence(index, 2);
        }
        static ::std::random_device randomDevice {"/dev/urandom"};
        static ::std::mt19937 generator {randomDevice()};
        ::std::shuffle(VALUES.begin(), VALUES.end(), generator);
        return true;
    }
}//namespace

float Camera::getBlock(const ::std::uint32_t sample) noexcept {
    const ::std::uint32_t current{
            this->block_.fetch_add(1, ::std::memory_order_relaxed) - NumberOfBlocks * sample};
    if (current >= NumberOfBlocks) {
        this->block_.fetch_sub(1, ::std::memory_order_relaxed);
        return 1.0f;
    }
    const auto it {VALUES.begin() + current};
    return *it;
}

AABB Camera::getAABB() const noexcept {
    const ::glm::vec3 &min {position_};
    const ::glm::vec3 &max {position_};
    const AABB res {min, max};
    return res;
}

//Left hand rule
Camera::Camera(const ::glm::vec3 &position, const ::glm::vec3 &lookAt, const ::glm::vec3 &up) noexcept :
        position_{position},
        direction_{::glm::normalize(lookAt - position)},
        right_{::glm::cross(up, direction_)},
        up_{::glm::cross(direction_,right_)} {
    static bool unused{FillThings()};
    static_cast<void> (unused);
}

Camera::Camera(const Camera &camera) noexcept {
    this->block_.store(camera.block_);
    this->position_ = camera.position_;
    this->direction_ = camera.direction_;
    this->right_ = camera.right_;
    this->up_ = camera.up_;
}

Camera::~Camera() noexcept {
    LOG("CAMERA DELETED");
}

void Camera::resetSampling() noexcept {
    this->block_ = 0;
}

float Camera::degToRad(const float deg) const noexcept {
    const float radians{(deg * ::glm::pi<float>()) / 180.0f};
    return radians;
}

float Camera::radToDeg(const float rad) const noexcept {
    const float degrees{(rad / ::glm::pi<float>()) * 180.0f};
    return degrees;
}
