package puscas.mobilertapp.utils;

import androidx.annotation.NonNull;

import com.google.common.base.Strings;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

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
    public static void waitExecutorToFinish(@NonNull final ExecutorService executorService) {
        LOGGER.info("waitExecutorToFinish");
        boolean running = true;
        do {
            try {
                running = !executorService.awaitTermination(1L, TimeUnit.DAYS);
            } catch (final InterruptedException ex) {
                LOGGER.warning(Strings.nullToEmpty(ex.getMessage()));
                Thread.currentThread().interrupt();
            }
        } while (running);
        LOGGER.info("waitExecutorToFinish finished");
    }
}
