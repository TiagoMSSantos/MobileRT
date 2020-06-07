package puscas.mobilertapp.utils;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * The unit tests for the {@link Shader} util class.
 */
public class ShaderTest {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(ShaderTest.class.getName());

    /**
     * Setup method called before each test.
     */
    @Before
    public void setUp() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Tests that the {@link Shader#getNames()} method contains all the expected shaders.
     */
    @Test
    public void testGetNames() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        Assertions.assertThat(Shader.getNames()).containsExactly(
            "NoShadows",
            "Whitted",
            "PathTracer",
            "DepthMap",
            "Diffuse"
        );
    }
}
