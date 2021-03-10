package puscas.mobilertapp;

import java.util.logging.Logger;
import lombok.Builder;
import lombok.Getter;

/**
 * The configurator for the Ray Tracer engine.
 */
@Builder
@Getter
public final class Config {

    /**
     * The scene.
     *
     * @see Config#getScene()
     */
    private final int scene;

    /**
     * The shader.
     *
     * @see Config#getShader()
     */
    private final int shader;

    /**
     * The accelerator.
     *
     * @see Config#getAccelerator()
     */
    private final int accelerator;

    /**
     * The objFilePath.
     *
     * @see Config#getObjFilePath()
     */
    @Builder.Default
    private final String objFilePath = "";

    /**
     * The matFilePath.
     *
     * @see Config#getMatFilePath()
     */
    @Builder.Default
    private final String matFilePath = "";

    /**
     * The camFilePath.
     *
     * @see Config#getCamFilePath()
     */
    @Builder.Default
    private final String camFilePath = "";

    /**
     * The configSamples.
     *
     * @see Config#getConfigSamples
     */
    @Builder.Default
    private final ConfigSamples configSamples = ConfigSamples.builder().build();

    /**
     * The configResolution.
     *
     * @see Config#getConfigResolution()
     */
    @Builder.Default
    private final ConfigResolution configResolution = ConfigResolution.builder().build();

    /**
     * The number of threads.
     *
     * @see Config#getThreads()
     */
    private final int threads;

    /**
     * Whether the Ray Tracing engine should render a preview frame.
     *
     * @see Config#isRasterize()
     */
    private final boolean rasterize;

}
