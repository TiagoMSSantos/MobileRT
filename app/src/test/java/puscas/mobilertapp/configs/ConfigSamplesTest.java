package puscas.mobilertapp.configs;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * The test suite for {@link ConfigSamples} class.
 */
public final class ConfigSamplesTest {

    /**
     * Tests the default constructor of {@link ConfigSamples}.
     */
    @Test
    public void testDefaultConfigSamples() {
        final ConfigSamples configSamples = ConfigSamples.Builder.Companion.create().build();

        Assertions.assertThat(configSamples.getSamplesPixel())
            .as("The ConfigSamples#Builder#build with default samplesPixel.")
            .isZero();
        Assertions.assertThat(configSamples.getSamplesLight())
            .as("The ConfigSamples#Builder#build with default samplesLight.")
            .isZero();
    }

    /**
     * Tests the building of {@link ConfigSamples} with valid values.
     */
    @Test
    public void testBuild() {
        final ConfigSamples.Builder builder = ConfigSamples.Builder.Companion.create();
        final int samplesPixel = 123;
        final int samplesLight = 456;
        builder.setSamplesPixel(samplesPixel);
        builder.setSamplesLight(samplesLight);

        final ConfigSamples configSamples = builder.build();
        Assertions.assertThat(configSamples.getSamplesPixel())
            .as("SamplesPixel not the expected value.")
            .isEqualTo(samplesPixel);
        Assertions.assertThat(configSamples.getSamplesLight())
            .as("SamplesLight not the expected value.")
            .isEqualTo(samplesLight);
    }

    /**
     * Test the building of {@link ConfigSamples} with invalid values.
     * <p>
     * The {@link ConfigSamples.Builder#build()} should fail with an exception.
     */
    @Test
    public void testBuildWithInvalidValues() {
        final ConfigSamples.Builder builder = ConfigSamples.Builder.Companion.create();
        final int samplesPixel = 123;
        final int samplesLight = 456;

        builder.setSamplesPixel(-1);
        builder.setSamplesLight(samplesLight);
        Assertions.assertThatThrownBy(builder::build)
            .as("SamplesPixel not the expected value.")
            .isInstanceOf(IllegalArgumentException.class);
        builder.setSamplesPixel(Integer.MIN_VALUE);
        builder.setSamplesLight(samplesLight);
        Assertions.assertThatThrownBy(builder::build)
            .as("SamplesPixel not the expected value.")
            .isInstanceOf(IllegalArgumentException.class);

        builder.setSamplesPixel(samplesPixel);
        builder.setSamplesLight(-1);
        Assertions.assertThatThrownBy(builder::build)
            .as("SamplesPixel not the expected value.")
            .isInstanceOf(IllegalArgumentException.class);
        builder.setSamplesPixel(samplesPixel);
        builder.setSamplesLight(Integer.MIN_VALUE);
        Assertions.assertThatThrownBy(builder::build)
            .as("SamplesPixel not the expected value.")
            .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Tests the {@link ConfigSamples.Builder}.
     */
    @Test
    public void testBuilder() {
        final ConfigSamples.Builder builder = ConfigSamples.Builder.Companion.create();
        final int samplesPixel = 123;
        final int samplesLight = 456;
        builder.setSamplesPixel(samplesPixel);
        builder.setSamplesLight(samplesLight);

        Assertions.assertThat(builder.getSamplesPixel())
            .as("SamplesPixel not the expected value.")
            .isEqualTo(samplesPixel);
        Assertions.assertThat(builder.getSamplesLight())
            .as("SamplesLight not the expected value.")
            .isEqualTo(samplesLight);
    }

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
