package puscas.mobilertapp.utils;

import androidx.annotation.NonNull;

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
        final String message = methodName + " exception: " + ex.getMessage();
        log.severe(message);
    }

    /**
     * Helper method which prints the stack trace.
     */
    public static void printStackTrace() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final StringBuilder stringBuilder = new StringBuilder();
        for (final StackTraceElement ste : stackTrace) {
            stringBuilder.append( "ste: ");
            stringBuilder.append(ste.toString());
            log.severe(stringBuilder.toString());
            stringBuilder.setLength(0);
        }
    }

}
