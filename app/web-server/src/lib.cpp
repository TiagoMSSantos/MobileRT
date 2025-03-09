#include <cstdint>
#include <iostream>
#include "MobileRT/Config.hpp"

extern "C" {

    bool m_async {};
    ::MobileRT::Config m_config {};

    void say_hello() {
        std::cout << "Hello from C++!" << std::endl;
    }

    ::std::int32_t *get_bitmap() {
        std::cout << "Getting bitmap from C++!" << std::endl;
        return m_config.bitmap.data();
    }
}
