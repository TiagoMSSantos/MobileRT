package puscas.mobilertapp.configs;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * The test suite for {@link ConfigGlAttribute} class.
 */
public final class ConfigGlAttributeTest {

    /**
     * Tests the {@link ConfigGlAttribute.Builder#toString()} method in the builder class of {@link Config}.
     */
    @Test
    public void testConfigGlAttributeBuilderToString() {
        final String configGlAttributeBuilderStr = ConfigGlAttribute.Builder.Companion.create().toString();
        Assertions.assertThat(configGlAttributeBuilderStr)
            .as("The toString of ConfigGlAttribute.Builder")
            .isNotNull()
            .isInstanceOf(String.class);
    }

}
