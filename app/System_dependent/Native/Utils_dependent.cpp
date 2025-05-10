#include "Utils_dependent.hpp"

namespace {
    inline void print(const ::std::string &log) {
        ::std::cout << log.c_str();
    }
}// namespace

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

void ::Dependent::printDebug(const ::std::string &log) {
    print(log);
    flushIfDebugMode();
}

void ::Dependent::printInfo(const ::std::string &log) {
    print(log);
    flushIfDebugMode();
}

void ::Dependent::printWarn(const ::std::string &log) {
    print(log);
    flush();
}

void ::Dependent::printError(const ::std::string &log) {
    ::std::cerr << log.c_str();
    flush();
}
