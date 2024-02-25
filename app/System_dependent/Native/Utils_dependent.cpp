#include "Utils_dependent.hpp"

namespace {
    inline void print(const ::std::string &log) {
        ::std::cout << log.c_str();
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
    ::std::cerr << log.c_str() << ::std::endl << ::std::flush;
}
