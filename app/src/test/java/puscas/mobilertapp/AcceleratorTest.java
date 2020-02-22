package puscas.mobilertapp;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import puscas.mobilertapp.utils.Accelerator;
import puscas.mobilertapp.utils.Constants;

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
        LOGGER.info(Constants.SET_UP);
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        LOGGER.info(Constants.TEAR_DOWN);
    }

    /**
     * Tests that the {@link Accelerator#getNames()} method contains all the expected accelerators.
     */
    @Test
    public void testGetNames() {
        LOGGER.info("testGetNames");
        Assertions.assertThat(Accelerator.getNames()).contains(
                Accelerator.NONE.getName(),
                Accelerator.NAIVE.getName(),
                Accelerator.REG_GRID.getName(),
                Accelerator.BVH.getName()
        );
    }
}
