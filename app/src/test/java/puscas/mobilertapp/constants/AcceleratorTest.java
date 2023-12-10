package puscas.mobilertapp.constants;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The unit tests for the {@link Accelerator} util class.
 */
public final class AcceleratorTest {

    /**
     * Tests the expected values of {@link Accelerator}.
     */
    @Test
    public void testValues() {
        Assertions.assertThat(Accelerator.values())
            .as("Accelerators available")
            .containsOnly(
                Accelerator.NONE,
                Accelerator.NAIVE,
                Accelerator.REG_GRID,
                Accelerator.BVH
            );
    }

    /**
     * Tests the {@link Accelerator#getNames() accelerator names}.
     */
    @Test
    public void testGetNames() {
        Assertions.assertThat(Accelerator.getNames())
            .as("Accelerators available")
            .containsOnly(
                (String) ReflectionTestUtils.getField(Accelerator.NONE, "name"),
                (String) ReflectionTestUtils.getField(Accelerator.NAIVE, "name"),
                (String) ReflectionTestUtils.getField(Accelerator.REG_GRID, "name"),
                (String) ReflectionTestUtils.getField(Accelerator.BVH, "name")
            );
    }

}
