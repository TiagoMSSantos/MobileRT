package puscas.mobilertapp.configs;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * The test suite for {@link ConfigResolution} class.
 */
public class ConfigResolutionTest {

    /**
     * Tests the {@link ConfigResolution#builder()#toString()} method in the builder class of {@link Config}.
     */
    @Test
    public void testConfigResolutionBuilderToString() {
        final String configResolutionBuilderStr = ConfigResolution.builder().toString();
        Assertions.assertThat(configResolutionBuilderStr)
            .as("The toString of ConfigResolution.builder()")
            .isNotNull()
            .isInstanceOf(String.class);
    }

}
