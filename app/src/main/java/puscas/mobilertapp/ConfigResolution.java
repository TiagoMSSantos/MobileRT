package puscas.mobilertapp;

import lombok.Builder;
import lombok.Getter;

/**
 * The configurator for the desired resolution in the Ray Tracer engine.
 */
@Builder
@Getter
public final class ConfigResolution {

    /**
     * The width.
     *
     * @see ConfigResolution#getWidth()
     */
    @Builder.Default
    private final int width = 1;

    /**
     * The height.
     *
     * @see ConfigResolution#getHeight()
     */
    @Builder.Default
    private final int height = 1;

}
