package puscas.mobilertapp.system;

import android.os.Debug;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.junit.Ignore;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runners.MethodSorters;
import puscas.mobilertapp.utils.Constants;

/**
 * The Android tests for memory behaviour in the system.
 *
 * These tests are useful to test the behaviour of allocating and freeing
 * native heap memory, as the Android unit tests only have Java heap and not a
 * native heap memory available.
 */
@Ignore("Ignore because JVM only has 2MB of native heap by default for the tests.")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class NativeMemoryTest {

    /**
     * The {@link Logger} for this class.
     */
    @Nonnull
    private static final Logger LOGGER = Logger.getLogger(NativeMemoryTest.class.getName());

    /**
     * Setup method called before each test.
     */
    @Before
    public void setUp() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }


    /**
     * Tests that allocating heap memory from native memory, makes the available memory decrease as expected.
     */
    @Test
    public void testAllocatingHeapMemoryNative() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        // Dummy array to hold the allocated memory.
        final Collection<ByteBuffer> dummyArrays = new ArrayList<>(1);

        final long firstAvailableMemoryMB = getAvailableNativeMemoryInMB();
        Assertions.assertTrue(firstAvailableMemoryMB > 300L, "Not enough available memory.");
        dummyArrays.add(ByteBuffer.allocateDirect(Constants.BYTES_IN_MEGABYTE));

        final long startAvailableMemory = getAvailableNativeMemoryInMB();
        Assertions.assertTrue(startAvailableMemory < firstAvailableMemoryMB,
            "Available memory didn't decrease as expected.");

        final long megaBytesToAllocate = 100L;
        int numAllocatedByteBuffers = 0;
        for(long l = 0L; getAvailableNativeMemoryInMB() >= startAvailableMemory - 2L * megaBytesToAllocate; l += megaBytesToAllocate) {
            // Force garbage collection now, before retrieving available memory
            // of the before and after allocating memory.
            System.gc();

            final long beforeAvailableMemoryMB = getAvailableNativeMemoryInMB();
            Assertions.assertTrue(beforeAvailableMemoryMB > megaBytesToAllocate, "Not enough available memory.");
            dummyArrays.add(ByteBuffer.allocateDirect(((int) megaBytesToAllocate * Constants.BYTES_IN_MEGABYTE)));

            final long afterAvailableMemory = getAvailableNativeMemoryInMB();
            Assertions.assertTrue(afterAvailableMemory <= (beforeAvailableMemoryMB - megaBytesToAllocate),
                "Not enough available memory.");

            ++numAllocatedByteBuffers;
        }

        Assertions.assertEquals(numAllocatedByteBuffers, dummyArrays.size(),
                "The number of allocated `ByteBuffer` is not the expected.");
    }

    /**
     * Helper method that calculates the available memory in the native Heap.
     * The returned value are in mega bytes.
     *
     * @return The available memory in the native Heap, in mega bytes.
     */
    private static long getAvailableNativeMemoryInMB() {
        return Debug.getNativeHeapFreeSize() / (long) Constants.BYTES_IN_MEGABYTE;
    }

}
