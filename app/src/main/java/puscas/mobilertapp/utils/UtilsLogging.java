package puscas.mobilertapp.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.logging.Logger;

/**
 * Utility class with some helper methods for the logging.
 */
public final class UtilsLogging {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(UtilsLogging.class.getName());

    /**
     * A private constructor in order to prevent instantiating this helper class.
     */
    private UtilsLogging() {
    }

    /**
     * Helper method that prints the message of a {@link Throwable}.
     *
     * @param ex         The {@link Throwable} to print.
     * @param methodName The name of the method to appear in the logs.
     */
    public static void logThrowable(@NonNull final Throwable ex,
                                    @NonNull final String methodName) {
        final String message = String.format("%s exception: %s", methodName, ex.getMessage());
        LOGGER.severe(message);
    }

    /**
     * Helper method which prints the stack trace.
     */
    public static void printStackTrace() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (final StackTraceElement ste : stackTrace) {
            final String stackTraceElement = String.format("ste: %s", ste.toString());
            LOGGER.severe(stackTraceElement);
        }
    }

}
