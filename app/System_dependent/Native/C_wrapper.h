#ifndef C_WRAPPER_HPP
#define C_WRAPPER_HPP

#include "MobileRT/Config.hpp"

#include <cstdint>

#ifndef __cplusplus
#include <stdbool.h>
#endif

#ifdef __cplusplus
extern "C"
#endif
void RayTrace(::MobileRT::Config &config, bool async);

#ifdef __cplusplus
extern "C"
#endif
void stopRender();

#endif // C_WRAPPER_HPP
