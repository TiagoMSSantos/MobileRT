#include "MobileRT/Ray.hpp"
#include <atomic>

using ::MobileRT::Ray;

namespace {
    ::std::int32_t getID() {
        static ::std::atomic<::std::int32_t> id{};
        const ::std::int32_t current{id.fetch_add(1, ::std::memory_order_relaxed)};
        return current;
    }
}//namespace

Ray::Ray(const ::glm::vec3 &dir, const ::glm::vec3 &origin,
         const ::std::int32_t depth, const void *const primitive) noexcept :
        origin_{origin},
        direction_{dir},
        depth_{depth},
        id_{getID()},
        primitive_{primitive} {
}
