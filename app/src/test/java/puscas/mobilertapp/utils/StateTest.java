package puscas.mobilertapp.utils;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * The unit tests for the {@link State} util class.
 */
public class StateTest {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(StateTest.class.getName());

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
     * Tests that the {@link State#getId()} method contains all the expected states.
     */
    @Test
    public void testGetId() {
        LOGGER.info("testGetId");
        Assertions.assertThat(State.IDLE.getId()).isEqualTo(0);
        Assertions.assertThat(State.BUSY.getId()).isEqualTo(1);
        Assertions.assertThat(State.END.getId()).isEqualTo(2);
        Assertions.assertThat(State.STOP.getId()).isEqualTo(3);
    }
}
