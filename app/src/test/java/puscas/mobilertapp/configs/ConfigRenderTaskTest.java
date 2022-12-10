package puscas.mobilertapp.configs;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * The test suite for {@link ConfigRenderTask} class.
 */
public class ConfigRenderTaskTest {

    /**
     * Tests the default constructor of {@link ConfigRenderTask}.
     */
    @Test
    public void testDefaultConfigRenderTask() {
        final ConfigRenderTask configRenderTask = ConfigRenderTask.builder().build();
        Assertions.assertThat(configRenderTask)
            .as("The default constructor of ConfigRenderTask")
            .isNotNull()
            .isInstanceOf(ConfigRenderTask.class);
    }

    /**
     * Tests the {@link ConfigRenderTask#builder()#toString()} method in the builder class of {@link Config}.
     */
    @Test
    public void testConfigRenderTaskBuilderToString() {
        final String configRenderTaskBuilderStr = ConfigRenderTask.builder().toString();
        Assertions.assertThat(configRenderTaskBuilderStr)
            .as("The toString of ConfigRenderTask.builder()")
            .isNotNull()
            .isInstanceOf(String.class);
    }

}
