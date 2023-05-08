package puscas.mobilertapp.utils;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Utility class with some helper methods for the logging.
 */
public final class UtilsLogging {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(UtilsLogging.class.getSimpleName());

    /**
     * Private constructor to avoid creating instances.
     */
    private UtilsLogging() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Helper method that prints the message of a {@link Throwable}.
     *
     * @param ex         The {@link Throwable} to print.
     * @param methodName The name of the method to appear in the logs.
     */
    public static void logThrowable(@NonNull final Throwable ex,
                                    @NonNull final String methodName) {
        final String message = methodName + " exception: " + ex.getMessage() + "\n" + Arrays.toString(ex.getStackTrace());
        logger.severe(message);
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
            logger.severe(stringBuilder.toString());
            stringBuilder.setLength(0);
        }
    }

}
