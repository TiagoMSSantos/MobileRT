package puscas.mobilertapp.utils;

import lombok.extern.java.Log;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import puscas.mobilertapp.constants.Accelerator;

/**
 * The unit tests for the {@link Accelerator} util class.
 */
@Log
final class AcceleratorTest {

    /**
     * Tests that the {@link Accelerator#getNames()} method contains all the expected accelerators.
     */
    @Test
    void testGetNames() {
        Assertions.assertThat(Accelerator.getNames())
        .as("Check available accelerators.")
        .containsExactly(
            "None",
            "Naive",
            "RegGrid",
            "BVH"
        );
    }

}
