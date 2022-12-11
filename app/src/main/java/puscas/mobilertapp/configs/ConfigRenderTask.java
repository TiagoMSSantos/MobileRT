package puscas.mobilertapp.configs;

import android.graphics.Bitmap;
import android.widget.Button;
import android.widget.TextView;

import lombok.Builder;
import lombok.Getter;
import puscas.mobilertapp.DrawView;
import puscas.mobilertapp.RenderTask;
import puscas.mobilertapp.constants.State;

/**
 * The configurator for the {@link RenderTask}.
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
     * The resolution of the {@link Bitmap} where the Ray Tracer engine will render
     * the scene.
     */
    @Builder.Default
    private final ConfigResolution resolution = ConfigResolution.builder().build();

    /**
     * The number of samples to be used by the Ray Tracing engine.
     */
    @Builder.Default
    private final ConfigSamples samples = ConfigSamples.builder().build();

    /**
     * The {@link TextView} which will output the debug information about the
     * Ray Tracer engine.
     */
    private final TextView textView ;

    /**
     * The number of threads to be used by the Ray Tracer engine.
     */
    private final int numThreads;

    /**
     * The number of primitives in the scene.
     */
    private final int numPrimitives;

    /**
     * The {@link Button} which can start and stop the Ray Tracer engine.
     * It is important to let the {@link RenderTask} update its state after the
     * rendering process.
     */
    private final Button buttonRender;
}
