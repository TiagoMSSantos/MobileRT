#include "ErrorCode.hpp"
#include "Utils.hpp"

#include <array>
#include <cerrno>
#include <limits>

namespace MobileRT {

    /**
     * Convert error code in integer format to string format.
     * Note: the descriptions in the comments were taken from: https://en.cppreference.com/w/cpp/error/errno_macros
     */
    #define NewError(errorCode, description) {errorCode, "##errorCode##", description}

    static ::std::array<ErrorType, 44> getErrorType() {
        ::std::array<ErrorType, 44> errorType = {{
            {0, "SUCCESS" , "Success"},                          // 1
            // Errors from errno-base.h
            NewError(EPERM, "Operation not permitted"),          // 2
            NewError(ENOENT, "No such file or directory"),       // 3
            NewError(ESRCH, "No such process"),                  // 4
            NewError(EINTR, "Interrupted function"),             // 5
            NewError(EIO, "I/O error"),                          // 6
            NewError(ENXIO, "No such device or address"),        // 7
            NewError(E2BIG, "Argument list too long"),           // 8
            NewError(ENOEXEC, "Executable file format error"),   // 9
            NewError(EBADF, "Bad file descriptor"),              // 10
            NewError(ECHILD, "No child processes"),              // 11
            NewError(EAGAIN, "Resource unavailable, try again"), // 12
            NewError(ENOMEM, "Not enough space"),                // 13
            NewError(EACCES, "Permission denied"),               // 14
            NewError(EFAULT, "Bad address"),                     // 15
#ifndef _WIN32
            NewError(ENOTBLK, "Block device required"), // not compatible with Windows (16)
#endif
            NewError(EBUSY, "Device or resource busy"),          // 17
            NewError(EEXIST, "File exists"),                     // 18
            NewError(EXDEV, "Cross-device link"),                // 19
            NewError(ENODEV, "No such device"),                  // 20
            NewError(ENOTDIR, "Not a directory"),                // 21
            NewError(EISDIR, "Is a directory"),                  // 22
            NewError(EINVAL, "Invalid argument"),                // 23
            NewError(ENFILE, "Too many files open in system"),   // 24
            NewError(EMFILE, "File descriptor value too large"), // 25
            NewError(ENOTTY, "Inappropriate I/O control operation"),
            NewError(ETXTBSY, "Text file busy"),                 // 27
            NewError(EFBIG, "File too large"),                   // 28
            NewError(ENOSPC, "No space left on device"),         // 29
            NewError(ESPIPE, "Invalid seek"),                    // 30
            NewError(EROFS, "Read-only file system"),            // 31
            NewError(EMLINK, "Too many links" ),                 // 32
            NewError(EPIPE, "Broken pipe" ),                     // 33
            NewError(EDOM, "Mathematics argument out of domain of function"),
            NewError(ERANGE, " Result too large"),               // 35
            // Errors from errno.h
            NewError(EDEADLK, "Resource deadlock would occur"),  // 36
            NewError(ENAMETOOLONG, "Filename too long"),         // 37
            NewError(ENOLCK, "No locks available"),              // 38
            NewError(ENOSYS, "Function not supported"),          // 39
            NewError(ENOTEMPTY, "Directory not empty"),          // 40
            NewError(ELOOP, "Too many levels of symbolic links"),// 41
            NewError(EWOULDBLOCK, "Operation would block"),      // 42
            NewError(ENOMSG, "No message of the desired type"),  // 43
            NewError(EIDRM, "Identifier removed"),               // 44
            // Errors that are not available in MacOS.
//            NewError(ECHRNG, "Channel number out of range"),
//            NewError(EL2NSYNC, "Level 2 not synchronized"),
//            NewError(EL3HLT, "Level 3 halted"),
//            NewError(EL3RST, "Level 3 reset"),
//            NewError(ELNRNG, "Link number out of range"),
//            NewError(EUNATCH, "Protocol driver not attached"),
//            NewError(ENOCSI, "No CSI structure available"),
//            NewError(EL2HLT, "Level 2 halted"),
//            NewError(EBADE, "Invalid exchange"),
//            NewError(EBADR, "Invalid request descriptor"),
//            NewError(EXFULL, "Exchange full")
        }};
        return errorType;
    }

    ErrorType getErrorCode() {
        try {
            LOG_ERROR("ERROR 2: ", errno);
            ErrorType currentError {};
            LOG_ERROR("ERROR 3: ", errno);
            const ::std::array<ErrorType, 44> errors {getErrorType()};
            const auto arrSize {static_cast<int> (errors.size())};
            LOG_ERROR("ERROR 4: ", errno, ", size: ", arrSize);

            if (errno < arrSize) {
                LOG_ERROR("ERROR 5: ", errno);
                for(auto i {0}; i < arrSize; i++) {
                    if(errors[static_cast<unsigned long> (i)].code == errno) {
                        LOG_ERROR("ERROR 6: ", errno, ", index: ", i);
                        currentError = errors[static_cast<unsigned long> (i)];
                        LOG_ERROR("ERROR 7: ", errno, ", index: ", i, ", currentError: ", currentError.description);
                    }
                }
                LOG_ERROR("ERROR 8: ", errno, ", size: ", arrSize, ", currentError: ", currentError.description);
            } else {
                // If the error is not identified.
                currentError = {::std::numeric_limits<int>::max(), "UNKNOWN" , "Unknown error occurred."};
            }

            return currentError;
        } catch (const ::std::exception &exception) {
            LOG_ERROR("exception: ", exception.what());
        } catch (...) {
            LOG_ERROR("Unknown error");
        }
        return ErrorType {};
    }
}
