package puscas.mobilertapp.utils;

import java.util.logging.Logger;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The unit tests for the {@link Accelerator} util class.
 */
public final class AcceleratorTest {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(AcceleratorTest.class.getName());

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
     * Tests that the {@link Accelerator#getNames()} method contains all the expected accelerators.
     */
    @Test
    public void testGetNames() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        Assertions.assertThat(Accelerator.getNames()).containsExactly(
            "None",
            "Naive",
            "RegGrid",
            "BVH"
        );
    }

}
