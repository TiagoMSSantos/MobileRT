package puscas.mobilertapp;

import android.graphics.Bitmap;
import android.widget.TextView;
import lombok.Builder;
import lombok.Getter;
import puscas.mobilertapp.utils.State;

/**
 * The configurator for the Ray Tracer engine.
 */
@Builder
@Getter
public final class ConfigRenderTask {

    /**
     * A {@link Runnable} to the {@link DrawView#requestRender} method which is
     * called in the {@link RenderTask#timer}.
     */
    private final Runnable requestRender;

    /**
     * A {@link Runnable} method which stops the Ray Tracer engine and sets the
     * {@link RenderTask#stateT} to {@link State#IDLE}.
     */
    private final Runnable finishRender;

    /**
     * The interval in {@code TimeUnit.MILLISECONDS} between each call to the
     * {@link RenderTask#timer} {@link Runnable}.
     */
    private final long updateInterval;

    /**
     * The number of lights in the scene.
     */
    private final int numLights;

    /**
     * The width of the {@link Bitmap} where the Ray Tracer engine will render
     * the scene.
     */
    private final int width;

    /**
     * The height of the {@link Bitmap} where the Ray Tracer engine will render
     * the scene.
     */
    private final int height;

    /**
     * The number of samples per pixel.
     */
    private final int samplesPixel;

    /**
     * The number of samples per light.
     */
    private final int samplesLight;

    /**
     * The {@link TextView} which will output the debug information about the
     * Ray Tracer engine.
     */
    private final TextView textView ;
}
