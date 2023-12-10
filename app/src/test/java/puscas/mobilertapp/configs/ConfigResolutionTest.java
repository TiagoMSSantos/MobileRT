package puscas.mobilertapp.configs;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * The test suite for {@link ConfigResolution} class.
 */
public final class ConfigResolutionTest {

    /**
     * Tests the default constructor of {@link ConfigResolution}.
     */
    @Test
    public void testDefaultConstructor() {
        final ConfigResolution configResolution = ConfigResolution.Builder.Companion.create().build();

        Assertions.assertThat(configResolution.getWidth())
            .as("Width not the expected value.")
            .isOne();

        Assertions.assertThat(configResolution.getHeight())
            .as("Height not the expected value.")
            .isOne();
    }

    /**
     * Tests the building of {@link ConfigResolution} with valid values.
     */
    @Test
    public void testBuild() {
        final ConfigResolution.Builder builder = ConfigResolution.Builder.Companion.create();
        final int width = 123;
        final int height = 456;
        builder.setWidth(width);
        builder.setHeight(height);

        final ConfigResolution configResolution = builder.build();
        Assertions.assertThat(configResolution.getWidth())
            .as("Width not the expected value.")
            .isEqualTo(width);
        Assertions.assertThat(configResolution.getHeight())
            .as("Height not the expected value.")
            .isEqualTo(height);
    }

    /**
     * Test the building of {@link ConfigResolution} with invalid values.
     * <p>
     * The {@link ConfigResolution.Builder#build()} should fail with an exception.
     */
    @Test
    public void testBuildWithInvalidValues() {
        final ConfigResolution.Builder builder = ConfigResolution.Builder.Companion.create();
        final int width = 123;
        final int height = 456;

        builder.setWidth(0);
        builder.setHeight(height);
        Assertions.assertThatThrownBy(builder::build)
            .as("Width not the expected value.")
            .isInstanceOf(IllegalArgumentException.class);
        builder.setWidth(-1);
        builder.setHeight(height);
        Assertions.assertThatThrownBy(builder::build)
            .as("Width not the expected value.")
            .isInstanceOf(IllegalArgumentException.class);
        builder.setWidth(Integer.MIN_VALUE);
        builder.setHeight(height);
        Assertions.assertThatThrownBy(builder::build)
            .as("Width not the expected value.")
            .isInstanceOf(IllegalArgumentException.class);

        builder.setWidth(width);
        builder.setHeight(-1);
        Assertions.assertThatThrownBy(builder::build)
            .as("Height not the expected value.")
            .isInstanceOf(IllegalArgumentException.class);
        builder.setWidth(width);
        builder.setHeight(0);
        Assertions.assertThatThrownBy(builder::build)
            .as("Height not the expected value.")
            .isInstanceOf(IllegalArgumentException.class);
        builder.setWidth(width);
        builder.setHeight(Integer.MIN_VALUE);
        Assertions.assertThatThrownBy(builder::build)
            .as("Height not the expected value.")
            .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Tests the {@link ConfigResolution.Builder}.
     */
    @Test
    public void testBuilder() {
        final ConfigResolution.Builder builder = ConfigResolution.Builder.Companion.create();
        final int width = 123;
        final int height = 456;
        builder.setWidth(width);
        builder.setHeight(height);

        Assertions.assertThat(builder.getWidth())
            .as("Width not the expected value.")
            .isEqualTo(width);
        Assertions.assertThat(builder.getHeight())
            .as("Height not the expected value.")
            .isEqualTo(height);
    }

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
