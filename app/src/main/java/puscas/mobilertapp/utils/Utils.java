package puscas.mobilertapp.utils;

import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import java8.util.Objects;
import kotlin.Pair;
import puscas.mobilertapp.constants.Constants;
import puscas.mobilertapp.constants.ConstantsMethods;
import puscas.mobilertapp.constants.ConstantsUI;
import puscas.mobilertapp.exceptions.FailureException;

/**
 * Utility class with some helper methods.
 */
public final class Utils {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(Utils.class.getSimpleName());

    /**
     * Private constructor to avoid creating instances.
     */
    private Utils() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Helper method that waits for an {@link ExecutorService} to finish all the
     * tasks.
     *
     * @param executorService The {@link ExecutorService}.
     */
    public static void waitExecutorToFinish(@NonNull final ExecutorService executorService) {
        logger.info("waitExecutorToFinish");

        boolean running = true;
        do {
            try {
                running = !executorService.awaitTermination(1L, TimeUnit.DAYS);
            } catch (final InterruptedException ex) {
                UtilsLogging.logThrowable(ex, "Utils#waitExecutorToFinish");
                Thread.currentThread().interrupt();
            } finally {
                handleInterruption("Utils#waitExecutorToFinish");
            }
        }
        while (running);

        final String message = "waitExecutorToFinish" + ConstantsMethods.FINISHED;
        logger.info(message);
    }

    /**
     * Helper method that handles an {@link InterruptedException}.
     *
     * @param methodName The name of the method to appear in the logs.
     * @implNote It resets the interrupted flag because when instrumented tests
     *     fail by timeout, this interrupt makes the {@link android.app.Activity}
     *     not finish properly.
     */
    public static void handleInterruption(@NonNull final String methodName) {
        final boolean interrupted = Thread.interrupted();
        final String message = methodName + " exception: " + interrupted;
        logger.severe(message);
    }

    /**
     * Helper method which reads a {@link String} from an {@link InputStreamReader}.
     *
     * @param inputStream The {@link InputStream} to read from.
     * @return A {@link String} containing the contents of the {@link InputStream}.
     */
    @NonNull
    public static String readTextFromInputStream(@NonNull final InputStream inputStream) {
        logger.info("readTextFromInputStream");
        try (InputStreamReader isReader = new InputStreamReader(
            inputStream, Charset.defaultCharset());
             BufferedReader reader = new BufferedReader(isReader)) {

            final StringBuilder stringBuilder = new StringBuilder();
            String str = reader.readLine();
            while (Objects.nonNull(str)) {
                stringBuilder.append(str).append(ConstantsUI.LINE_SEPARATOR);
                str = reader.readLine();
            }
            return stringBuilder.toString();
        } catch (final IOException ex) {
            UtilsLogging.logThrowable(ex, "Utils#readTextFromInputStream");
            throw new FailureException(ex);
        } finally {
            final String message = "readTextFromInputStream" + ConstantsMethods.FINISHED;
            logger.info(message);
        }
    }

    /**
     * Calculates the size, in MegaBytes of the scene with a certain number of
     * primitives.
     * Note that this method tries to predict the size of the scene in the
     * Ray Tracer engine context, and not in the OpenGL context.
     *
     * @param numPrimitives The number of primitives in the scene.
     * @return The size, in MegaBytes, of the scene.
     */
    @Contract(pure = true)
    public static int calculateSceneSize(final int numPrimitives) {
        logger.info("calculateSceneSize");
        final int triangleVerticesSize = 3 * Constants.BYTES_IN_FLOAT * 3;
        final int triangleNormalsSize = 3 * Constants.BYTES_IN_FLOAT * 3;
        final int triangleTextureCoordinatesSize = 2 * Constants.BYTES_IN_FLOAT * 3;
        final int triangleMaterialIndexSize = Constants.BYTES_IN_INTEGER;

        final int triangleMembersSize = triangleVerticesSize
            + triangleNormalsSize
            + triangleTextureCoordinatesSize
            + triangleMaterialIndexSize;

        final int triangleMethodsSize = Constants.BYTES_IN_POINTER * 21;
        final int triangleSize = triangleMembersSize + triangleMethodsSize;
        return 1 + ((numPrimitives * triangleSize) / Constants.BYTES_IN_MEGABYTE);
    }

    /**
     * Helper method that parses the displayed value from a {@link NumberPicker}
     * to an actual {@link Integer}.
     *
     * @param picker The {@link NumberPicker} to parse the displayed value.
     * @return The current displayed value in the {@link NumberPicker}.
     */
    public static int getValueFromPicker(@NonNull final NumberPicker picker) {
        logger.info("getValueFromPicker");

        try {
            return Integer.parseInt(picker.getDisplayedValues()[picker.getValue() - 1]);
        } catch (final Exception ex) {
            UtilsLogging.logThrowable(ex, "Utils#getValueFromPicker");
            throw new FailureException(ex);
        }
    }

    /**
     * Helper method that parses the displayed value from the resolution
     * {@link NumberPicker} to a {@link Pair} with the actual {@link Integer}s
     * (width, height).
     *
     * @param picker The {@link NumberPicker} to parse the displayed resolution.
     * @return The current displayed resolution in the {@link NumberPicker}.
     */
    @NonNull
    public static Pair<Integer, Integer> getResolutionFromPicker(@NonNull final NumberPicker picker) {
        logger.info("getResolutionFromPicker");

        try {
            final String strResolution = picker.getDisplayedValues()[picker.getValue() - 1];
            final int width = Integer.parseInt(strResolution.substring(0, strResolution.indexOf('x')));
            final int height = Integer.parseInt(strResolution.substring(strResolution.indexOf('x') + 1));
            return new Pair<>(width, height);
        } catch (final Exception ex) {
            UtilsLogging.logThrowable(ex, "Utils#getResolutionFromPicker");
            throw new FailureException("The Resolution Picker is not properly set. " +
                "Probably the View was closed, and now it can't render anything in it.");
        }
    }

}
