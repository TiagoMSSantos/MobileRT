package puscas.mobilertapp.system;

import static puscas.mobilertapp.ConstantsAndroidTests.NOT_ENOUGH_MEMORY_MESSAGE;

import android.os.Debug;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runners.MethodSorters;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import lombok.extern.java.Log;
import puscas.mobilertapp.constants.Constants;

/**
 * The Android tests for memory behaviour in the system.
 *
 * These tests are useful to test the behaviour of allocating and freeing
 * native heap memory, as the Android unit tests only have Java heap and not a
 * native heap memory available.
 */
@Ignore("Ignore because JVM only has 2MB of native heap by default for the tests.")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Log
public final class NativeMemoryTest {

    /**
     * Setup method called before each test.
     */
    @Before
    public void setUp() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.info(methodName);
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.info(methodName);
    }


    /**
     * Tests that allocating heap memory from native memory, makes the available memory decrease as expected.
     */
    @Test
    public void testAllocatingHeapMemoryNative() {
        // Dummy array to hold the allocated memory.
        final Collection<ByteBuffer> dummyArrays = new ArrayList<>(1);

        final long firstAvailableMemoryMB = getAvailableNativeMemoryInMB();
        Assertions.assertTrue(firstAvailableMemoryMB > 300L, NOT_ENOUGH_MEMORY_MESSAGE);
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
            Assertions.assertTrue(beforeAvailableMemoryMB > megaBytesToAllocate, NOT_ENOUGH_MEMORY_MESSAGE);
            dummyArrays.add(ByteBuffer.allocateDirect(((int) megaBytesToAllocate * Constants.BYTES_IN_MEGABYTE)));

            final long afterAvailableMemory = getAvailableNativeMemoryInMB();
            Assertions.assertTrue(afterAvailableMemory <= (beforeAvailableMemoryMB - megaBytesToAllocate),
                    NOT_ENOUGH_MEMORY_MESSAGE);

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
        final String template = "%s: %dKB (%dMB)";
        final long nativeHeapSizeBytes = Debug.getNativeHeapSize();
        final String sizeNativeHeap = String.format(Locale.US, template,
                "The size of the native heap",
                convertBytesToKiloBytes(nativeHeapSizeBytes),
                convertBytesToMegaBytes(nativeHeapSizeBytes)
        );
        log.info(sizeNativeHeap);


        final long nativeHeapAllocatedSizeBytes = Debug.getNativeHeapAllocatedSize();
        final String sizeNativeAllocatedHeap = String.format(Locale.US, template,
                "Allocated memory in the native heap",
                convertBytesToKiloBytes(nativeHeapAllocatedSizeBytes),
                convertBytesToMegaBytes(nativeHeapAllocatedSizeBytes)
        );
        log.info(sizeNativeAllocatedHeap);


        final long nativeHeapFreeSizeBytes = Debug.getNativeHeapFreeSize();
        final long availableMemoryMb = convertBytesToMegaBytes(nativeHeapFreeSizeBytes);
        final String sizeNativeHeapFree = String.format(Locale.US, template,
                "Available native heap memory",
                convertBytesToKiloBytes(nativeHeapFreeSizeBytes),
                availableMemoryMb
        );
        log.info(sizeNativeHeapFree);

        return availableMemoryMb;
    }

    /**
     * Helper method that converts a number of bytes into kilobytes.
     *
     * @param bytes The number of bytes.
     * @return The number of kilobytes.
     */
    private static long convertBytesToKiloBytes(final long bytes) {
        return bytes / 1024L;
    }

    /**
     * Helper method that converts a number of bytes into megabytes.
     *
     * @param bytes The number of bytes.
     * @return The number of megabytes.
     */
    private static long convertBytesToMegaBytes(final long bytes) {
        return bytes / (long) Constants.BYTES_IN_MEGABYTE;
    }

}
