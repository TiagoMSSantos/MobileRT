//
// Created by Tiago on 11-03-2018.
//

#ifndef MOBILERT_RAYPACKET_HPP
#define MOBILERT_RAYPACKET_HPP

#include "MobileRT/Utils.hpp"
#include <array>
#include <glm/glm.hpp>

namespace MobileRT {
    class RayPacket final {
        const static ::std::uint32_t RayPacketSize{4};

    public:
        const ::std::array<::glm::vec3, RayPacketSize> origins_ {};
        const ::std::array<::glm::vec3, RayPacketSize> directions_  {};
        const ::std::int32_t depth_ {};
        const ::std::int32_t id_ {};
        const void *const primitive_ {};

    public:
        explicit RayPacket () noexcept = delete;

        explicit RayPacket(
            ::std::array<::glm::vec3, RayPacketSize> directions,
            ::std::array<::glm::vec3, RayPacketSize> origins,
            ::std::int32_t depth, const void *primitive = nullptr) noexcept;

        RayPacket(const RayPacket &rayPacket) noexcept = default;

        RayPacket(RayPacket &&rayPacket) noexcept = default;

        ~RayPacket() noexcept = default;

        RayPacket &operator=(const RayPacket &rayPacket) noexcept = delete;

        RayPacket &operator=(RayPacket &&rayPacket) noexcept = delete;
    };
}//namespace MobileRT

#endif //MOBILERT_RAY_HPP
