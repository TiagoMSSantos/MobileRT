//
// Created by puscas on 07-04-2017.
//

#include "Components/Samplers/Constant.hpp"

using ::Components::Constant;

Constant::Constant(const float value) noexcept :
        value_(value) {
}

float Constant::getSample(const ::std::uint32_t /*sample*/) noexcept {
    return value_;
}
