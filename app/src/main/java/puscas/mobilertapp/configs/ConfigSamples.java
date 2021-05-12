package puscas.mobilertapp.configs;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * The configurator for the number of samples in the Ray Tracer engine.
 */
@Builder
@Getter
@ToString
public final class ConfigSamples {

    /**
     * The number of samples per pixel.
     */
    private final int samplesPixel;

    /**
     * The number of samples per light.
     */
    private final int samplesLight;

}
