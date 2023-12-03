package puscas.mobilertapp;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * The test suite for the {@link RenderTask} class.
 */
public final class RenderTaskTest {

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
