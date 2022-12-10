package puscas.mobilertapp;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * The test suite for the {@link RenderTask} class.
 */
public class RenderTaskTest {

    /**
     * Tests the {@link RenderTask#builder()#toString()} method in the builder class of
     * {@link RenderTask}.
     */
    @Test
    public void testRenderTaskBuilderToString() {
        final String renderTaskBuilderStr = RenderTask.builder().toString();
        Assertions.assertThat(renderTaskBuilderStr)
            .as("The toString of RenderTask.builder()")
            .isNotNull()
            .isInstanceOf(String.class);
    }
}
