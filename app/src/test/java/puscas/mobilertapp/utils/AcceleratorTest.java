package puscas.mobilertapp.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import lombok.extern.java.Log;
import puscas.mobilertapp.constants.Accelerator;

/**
 * The unit tests for the {@link Accelerator} util class.
 */
@Log
public final class AcceleratorTest {

    /**
     * Tests that the {@link Accelerator#getNames()} method contains all the expected accelerators.
     */
    @Test
    public void testGetNames() {
        Assertions.assertThat(Accelerator.getNames()).containsExactly(
            "None",
            "Naive",
            "RegGrid",
            "BVH"
        );
    }

}
