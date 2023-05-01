#ifndef UTILS_DEPENDENT_HPP
#define UTILS_DEPENDENT_HPP

#include <android/log.h>
#include <string>

namespace Dependent {
    inline void printDebug(const ::std::string &log);

    inline void printInfo(const ::std::string &log);

    inline void printWarn(const ::std::string &log);

    inline void printError(const ::std::string &log);
}//namespace Dependent

void Dependent::printDebug(const ::std::string &log) {
    __android_log_print(ANDROID_LOG_DEBUG, "LOG", "%s", log.c_str());
}

void Dependent::printInfo(const ::std::string &log) {
    __android_log_print(ANDROID_LOG_INFO, "LOG", "%s", log.c_str());
}

void Dependent::printWarn(const ::std::string &log) {
    __android_log_print(ANDROID_LOG_WARN, "LOG", "%s", log.c_str());
}

void Dependent::printError(const ::std::string &log) {
    __android_log_print(ANDROID_LOG_ERROR, "LOG", "%s", log.c_str());
}

#endif //UTILS_DEPENDENT_HPP
