#include "Utils_dependent.hpp"

namespace {
    inline void print(const ::std::string &log) {
        ::std::cout << log.c_str();
        #ifndef NDEBUG // Force flush if in debug mode.
            ::std::cout << ::std::flush;
        #endif
    }
}// namespace

void ::Dependent::printDebug(const ::std::string &log) {
    print(log);
}

void ::Dependent::printInfo(const ::std::string &log) {
    print(log);
}

void ::Dependent::printWarn(const ::std::string &log) {
    print(log);
}

void ::Dependent::printError(const ::std::string &log) {
    ::std::cout << ::std::flush;
    ::std::cerr << log.c_str() << ::std::endl << ::std::flush;
}
