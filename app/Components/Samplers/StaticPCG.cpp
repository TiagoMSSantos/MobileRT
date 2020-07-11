#include "Components/Samplers/StaticPCG.hpp"
#include <array>
#include <pcg_random.hpp>

using ::Components::StaticPCG;

namespace {
    ::std::array<float, ::MobileRT::ArraySize> values {};
}//namespace

StaticPCG::StaticPCG() {
    ::MobileRT::fillArrayWithPCG(&values);
}

float StaticPCG::getSample(const ::std::uint32_t /*sample*/) {
    return Sampler::getSampleFromArray(values);
}
