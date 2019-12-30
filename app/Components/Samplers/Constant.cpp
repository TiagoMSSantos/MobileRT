#include "Components/Samplers/Constant.hpp"

using ::Components::Constant;

Constant::Constant(const float value) :
    value_ {value} {
}

float Constant::getSample(const ::std::uint32_t /*sample*/) {
    return this->value_;
}
