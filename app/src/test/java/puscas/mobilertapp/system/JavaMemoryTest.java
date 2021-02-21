package puscas.mobilertapp.system;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import puscas.mobilertapp.utils.Constants;

/**
 * The unit tests for Java memory behaviour in the system.
 */
public class JavaMemoryTest {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(JavaMemoryTest.class.getName());

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
     * Tests that allocating heap memory from Java, makes the available memory decrease as expected.
     */
    @Test
    public void testAllocatingHeapMemoryJava() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        // Dummy array to hold the allocated memory.
        final Collection<ByteBuffer> dummyArrays = new ArrayList<>();

        final long startAvailableMemory = getAvailableJavaMemoryInMB();
        Assertions.assertThat(startAvailableMemory)
            .as("Not enough available memory.")
            .isGreaterThan(300L);

        final long megaBytesToAllocate = 100L;
        for(long l = 0L; getAvailableJavaMemoryInMB() >= startAvailableMemory - 200L; l += megaBytesToAllocate) {
            // Force garbage collection now, before retrieving available memory
            // of the before and after allocating memory.
            System.gc();

            final long beforeAvailableMemoryMB = getAvailableJavaMemoryInMB();
            Assertions.assertThat(beforeAvailableMemoryMB)
                .as("Not enough available memory.")
                .isGreaterThan(megaBytesToAllocate);
            dummyArrays.add(ByteBuffer.allocate(((int) megaBytesToAllocate * Constants.BYTES_IN_MEGABYTE)));

            final long afterAvailableMemory = getAvailableJavaMemoryInMB();
            Assertions.assertThat(afterAvailableMemory)
                .as("Available memory didn't decrease as expected.")
                .isLessThanOrEqualTo(beforeAvailableMemoryMB - megaBytesToAllocate);
        }
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
