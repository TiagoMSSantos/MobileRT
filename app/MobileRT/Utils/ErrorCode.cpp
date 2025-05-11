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

    static ::std::array<ErrorType, 55> getErrorType() {
        ::std::array<ErrorType, 55> errorType = {{
            {0, "SUCCESS" , "Success"},              // 0
            // Errors from errno-base.h
            NewError(EPERM, "Operation not permitted"),                       // 1
            NewError(ENOENT, "No such file or directory"),                    // 2
            NewError(ESRCH, "No such process"),                               // 3
            NewError(EINTR, "Interrupted function"),                          // 4
            NewError(EIO, "I/O error"),                                       // 5
            NewError(ENXIO, "No such device or address"),                     // 6
            NewError(E2BIG, "Argument list too long"),                        // 7
            NewError(ENOEXEC, "Executable file format error"),                // 8
            NewError(EBADF, "Bad file descriptor"),                           // 9
            NewError(ECHILD, "No child processes"),                           // 10
            NewError(EAGAIN, "Resource unavailable, try again"),              // 11
            NewError(ENOMEM, "Not enough space"),                             // 12
            NewError(EACCES, "Permission denied"),                            // 13
            NewError(EFAULT, "Bad address"),                                  // 14
#ifndef _WIN32 // Not compatible with Windows.
            NewError(ENOTBLK, "Block device required"),                       // 15
#endif
            NewError(EBUSY, "Device or resource busy"),                       // 16
            NewError(EEXIST, "File exists"),                                  // 17
            NewError(EXDEV, "Cross-device link"),                             // 18
            NewError(ENODEV, "No such device"),                               // 19
            NewError(ENOTDIR, "Not a directory"),                             // 20
            NewError(EISDIR, "Is a directory"),                               // 21
            NewError(EINVAL, "Invalid argument"),                             // 22
            NewError(ENFILE, "Too many files open in system"),                // 23
            NewError(EMFILE, "File descriptor value too large"),              // 24
            NewError(ENOTTY, "Inappropriate I/O control operation"),          // 25
            NewError(ETXTBSY, "Text file busy"),                              // 26
            NewError(EFBIG, "File too large"),                                // 27
            NewError(ENOSPC, "No space left on device"),                      // 28
            NewError(ESPIPE, "Invalid seek"),                                 // 29
            NewError(EROFS, "Read-only file system"),                         // 30
            NewError(EMLINK, "Too many links" ),                              // 31
            NewError(EPIPE, "Broken pipe" ),                                  // 32
            NewError(EDOM, "Mathematics argument out of domain of function"), // 33
            NewError(ERANGE, " Result too large"),                            // 34
            // Errors from errno.h
            NewError(EDEADLK, "Resource deadlock would occur"),               // 35
            NewError(ENAMETOOLONG, "Filename too long"),                      // 36
            NewError(ENOLCK, "No locks available"),                           // 37
            NewError(ENOSYS, "Function not supported"),                       // 38
            NewError(ENOTEMPTY, "Directory not empty"),                       // 39
            NewError(ELOOP, "Too many levels of symbolic links"),             // 40
            NewError(EWOULDBLOCK, "Operation would block"),                   // 41
            NewError(ENOMSG, "No message of the desired type"),               // 42
            NewError(EIDRM, "Identifier removed"),                            // 43
#if !defined(_WIN32) && !defined(__APPLE__) // Not compatible with Windows & MacOS.
            NewError(ECHRNG, "Channel number out of range"),                  // 44
            NewError(EL2NSYNC, "Level 2 not synchronized"),
            NewError(EL3HLT, "Level 3 halted"),
            NewError(EL3RST, "Level 3 reset"),
            NewError(ELNRNG, "Link number out of range"),
            NewError(EUNATCH, "Protocol driver not attached"),
            NewError(ENOCSI, "No CSI structure available"),
            NewError(EL2HLT, "Level 2 halted"),
            NewError(EBADE, "Invalid exchange"),
            NewError(EBADR, "Invalid request descriptor"),
            NewError(EXFULL, "Exchange full"),                                // 54
#endif
        }};
        return errorType;
    }

    ErrorType getErrorCode() {
        ErrorType currentError {};
        const ::std::array<ErrorType, 55> errors {getErrorType()};
        const int arrSize {static_cast<int> (errors.size())};

        if (errno < arrSize) {
            for(int i {0}; i < arrSize; i++) {
                if(errors[static_cast<unsigned long> (i)].code == errno) {
                    currentError = errors[static_cast<unsigned long> (i)];
                }
            }
        } else {
            // If the error is not identified.
            currentError = {::std::numeric_limits<int>::max(), "UNKNOWN" , "Unknown error occurred."};
        }

        return currentError;
    }
}
