package puscas.mobilertapp.system;

import static puscas.mobilertapp.Constants.NOT_ENOUGH_MEMORY_MESSAGE;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;

import puscas.mobilertapp.constants.Constants;

/**
 * The unit tests for Java memory behaviour in the system.
 */
public final class JavaMemoryTest {

    /**
     * Tests that allocating heap memory from Java, makes the available memory decrease as expected.
     */
    @Test
    public void testAllocatingHeapMemoryJava() {
        // Dummy array to hold the allocated memory.
        final Collection<ByteBuffer> dummyArrays = new ArrayList<>(0);

        final long startAvailableMemory = getAvailableJavaMemoryInMB();
        Assertions.assertThat(startAvailableMemory)
            .as(NOT_ENOUGH_MEMORY_MESSAGE)
            .isGreaterThan(300L);

        final long megaBytesToAllocate = 100L;
        for (int numAllocatedByteBuffers = 0; getAvailableJavaMemoryInMB() >= (megaBytesToAllocate + 1L) && numAllocatedByteBuffers < 2; ++numAllocatedByteBuffers) {
            // Force garbage collection now, before retrieving available memory
            // of the before and after allocating memory.
            Runtime.getRuntime().gc();

            final long beforeAvailableMemoryMB = getAvailableJavaMemoryInMB();
            Assertions.assertThat(beforeAvailableMemoryMB)
                .as(NOT_ENOUGH_MEMORY_MESSAGE)
                .isGreaterThan(megaBytesToAllocate);
            dummyArrays.add(ByteBuffer.allocate(((int) megaBytesToAllocate * Constants.BYTES_IN_MEGABYTE)));

            final long afterAvailableMemory = getAvailableJavaMemoryInMB();
            Assertions.assertThat(afterAvailableMemory)
                .as("Available memory didn't decrease as expected.")
                .isLessThanOrEqualTo(beforeAvailableMemoryMB - megaBytesToAllocate);
        }

        Assertions.assertThat(dummyArrays)
            .as("The number of allocated `ByteBuffer` is not the expected.")
            .hasSizeBetween(1, 2);
    }

    /**
     * Helper method that calculates the available memory in the Java Heap.
     * The returned value are in mega bytes.
     *
     * @return The available memory in the Java Heap, in mega bytes.
     */
    private static long getAvailableJavaMemoryInMB() {
        final Runtime runtime = Runtime.getRuntime();
        final long usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / (long) Constants.BYTES_IN_MEGABYTE;
        final long maxHeapSizeInMB = runtime.maxMemory() / (long) Constants.BYTES_IN_MEGABYTE;
        return maxHeapSizeInMB - usedMemInMB;
    }

}
