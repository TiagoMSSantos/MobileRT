package puscas.mobilertapp.utils;

import lombok.extern.java.Log;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import puscas.mobilertapp.constants.Shader;

/**
 * The unit tests for the {@link Shader} util class.
 */
@Log
final class ShaderTest {

    /**
     * Tests that the {@link Shader#getNames()} method contains all the expected shaders.
     */
    @Test
    void testGetNames() {
        Assertions.assertThat(Shader.getNames())
        .as("Check available shaders.")
        .containsExactly(
            "NoShadows",
            "Whitted",
            "PathTracing",
            "DepthMap",
            "Diffuse"
        );
    }

}
