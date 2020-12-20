#ifndef UTILS_DEPENDENT_HPP
#define UTILS_DEPENDENT_HPP

#include <iostream>

namespace Dependent {
    inline void printDebug(const ::std::string &log);

    inline void printInfo(const ::std::string &log);

    inline void printWarn(const ::std::string &log);

    inline void printError(const ::std::string &log);
}//namespace Dependent

namespace {
    inline void print(const ::std::string &log) {
        ::std::cout << log;
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
    ::std::cerr << log;
}

#endif //UTILS_DEPENDENT_HPP
