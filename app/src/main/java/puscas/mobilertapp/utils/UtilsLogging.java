package puscas.mobilertapp.utils;

import java.util.logging.Logger;
import javax.annotation.Nonnull;

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
    public static void logThrowable(@Nonnull final Throwable ex,
                                    @Nonnull final String methodName) {
        final String message = String.format("%s exception: %s", methodName, ex.getMessage());
        LOGGER.severe(message);
    }

    /**
     * Helper method which prints the stack trace.
     */
    public static void printStackTrace() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (final StackTraceElement ste : stackTrace) {
            final String stackTraceElement = "ste: " + ste.toString();
            LOGGER.severe(stackTraceElement);
        }
    }

}
