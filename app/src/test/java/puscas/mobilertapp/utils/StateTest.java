package puscas.mobilertapp.utils;

import lombok.extern.java.Log;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * The unit tests for the {@link State} util class.
 */
@Log
public final class StateTest {

    /**
     * Tests that the {@link State#getId()} method contains all the expected states.
     */
    @Test
    public void testGetId() {
        Assertions.assertThat(State.IDLE.getId())
            .as("State id is not the expected.")
            .isZero();

        Assertions.assertThat(State.BUSY.getId())
            .as("State id is not the expected.")
            .isEqualTo(1);

        Assertions.assertThat(State.FINISHED.getId())
            .as("State id is not the expected.")
            .isEqualTo(2);

        Assertions.assertThat(State.STOPPED.getId())
            .as("State id is not the expected.")
            .isEqualTo(3);
    }

}
