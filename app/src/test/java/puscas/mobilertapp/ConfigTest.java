package puscas.mobilertapp;

import lombok.extern.java.Log;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import puscas.mobilertapp.configs.Config;
import puscas.mobilertapp.configs.ConfigResolution;
import puscas.mobilertapp.configs.ConfigSamples;
import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.constants.Shader;

/**
 * The test suite for {@link Config} class.
 */
@Log
public final class ConfigTest {

    /**
     * Test the building of {@link Config} with default values.
     */
    @Test
    public void testDefaultBuild() {
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
