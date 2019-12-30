#ifndef UTILS_DEPENDENT_HPP
#define UTILS_DEPENDENT_HPP

#include <iostream>

namespace Dependent {
    inline void printString(const ::std::string &log);
}//namespace Dependent

void ::Dependent::printString(const ::std::string &log) {
    ::std::cout << log;
}

#endif //UTILS_DEPENDENT_HPP
