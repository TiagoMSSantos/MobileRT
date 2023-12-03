package puscas.mobilertapp.configs;

import android.widget.Button;
import android.widget.TextView;

import org.assertj.core.api.Assertions;
import org.easymock.EasyMock;
import org.junit.Test;

import kotlin.UninitializedPropertyAccessException;

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
        Assertions.assertThatThrownBy(() -> ConfigRenderTask.Builder.Companion.create().build())
            .as("The ConfigRenderTask#Builder#build shouldn't be possible with the default values")
            .isInstanceOf(UninitializedPropertyAccessException.class);

        final TextView mockedTextView = EasyMock.mock(TextView.class);
        final Button mockedButton = EasyMock.mock(Button.class);
        builder.setTextView(mockedTextView);
        builder.setButtonRender(mockedButton);
        final ConfigRenderTask configRenderTask = builder.build();

        Assertions.assertThat(configRenderTask.getUpdateInterval())
            .as("UpdateInterval not the expected value.")
            .isZero();
        Assertions.assertThat(configRenderTask.getNumLights())
            .as("NumLights not the expected value.")
            .isZero();
        Assertions.assertThat(configRenderTask.getResolution())
            .as("Resolution not the expected value.")
            .isEqualTo(ConfigResolution.Builder.Companion.create().build());
        Assertions.assertThat(configRenderTask.getSamples())
            .as("Samples not the expected value.")
            .isEqualTo(ConfigSamples.Builder.Companion.create().build());
        Assertions.assertThat(configRenderTask.getTextView())
            .as("TextView not the expected value.")
            .isSameAs(mockedTextView);
        Assertions.assertThat(configRenderTask.getNumThreads())
            .as("NumThreads not the expected value.")
            .isZero();
        Assertions.assertThat(configRenderTask.getNumPrimitives())
            .as("NumPrimitives not the expected value.")
            .isZero();
        Assertions.assertThat(configRenderTask.getButtonRender())
            .as("ButtonRender not the expected value.")
            .isSameAs(mockedButton);
    }

    /**
     * Tests the building of {@link ConfigRenderTask} with valid values.
     */
    @Test
    public void testBuildConfigGlAttribute() {
        final ConfigRenderTask.Builder builder = ConfigRenderTask.Builder.Companion.create();

        final int updateInterval = 123;
        final int numLights = 234;
        final ConfigResolution.Builder resolutionBuilder = ConfigResolution.Builder.Companion.create();
        resolutionBuilder.setHeight(123);
        resolutionBuilder.setWidth(345);
        final ConfigSamples.Builder samplesBuilder = ConfigSamples.Builder.Companion.create();
        samplesBuilder.setSamplesLight(234);
        samplesBuilder.setSamplesPixel(456);
        final TextView mockedTextView = EasyMock.mock(TextView.class);
        final Button mockedButton = EasyMock.mock(Button.class);
        final int numThreads = 134;
        final int numPrimitives = 567;
        builder.setUpdateInterval(updateInterval);
        builder.setNumLights(numLights);
        builder.setResolution(resolutionBuilder.build());
        builder.setSamples(samplesBuilder.build());
        builder.setTextView(mockedTextView);
        builder.setButtonRender(mockedButton);
        builder.setNumThreads(numThreads);
        builder.setNumPrimitives(numPrimitives);
        final ConfigRenderTask configRenderTask = builder.build();

        Assertions.assertThat(configRenderTask.getUpdateInterval())
            .as("UpdateInterval not the expected value.")
            .isEqualTo(updateInterval);
        Assertions.assertThat(configRenderTask.getNumLights())
            .as("NumLights not the expected value.")
            .isEqualTo(numLights);
        Assertions.assertThat(configRenderTask.getResolution())
            .as("Resolution not the expected value.")
            .isEqualTo(resolutionBuilder.build());
        Assertions.assertThat(configRenderTask.getSamples())
            .as("Samples not the expected value.")
            .isEqualTo(samplesBuilder.build());
        Assertions.assertThat(configRenderTask.getTextView())
            .as("TextView not the expected value.")
            .isSameAs(mockedTextView);
        Assertions.assertThat(configRenderTask.getNumThreads())
            .as("NumThreads not the expected value.")
            .isEqualTo(numThreads);
        Assertions.assertThat(configRenderTask.getNumPrimitives())
            .as("NumPrimitives not the expected value.")
            .isEqualTo(numPrimitives);
        Assertions.assertThat(configRenderTask.getButtonRender())
            .as("ButtonRender not the expected value.")
            .isSameAs(mockedButton);
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
