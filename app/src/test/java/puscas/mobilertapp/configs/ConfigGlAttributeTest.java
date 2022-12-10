package puscas.mobilertapp.configs;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * The test suite for {@link ConfigGlAttribute} class.
 */
public class ConfigGlAttributeTest {

    /**
     * Tests the {@link ConfigGlAttribute#builder()#toString()} method in the builder class of {@link Config}.
     */
    @Test
    public void testConfigGlAttributeBuilderToString() {
        final String configGlAttributeBuilderStr = ConfigGlAttribute.builder().toString();
        Assertions.assertThat(configGlAttributeBuilderStr)
            .as("The toString of ConfigGlAttribute.builder()")
            .isNotNull()
            .isInstanceOf(String.class);
    }

}
