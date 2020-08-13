package puscas.mobilertapp.utils;

import java.util.logging.Logger;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The unit tests for the {@link State} util class.
 */
public final class StateTest {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(StateTest.class.getName());

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
     * Tests that the {@link State#getId()} method contains all the expected states.
     */
    @Test
    public void testGetId() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        Assertions.assertThat(State.IDLE.getId()).isEqualTo(0);
        Assertions.assertThat(State.BUSY.getId()).isEqualTo(1);
        Assertions.assertThat(State.FINISHED.getId()).isEqualTo(2);
        Assertions.assertThat(State.STOPPED.getId()).isEqualTo(3);
    }

}
