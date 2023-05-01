#include "Components/Samplers/PCG.hpp"
#include <pcg_random.hpp>

using ::Components::PCG;

float PCG::getSample(const ::std::uint32_t /*sample*/) {
    thread_local static ::pcg_extras::seed_seq_from<::std::random_device> seedSource {};
    thread_local static ::pcg32 generator(seedSource);
    thread_local static ::std::uniform_real_distribution<float> uniformDist {0.0F, 1.0F};

    const auto res {uniformDist(generator)};
    return res;
}
