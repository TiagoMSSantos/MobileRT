//
// Created by Tiago on 20-09-2018.
//

#ifndef UTILS_DEPENDENT_HPP
#define UTILS_DEPENDENT_HPP

#include <iostream>

namespace Dependent {
    inline void printString(const ::std::string &log) noexcept;
}//namespace Dependent

void ::Dependent::printString(const ::std::string &log) noexcept {
    ::std::cout << log;
}

#endif //UTILS_DEPENDENT_HPP
