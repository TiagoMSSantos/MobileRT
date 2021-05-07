package puscas.mobilertapp;

import java.lang.annotation.Native;
import lombok.Builder;
import lombok.Getter;

/**
 * The configurator for the Ray Tracer engine.
 * <br>
 * Note that this class is used to pass data from JVM to Native code in C++ via JNI.
 */
@Builder
@Getter
public final class Config {

    /**
     * The scene.
     */
    @Native
    private final int scene;

    /**
     * The shader.
     */
    @Native
    private final int shader;

    /**
     * The accelerator.
     */
    @Native
    private final int accelerator;

    /**
     * The path to the OBJ file.
     */
    @Native
    @Builder.Default
    private final String objFilePath = "";

    /**
     * The path to the MAT file.
     */
    @Native
    @Builder.Default
    private final String matFilePath = "";

    /**
     * The path to the CAM file.
     */
    @Native
    @Builder.Default
    private final String camFilePath = "";

    /**
     * The configurator for the number of samples.
     */
    @Native
    @Builder.Default
    private final ConfigSamples configSamples = ConfigSamples.builder().build();

    /**
     * The configurator for the desired resolution.
     */
    @Native
    @Builder.Default
    private final ConfigResolution configResolution = ConfigResolution.builder().build();

    /**
     * The number of threads.
     */
    @Native
    private final int threads;

    /**
     * Whether the Ray Tracing engine should render a preview frame.
     */
    @Native
    private final boolean rasterize;

}
