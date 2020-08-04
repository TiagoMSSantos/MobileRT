package puscas.mobilertapp.utils;

import org.jetbrains.annotations.Contract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import java8.util.Objects;
import puscas.mobilertapp.exceptions.FailureException;

import static puscas.mobilertapp.utils.Constants.BYTES_IN_FLOAT;
import static puscas.mobilertapp.utils.Constants.BYTES_IN_INTEGER;
import static puscas.mobilertapp.utils.Constants.BYTES_IN_POINTER;
import static puscas.mobilertapp.utils.ConstantsMethods.FINISHED;

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
     * Helper method that waits for an {@link ExecutorService} to finish all the
     * tasks.
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
                Utils.handleInterruption("Utils#waitExecutorToFinish");
            }
        } while (running);
        LOGGER.info("waitExecutorToFinish" + ConstantsMethods.FINISHED);
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
     * Helper method that handles an {@link InterruptedException}.
     *
     * @param methodName The name of the method to appear in the logs.
     * @implNote It resets the interrupted flag because when instrumented tests
     * fail by timeout, this interrupt makes the {@link android.app.Activity}
     * not finish properly.
     */
    public static void handleInterruption(@Nonnull final String methodName) {
        final boolean interrupted = Thread.interrupted();
        final String message = String.format("%s exception: %s", methodName, interrupted);
        LOGGER.severe(message);
    }

    /**
     * Helper method that handles a general {@link Throwable}.
     *
     * @param methodName The name of the method to appear in the logs.
     */
    public static void logThrowable(@Nonnull final Throwable ex,
                                    @Nonnull final String methodName) {
        final String message = String.format("%s exception: %s", methodName, ex.getMessage());
        LOGGER.severe(message);
    }

    /**
     * Helper method which reads a {@link String} from an {@link InputStreamReader}.
     *
     * @param inputStream The {@link InputStream} to read from.
     * @return A {@link String} containing the contents of the {@link InputStream}.
     */
    @Nonnull
    public static String readTextFromInputStream(@Nonnull final InputStream inputStream) {
        LOGGER.info("readTextFromInputStream");
        try (InputStreamReader isReader = new InputStreamReader(
            inputStream, Charset.defaultCharset());
             BufferedReader reader = new BufferedReader(isReader)) {

            final StringBuilder stringBuilder = new StringBuilder(1);
            String str = reader.readLine();
            while (Objects.nonNull(str)) {
                stringBuilder.append(str).append(ConstantsUI.LINE_SEPARATOR);
                str = reader.readLine();
            }
            return stringBuilder.toString();
        } catch (final OutOfMemoryError ex1) {
            throw new FailureException(ex1);
        } catch (final IOException ex2) {
            throw new FailureException(ex2);
        } finally {
            LOGGER.info("readTextFromInputStream" + FINISHED);
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
        LOGGER.info("calculateSceneSize");
        final int triangleVerticesSize = 3 * BYTES_IN_FLOAT * 3;
        final int triangleNormalsSize = 3 * BYTES_IN_FLOAT * 3;
        final int triangleTextureCoordinatesSize = 2 * BYTES_IN_FLOAT * 3;
        final int triangleMaterialIndexSize = BYTES_IN_INTEGER;

        final int triangleMembersSize = triangleVerticesSize +
            triangleNormalsSize +
            triangleTextureCoordinatesSize +
            triangleMaterialIndexSize;

        final int triangleMethodsSize = BYTES_IN_POINTER * 21;
        final int triangleSize = triangleMembersSize + triangleMethodsSize;
        return 1 + ((numPrimitives * triangleSize) / Constants.BYTES_IN_MB);
    }

    /**
     * Helper method that resets the position to read the {@link ByteBuffer}.
     * It also checks if the system has at least 1 mega byte free in the main
     * memory.
     *
     * @param byteBuffer The {@link ByteBuffer} to reset.
     */
    public static void resetByteBuffer(@Nonnull final ByteBuffer byteBuffer) {
        LOGGER.info("calculateSceneSize");
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.position(0);
    }

    /**
     * Helper method that allocates a {@link FloatBuffer} with values from a
     * float array received via parameters.
     *
     * @param arrayValues The array containing the values to put in the new
     *                    {@link FloatBuffer}.
     * @return A new {@link FloatBuffer} with the values.
     */
    @Nonnull
    public static FloatBuffer allocateBuffer(@Nonnull final float[] arrayValues) {
        LOGGER.info("allocateBuffer");
        final int byteBufferSize = arrayValues.length * BYTES_IN_FLOAT;
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(byteBufferSize);
        resetByteBuffer(byteBuffer);
        final FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(arrayValues);
        floatBuffer.position(0);
        return floatBuffer;
    }

}
