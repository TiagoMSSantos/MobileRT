#ifndef UTILS_DEPENDENT_HPP
#define UTILS_DEPENDENT_HPP

#include <android/log.h>
#include <string>

namespace Dependent {
    inline void printString(const ::std::string &log);
}//namespace Dependent

void Dependent::printString(const ::std::string &log) {
    __android_log_print(ANDROID_LOG_DEBUG, "LOG", "%s", log.c_str());
}

#endif //UTILS_DEPENDENT_HPP
