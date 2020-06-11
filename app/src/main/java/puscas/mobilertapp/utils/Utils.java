package puscas.mobilertapp.utils;

import com.google.common.base.Strings;

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
                LOGGER.severe("waitExecutorToFinish exception 1: " + ex.getClass().getName());
                LOGGER.severe("waitExecutorToFinish exception 2: " + Strings.nullToEmpty(ex.getMessage()));
                // Can't call Thread interrupt because when instrumented tests
                // fail by timeout, this interrupt makes the Activity not finish
                // properly.
//                Thread.currentThread().interrupt();
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
            LOGGER.severe(ste.toString());
        }
    }
}
