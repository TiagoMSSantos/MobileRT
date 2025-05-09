#ifndef UTILS_DEPENDENT_HPP
#define UTILS_DEPENDENT_HPP

#include <android/log.h>
#include <iostream>
#include <string>

namespace Dependent {
    inline void printDebug(const ::std::string &log);

    inline void printInfo(const ::std::string &log);

    inline void printWarn(const ::std::string &log);

    inline void printError(const ::std::string &log);
}//namespace Dependent

// Flush all buffers known.
static void flush() {
    ::std::cout << ::std::flush;
    ::std::wcout << ::std::flush;
    ::std::cerr << ::std::flush;
    ::std::wcerr << ::std::flush;
    ::std::clog << ::std::flush;
    ::std::wclog << ::std::flush;
}

// Force flush if in debug mode.
static void flushIfDebugMode() {
#ifndef NDEBUG
    flush();
#endif
}

void Dependent::printDebug(const ::std::string &log) {
    __android_log_print(ANDROID_LOG_DEBUG, "LOG", "%s", log.c_str());
    flushIfDebugMode();
}

void Dependent::printInfo(const ::std::string &log) {
    __android_log_print(ANDROID_LOG_INFO, "LOG", "%s", log.c_str());
    flushIfDebugMode();
}

void Dependent::printWarn(const ::std::string &log) {
    __android_log_print(ANDROID_LOG_WARN, "LOG", "%s", log.c_str());
    flush();
}

void Dependent::printError(const ::std::string &log) {
    __android_log_print(ANDROID_LOG_ERROR, "LOG", "%s", log.c_str());
    flush();
}

#endif //UTILS_DEPENDENT_HPP
