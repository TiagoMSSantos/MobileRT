package puscas.mobilertapp.configs;

import android.widget.Button;
import android.widget.TextView;

import org.assertj.core.api.Assertions;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * The test suite for {@link ConfigRenderTask} class.
 */
public final class ConfigRenderTaskTest {

    /**
     * Tests the default constructor of {@link ConfigRenderTask}.
     */
    @Test
    public void testDefaultConfigRenderTask() {
        final ConfigRenderTask.Builder builder = ConfigRenderTask.Builder.Companion.create();
        final TextView mockedTextView = EasyMock.mock(TextView.class);
        final Button mockedButton = EasyMock.mock(Button.class);
        builder.setTextView(mockedTextView);
        builder.setButtonRender(mockedButton);
        final ConfigRenderTask configRenderTask = builder.build();
        Assertions.assertThat(configRenderTask)
            .as("The default constructor of ConfigRenderTask")
            .isNotNull()
            .isInstanceOf(ConfigRenderTask.class);
    }

    /**
     * Tests the {@link ConfigRenderTask.Builder#toString()} method in the builder class of {@link Config}.
     */
    @Test
    public void testConfigRenderTaskBuilderToString() {
        final String configRenderTaskBuilderStr = ConfigRenderTask.Builder.Companion.create().toString();
        Assertions.assertThat(configRenderTaskBuilderStr)
            .as("The toString of ConfigRenderTask.Builder")
            .isNotNull()
            .isInstanceOf(String.class);
    }

}
