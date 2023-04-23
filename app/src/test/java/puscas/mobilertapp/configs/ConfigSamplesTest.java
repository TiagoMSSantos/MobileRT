package puscas.mobilertapp.configs;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * The test suite for {@link ConfigSamples} class.
 */
public class ConfigSamplesTest {

    /**
     * Tests the {@link ConfigSamples.Builder#toString()} method in the builder class of {@link Config}.
     */
    @Test
    public void testConfigSamplesBuilderToString() {
        final String configSamplesBuilderStr = ConfigSamples.Builder.Companion.create().toString();
        Assertions.assertThat(configSamplesBuilderStr)
            .as("The toString of ConfigSamples.Builder")
            .isNotNull()
            .isInstanceOf(String.class);
    }

}
