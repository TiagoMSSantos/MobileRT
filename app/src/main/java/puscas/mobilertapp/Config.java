package puscas.mobilertapp;

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
     */
    private final int scene;

    /**
     * The shader.
     */
    private final int shader;

    /**
     * The accelerator.
     */
    private final int accelerator;

    /**
     * The path to the OBJ file.
     */
    @Builder.Default
    private final String objFilePath = "";

    /**
     * The path to the MAT file.
     */
    @Builder.Default
    private final String matFilePath = "";

    /**
     * The path to the CAM file.
     */
    @Builder.Default
    private final String camFilePath = "";

    /**
     * The configurator for the number of samples.
     */
    @Builder.Default
    private final ConfigSamples configSamples = ConfigSamples.builder().build();

    /**
     * The configurator for the desired resolution.
     */
    @Builder.Default
    private final ConfigResolution configResolution = ConfigResolution.builder().build();

    /**
     * The number of threads.
     */
    private final int threads;

    /**
     * Whether the Ray Tracing engine should render a preview frame.
     */
    private final boolean rasterize;

}
