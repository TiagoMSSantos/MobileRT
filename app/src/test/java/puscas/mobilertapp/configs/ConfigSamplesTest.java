package puscas.mobilertapp.configs;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * The test suite for {@link ConfigSamples} class.
 */
public class ConfigSamplesTest {

    /**
     * Tests the {@link ConfigSamples#builder()#toString()} method in the builder class of {@link Config}.
     */
    @Test
    public void testConfigSamplesBuilderToString() {
        final String configSamplesBuilderStr = ConfigSamples.builder().toString();
        Assertions.assertThat(configSamplesBuilderStr)
            .as("The toString of ConfigSamples.builder()")
            .isNotNull()
            .isInstanceOf(String.class);
    }

}
