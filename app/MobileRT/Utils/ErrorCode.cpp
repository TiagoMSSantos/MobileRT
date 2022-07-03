#include "ErrorCode.hpp"

#include <cerrno>
#include <limits>
#include <vector>

namespace MobileRT {

    /**
     * Convert error code in integer format to string format.
     * Note: the descriptions in the comments were taken from: https://en.cppreference.com/w/cpp/error/errno_macros
     */
    #define NewError(errorCode, description) {errorCode, "##errorCode##", description}

    static ::std::vector<ErrorType> getErrorType() {
        static ::std::vector<ErrorType> errorType = {
            {0, "SUCCESS" , "Success"},            // 0
            // Errors from errno-base.h
            NewError(EPERM, "Operation not permitted"),          // 1
            NewError(ENOENT, "No such file or directory"),       // 2
            NewError(ESRCH, "No such process"),                  // 3
            NewError(EINTR, "Interrupted function"),             // 4
            NewError(EIO, "I/O error"),                          // 5
            NewError(ENXIO, "No such device or address"),        // 6
            NewError(E2BIG, "Argument list too long"),           // 7
            NewError(ENOEXEC, "Executable file format error"),   // 8
            NewError(EBADF, "Bad file descriptor"),              // 9
            NewError(ECHILD, "No child processes"),              // 10
            NewError(EAGAIN, "Resource unavailable, try again"), // 11
            NewError(ENOMEM, "Not enough space"),                // 12
            NewError(EACCES, "Permission denied"),               // 13
            NewError(EFAULT, "Bad address"),                     // 14
#ifndef _WIN32
            NewError(ENOTBLK, "Block device required"), // not compatible with Windows (15)
#endif
            NewError(EBUSY, "Device or resource busy"),          // 16
            NewError(EEXIST, "File exists"),                     // 17
            NewError(EXDEV, "Cross-device link"),                // 18
            NewError(ENODEV, "No such device"),                  // 19
            NewError(ENOTDIR, "Not a directory"),                // 20
            NewError(EISDIR, "Is a directory"),                  // 21
            NewError(EINVAL, "Invalid argument"),                // 22
            NewError(ENFILE, "Too many files open in system"),
            NewError(EMFILE, "File descriptor value too large"),
            NewError(ENOTTY, "Inappropriate I/O control operation"),
            NewError(ETXTBSY, "Text file busy"),
            NewError(EFBIG, "File too large"),
            NewError(ENOSPC, "No space left on device"),
            NewError(ESPIPE, "Invalid seek"),
            NewError(EROFS, "Read-only file system"),
            NewError(EMLINK, "Too many links" ),
            NewError(EPIPE, "Broken pipe" ),
            NewError(EDOM, "Mathematics argument out of domain of function"),
            NewError(ERANGE, " Result too large"),
            // Errors from errno.h
            NewError(EDEADLK, "Resource deadlock would occur"),
            NewError(ENAMETOOLONG, "Filename too long"),
            NewError(ENOLCK, "No locks available"),
            NewError(ENOSYS, "Function not supported"),
            NewError(ENOTEMPTY, "Directory not empty"),
            NewError(ELOOP, "Too many levels of symbolic links"),
            NewError(EWOULDBLOCK, "Operation would block"),
            NewError(ENOMSG, "No message of the desired type"),
            NewError(EIDRM, "Identifier removed"),
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
        };
        return errorType;
    }

    ErrorType getErrorCode() {
        ErrorType currentError {};
        const auto arrSize {static_cast<int> (getErrorType().size())};

        if (errno < arrSize) {
            for(auto i {0}; i < arrSize; i++) {
                if(getErrorType()[static_cast<unsigned long> (i)].code == errno) {
                    currentError = getErrorType()[static_cast<unsigned long> (i)];
                }
            }
        } else {
            // If the error is not identified.
            currentError = {::std::numeric_limits<int>::max(), "UNKNOWN" , "Unknown error occurred."};
        }

        return currentError;
    }
}
