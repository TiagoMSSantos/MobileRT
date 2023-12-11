package puscas.mobilertapp;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import kotlin.UninitializedPropertyAccessException;

/**
 * The test suite for the {@link RenderTask} class.
 */
public final class RenderTaskTest {

    /**
     * Tests the default constructor of {@link RenderTask}.
     */
    @Test
    public void testDefaultConstructor() {
        Assertions.assertThatThrownBy(() -> RenderTask.Builder.Companion.create().build())
            .as("The RenderTask#Builder#build shouldn't be possible with the default values")
            .isInstanceOf(UninitializedPropertyAccessException.class)
            .hasMessage("lateinit property config has not been initialized");
    }

    /**
     * Tests the {@link RenderTask.Builder#toString()} method in the builder class of
     * {@link RenderTask}.
     */
    @Test
    public void testRenderTaskBuilderToString() {
        final String renderTaskBuilderStr = RenderTask.Builder.Companion.create().toString();
        Assertions.assertThat(renderTaskBuilderStr)
            .as("The toString of RenderTask.Builder")
            .isNotNull()
            .isInstanceOf(String.class);
    }
}
