#include "MobileRT/RayPacket.hpp"
#include <atomic>

using ::MobileRT::RayPacket;

namespace {
    ::std::int32_t getID() {
        static ::std::atomic<::std::int32_t> id{0};
        const ::std::int32_t current{id.fetch_add(1, ::std::memory_order_relaxed)};
        return current;
    }
}//namespace

RayPacket::RayPacket(
    const ::std::array<::glm::vec3, RayPacketSize> directions,
    const ::std::array<::glm::vec3, RayPacketSize> origins,
    ::std::int32_t depth, const void *primitive) noexcept :
        origins_{origins},
        directions_{directions},
        depth_{depth},
        id_{getID()},
        primitive_{primitive} {
}
