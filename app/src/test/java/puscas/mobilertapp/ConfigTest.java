package puscas.mobilertapp;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import lombok.extern.java.Log;
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

        Assertions.assertThat(config.getConfigResolution().getWidth())
            .as("Width not the expected value.")
            .isOne();

        Assertions.assertThat(config.getConfigResolution().getHeight())
            .as("Height not the expected value.")
            .isOne();

        Assertions.assertThat(config.getScene())
            .as("Scene not the expected value.")
            .isZero();

        Assertions.assertThat(config.getShader())
            .as("Shader not the expected value.")
            .isZero();

        Assertions.assertThat(config.getAccelerator())
            .as("Accelerator not the expected value.")
            .isZero();

        Assertions.assertThat(config.getConfigSamples().getSamplesPixel())
            .as("Samples per pixel not the expected value.")
            .isZero();

        Assertions.assertThat(config.getConfigSamples().getSamplesLight())
            .as("Samples per light not the expected value.")
            .isZero();

        Assertions.assertThat(config.getObjFilePath())
            .as("OBJ file path not the expected value.")
            .isNotNull()
            .isEmpty();

        Assertions.assertThat(config.getMatFilePath())
            .as("MAT file path not the expected value.")
            .isNotNull()
            .isEmpty();

        Assertions.assertThat(config.getCamFilePath())
            .as("CAM file path not the expected value.")
            .isNotNull()
            .isEmpty();
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

        Assertions.assertThat(config.getConfigResolution().getWidth())
            .as("Width not the expected value.")
            .isEqualTo(width);

        Assertions.assertThat(config.getConfigResolution().getHeight())
            .as("Height not the expected value.")
            .isEqualTo(height);

        Assertions.assertThat(config.getScene())
            .as("Scene not the expected value.")
            .isEqualTo(scene);

        Assertions.assertThat(config.getShader())
            .as("Shader not the expected value.")
            .isEqualTo(shader);

        Assertions.assertThat(config.getAccelerator())
            .as("Accelerator not the expected value.")
            .isEqualTo(accelerator);

        Assertions.assertThat(config.getConfigSamples().getSamplesPixel())
            .as("Samples per pixel not the expected value.")
            .isEqualTo(spp);

        Assertions.assertThat(config.getConfigSamples().getSamplesLight())
            .as("Samples per light not the expected value.")
            .isEqualTo(spl);

        Assertions.assertThat(config.getObjFilePath())
            .as("OBJ file path not the expected value.")
            .isNotNull()
            .isEqualTo(obj);

        Assertions.assertThat(config.getMatFilePath())
            .as("MAT file path not the expected value.")
            .isNotNull()
            .isEqualTo(mat);

        Assertions.assertThat(config.getCamFilePath())
            .as("CAM file path not the expected value.")
            .isNotNull()
            .isEqualTo(cam);
    }

    /**
     * Tests the {@link Config#builder()#toString()} method in the builder class of {@link Config}.
     */
    @Test
    public void testConfigBuilderToString() {
        final String configBuilderStr = Config.builder().toString();
        org.assertj.core.api.Assertions.assertThat(configBuilderStr)
            .as("The toString of Config.builder()")
            .isNotNull()
            .isInstanceOf(String.class);
    }

}
