#ifndef MOBILERT_ERRORCODE_HPP
#define MOBILERT_ERRORCODE_HPP

#include <string>

namespace MobileRT {

    /**
     * Struct to help convert error code in integer format to string format.
     */
    struct ErrorType {
        int code;
        ::std::string codeText;
        ::std::string description;
    };

    /**
     * Helper method that gets the error code, in string format, so its easy to find information on the internet.
     *
     * @return The error code with its description in string format.
     */
    ErrorType getErrorCode();
}//namespace MobileRT

#endif //MOBILERT_ERRORCODE_HPP
