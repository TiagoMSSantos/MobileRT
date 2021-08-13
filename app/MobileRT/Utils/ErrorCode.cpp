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
            {0, "SUCCESS" , "Success"},
            // Errors from errno-base.h
            NewError(EPERM, "Operation not permitted"),
            NewError(ENOENT, "No such file or directory"),
            NewError(ESRCH, "No such process"),
            NewError(EINTR, "Interrupted function"),
            NewError(EIO, "I/O error"),
            NewError(ENXIO, "No such device or address"),
            NewError(E2BIG, "Argument list too long"),
            NewError(ENOEXEC, "Executable file format error"),
            NewError(EBADF, "Bad file descriptor"),
            NewError(ECHILD, "No child processes"),
            NewError(EAGAIN, "Resource unavailable, try again"),
            NewError(ENOMEM, "Not enough space"),
            NewError(EACCES, "Permission denied"),
            NewError(EFAULT, "Bad address"),
#ifndef _WIN32
            NewError(ENOTBLK, "Block device required"),// not compatible with Windows
#endif
            NewError(EBUSY, "Device or resource busy"),
            NewError(EEXIST, "File exists"),
            NewError(EXDEV, "Cross-device link"),
            NewError(ENODEV, "No such device"),
            NewError(ENOTDIR, "Not a directory"),
            NewError(EISDIR, "Is a directory"),
            NewError(EINVAL, "Invalid argument"),
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
