package puscas.mobilertapp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.logging.Logger;

/**
 * The test suite for {@link Config} class.
 */
public final class ConfigTest {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(ConfigTest.class.getName());

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
     * Test the building of {@link Config} with default values.
     */
    @Test
    public void testDefaultBuild() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final Config config = new Config.Builder().build();
        Assertions.assertEquals(0, config.getWidth(), "Width not the expected value.");
        Assertions.assertEquals(0, config.getHeight(), "Height not the expected value.");
        Assertions.assertEquals(0, config.getScene(), "Scene not the expected value.");
        Assertions.assertEquals(0, config.getShader(), "Shader not the expected value.");
        Assertions.assertEquals(0, config.getAccelerator(), "Accelerator not the expected value.");
        Assertions.assertEquals(0, config.getSamplesPixel(), "Samples per pixel not the expected value.");
        Assertions.assertEquals(0, config.getSamplesLight(), "Samples per light not the expected value.");
        Assertions.assertEquals("", config.getObjFilePath(), "OBJ file path not the expected value.");
        Assertions.assertEquals("", config.getMatFilePath(), "MAT file path not the expected value.");
        Assertions.assertEquals("", config.getCamFilePath(), "CAM file path not the expected value.");
    }

    /**
     * Test the building of {@link Config} with valid values.
     */
    @Test
    public void testBuild() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final int width = 1;
        final int height = 2;
        final int scene = 3;
        final int shader = 4;
        final int accelerator = 5;
        final int spp = 6;
        final int spl = 7;
        final String obj = "abc";
        final String mat = "def";
        final String cam = "ghi";

        final Config config = new Config.Builder()
            .withWidth(width)
            .withHeight(height)
            .withScene(scene)
            .withShader(shader)
            .withAccelerator(accelerator)
            .withSamplesPixel(spp)
            .withSamplesLight(spl)
            .withOBJ(obj)
            .withMAT(mat)
            .withCAM(cam)
            .build();

        Assertions.assertEquals(width, config.getWidth(), "Width not the expected value.");
        Assertions.assertEquals(height, config.getHeight(), "Height not the expected value.");
        Assertions.assertEquals(scene, config.getScene(), "Scene not the expected value.");
        Assertions.assertEquals(shader, config.getShader(), "Shader not the expected value.");
        Assertions.assertEquals(accelerator, config.getAccelerator(), "Accelerator not the expected value.");
        Assertions.assertEquals(spp, config.getSamplesPixel(), "Samples per pixel not the expected value.");
        Assertions.assertEquals(spl, config.getSamplesLight(), "Samples per light not the expected value.");
        Assertions.assertEquals(obj, config.getObjFilePath(), "OBJ file path not the expected value.");
        Assertions.assertEquals(mat, config.getMatFilePath(), "MAT file path not the expected value.");
        Assertions.assertEquals(cam, config.getCamFilePath(), "CAM file path not the expected value.");
    }
}
