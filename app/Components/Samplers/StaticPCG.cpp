#include "Components/Samplers/StaticPCG.hpp"

#include "MobileRT/Utils/Utils.hpp"

#include <array>
#include <pcg_random.hpp>

using ::Components::StaticPCG;

namespace {
    ::std::array<float, ::MobileRT::ArraySize> randomSequence {};
}//namespace

StaticPCG::StaticPCG() {
    ::MobileRT::fillArrayWithPCG(&randomSequence);
}

float StaticPCG::getSample(const ::std::uint32_t /*sample*/) {
    return Sampler::getSampleFromArray(randomSequence);
}
