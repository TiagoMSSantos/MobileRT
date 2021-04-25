package puscas.mobilertapp;

import lombok.Builder;
import lombok.Getter;

/**
 * The configurator for the desired resolution in the {@link android.view.SurfaceView}.
 */
@Builder
@Getter
class ConfigResolutionView {

    /**
     * The width.
     */
    @Builder.Default
    private final int width = 1;

    /**
     * The height.
     */
    @Builder.Default
    private final int height = 1;
}
