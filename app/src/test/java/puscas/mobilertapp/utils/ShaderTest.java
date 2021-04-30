package puscas.mobilertapp.utils;

import lombok.extern.java.Log;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * The unit tests for the {@link Shader} util class.
 */
@Log
public final class ShaderTest {

    /**
     * Tests that the {@link Shader#getNames()} method contains all the expected shaders.
     */
    @Test
    public void testGetNames() {
        Assertions.assertThat(Shader.getNames()).containsExactly(
            "NoShadows",
            "Whitted",
            "PathTracing",
            "DepthMap",
            "Diffuse"
        );
    }

}
