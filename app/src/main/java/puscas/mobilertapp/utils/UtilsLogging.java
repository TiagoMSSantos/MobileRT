package puscas.mobilertapp.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;

/**
 * Utility class with some helper methods for the logging.
 */
@UtilityClass
@Log
public final class UtilsLogging {

    /**
     * Helper method that prints the message of a {@link Throwable}.
     *
     * @param ex         The {@link Throwable} to print.
     * @param methodName The name of the method to appear in the logs.
     */
    public static void logThrowable(@NonNull final Throwable ex,
                                    @NonNull final String methodName) {
        final String message = String.format("%s exception: %s", methodName, ex.getMessage());
        log.severe(message);
    }

    /**
     * Helper method which prints the stack trace.
     */
    public static void printStackTrace() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (final StackTraceElement ste : stackTrace) {
            final String stackTraceElement = String.format("ste: %s", ste.toString());
            log.severe(stackTraceElement);
        }
    }

}
