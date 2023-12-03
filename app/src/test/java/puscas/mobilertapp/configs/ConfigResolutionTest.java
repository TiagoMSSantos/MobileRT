package puscas.mobilertapp.configs;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * The test suite for {@link ConfigResolution} class.
 */
public final class ConfigResolutionTest {

    /**
     * Tests the {@link ConfigResolution.Builder#toString()} method in the builder class of {@link Config}.
     */
    @Test
    public void testConfigResolutionBuilderToString() {
        final String configResolutionBuilderStr = ConfigResolution.Builder.Companion.create().toString();
        Assertions.assertThat(configResolutionBuilderStr)
            .as("The toString of ConfigResolution.Builder")
            .isNotNull()
            .isInstanceOf(String.class);
    }

}
