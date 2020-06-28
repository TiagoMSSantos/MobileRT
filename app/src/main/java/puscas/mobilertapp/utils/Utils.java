package puscas.mobilertapp.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Utility class with some helper methods.
 */
public final class Utils {
    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());

    /**
     * A private constructor in order to prevent instantiating this helper class.
     */
    private Utils() {
        LOGGER.info("Utils");
    }

    /**
     * Helper method that waits for an {@link ExecutorService} to finish all the tasks.
     *
     * @param executorService The {@link ExecutorService}.
     */
    public static void waitExecutorToFinish(@Nonnull final ExecutorService executorService) {
        LOGGER.info("waitExecutorToFinish");
        boolean running = true;
        do {
            try {
                running = !executorService.awaitTermination(1L, TimeUnit.DAYS);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                Utils.handleInterruption("Utils#waitExecutorToFinish");
            }
        } while (running);
        LOGGER.info("waitExecutorToFinish finished");
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

    /**
     * The {@link InterruptedException} to handle.
     *
     * @param methodName The name of the method to appear in the logs.
     */
    public static void handleInterruption(@Nonnull final String methodName) {
        // Reset the interrupted flag because when instrumented tests
        // fail by timeout, this interrupt makes the Activity not finish
        // properly.
        final boolean interrupted = Thread.interrupted();
        final String message = String.format("%s exception: %s", methodName, interrupted);
        LOGGER.severe(message);
    }
}
