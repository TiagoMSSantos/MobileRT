package puscas.mobilertapp.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.logging.Logger;

/**
 * Utility class with some helper methods to use with {@link Buffer}s.
 */
public final class UtilsBuffer {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(UtilsBuffer.class.getName());

    /**
     * A private constructor in order to prevent instantiating this helper class.
     */
    private UtilsBuffer() {
    }

    /**
     * Helper method that checks if any of the {@link ByteBuffer}s is empty.
     *
     * @param byteBuffers The {@link ByteBuffer}s to check.
     * @return Whether any of {@link ByteBuffer}s is empty or not.
     */
    public static boolean isAnyByteBufferEmpty(@NonNull final ByteBuffer... byteBuffers) {
        LOGGER.info("isAnyByteBufferEmpty");

        for (final ByteBuffer byteBuffer : byteBuffers) {
            if (byteBuffer.capacity() <= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method that resets the position to read the {@link ByteBuffer}s.
     *
     * @param byteBuffers The {@link ByteBuffer}s to reset.
     */
    public static void resetByteBuffers(@NonNull final ByteBuffer... byteBuffers) {
        LOGGER.info("resetByteBuffers");

        for (final ByteBuffer byteBuffer : byteBuffers) {
            byteBuffer.order(ByteOrder.nativeOrder());
            byteBuffer.position(0);
        }
    }

    /**
     * Helper method that allocates a {@link FloatBuffer} with values from a
     * float array received via parameters.
     *
     * @param arrayValues The array containing the values to put in the new
     *                    {@link FloatBuffer}.
     * @return A new {@link FloatBuffer} with the values.
     */
    @NonNull
    public static FloatBuffer allocateBuffer(@NonNull final float[] arrayValues) {
        LOGGER.info("allocateBuffer");
        final int byteBufferSize = arrayValues.length * Constants.BYTES_IN_FLOAT;
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(byteBufferSize);
        resetByteBuffers(byteBuffer);
        final FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(arrayValues);
        floatBuffer.position(0);
        return floatBuffer;
    }

}
