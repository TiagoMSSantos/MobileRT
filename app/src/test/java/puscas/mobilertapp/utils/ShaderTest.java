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
        LOGGER.info(Constants.SET_UP);
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        LOGGER.info(Constants.TEAR_DOWN);
    }

    /**
     * Tests that the {@link Shader#getNames()} method contains all the expected shaders.
     */
    @Test
    public void testGetNames() {
        LOGGER.info(Constants.TEST_GET_NAMES);
        Assertions.assertThat(Shader.getNames()).containsExactly(
            "NoShadows",
            "Whitted",
            "PathTracer",
            "DepthMap",
            "Diffuse"
        );
    }
}
