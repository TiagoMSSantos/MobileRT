package puscas.mobilertapp;

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
     * The samplesPixel.
     *
     * @see ConfigSamples#getSamplesPixel()
     */
    private final int samplesPixel;

    /**
     * The samplesLight.
     *
     * @see ConfigSamples#getSamplesLight()
     */
    private final int samplesLight;

}
