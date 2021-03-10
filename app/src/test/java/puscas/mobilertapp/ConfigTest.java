package puscas.mobilertapp;

import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import puscas.mobilertapp.utils.Accelerator;
import puscas.mobilertapp.utils.Scene;
import puscas.mobilertapp.utils.Shader;

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

        final Config config = Config.builder().build();
        Assertions.assertEquals(1, config.getConfigResolution().getWidth(),
            "Width not the expected value.");
        Assertions.assertEquals(1, config.getConfigResolution().getHeight(),
            "Height not the expected value.");
        Assertions.assertEquals(0, config.getScene(), "Scene not the expected value.");
        Assertions.assertEquals(0, config.getShader(), "Shader not the expected value.");
        Assertions.assertEquals(0, config.getAccelerator(), "Accelerator not the expected value.");
        Assertions.assertEquals(0, config.getConfigSamples().getSamplesPixel(),
            "Samples per pixel not the expected value.");
        Assertions.assertEquals(0, config.getConfigSamples().getSamplesLight(),
            "Samples per light not the expected value.");
        Assertions
            .assertEquals("", config.getObjFilePath(), "OBJ file path not the expected value.");
        Assertions
            .assertEquals("", config.getMatFilePath(), "MAT file path not the expected value.");
        Assertions
            .assertEquals("", config.getCamFilePath(), "CAM file path not the expected value.");
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
        final int scene = Scene.CORNELL.ordinal();
        final int shader = Shader.WHITTED.ordinal();
        final int accelerator = Accelerator.BVH.ordinal();
        final int spp = 6;
        final int spl = 7;
        final String obj = "abc";
        final String mat = "def";
        final String cam = "ghi";

        final Config config = Config.builder()
            .configResolution(
                ConfigResolution.builder()
                    .width(width)
                    .height(height)
                    .build()
            )
            .scene(scene)
            .shader(shader)
            .accelerator(accelerator)
            .configSamples(
                ConfigSamples.builder()
                    .samplesPixel(spp)
                    .samplesLight(spl)
                    .build()
            )
            .objFilePath(obj)
            .matFilePath(mat)
            .camFilePath(cam)
            .build();

        Assertions.assertEquals(width, config.getConfigResolution().getWidth(),
            "Width not the expected value.");
        Assertions.assertEquals(height, config.getConfigResolution().getHeight(),
            "Height not the expected value.");
        Assertions.assertEquals(scene, config.getScene(), "Scene not the expected value.");
        Assertions.assertEquals(shader, config.getShader(), "Shader not the expected value.");
        Assertions.assertEquals(accelerator, config.getAccelerator(),
            "Accelerator not the expected value.");
        Assertions.assertEquals(spp, config.getConfigSamples().getSamplesPixel(),
            "Samples per pixel not the expected value.");
        Assertions.assertEquals(spl, config.getConfigSamples().getSamplesLight(),
            "Samples per light not the expected value.");
        Assertions
            .assertEquals(obj, config.getObjFilePath(), "OBJ file path not the expected value.");
        Assertions
            .assertEquals(mat, config.getMatFilePath(), "MAT file path not the expected value.");
        Assertions
            .assertEquals(cam, config.getCamFilePath(), "CAM file path not the expected value.");
    }

}
