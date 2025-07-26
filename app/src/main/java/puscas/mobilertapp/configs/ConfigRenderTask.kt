package puscas.mobilertapp.configs

import android.graphics.Bitmap
import android.widget.Button
import android.widget.TextView
import puscas.mobilertapp.DrawView
import puscas.mobilertapp.RenderTask
import puscas.mobilertapp.constants.State

/**
 * The configurator for the [RenderTask].
 *
 * @property requestRender  A [Runnable] to the [DrawView.requestRender] method which is called in the [RenderTask.timer].
 * @property finishRender   A [Runnable] method which stops the Ray Tracer engine and sets the [RenderTask.stateT] to [State.IDLE].
 * @property updateInterval The interval in `TimeUnit.MILLISECONDS` between each call to the [RenderTask.timer].
 * @property numLights      The number of lights in the scene.
 * @property resolution     The resolution of the [Bitmap] where the Ray Tracer engine will render the scene.
 * @property samples        The number of samples to be used by the Ray Tracing engine.
 * @property textView       The [TextView] which will output the debug information about the Ray Tracer engine.
 * @property numThreads     The number of threads to be used by the Ray Tracer engine.
 * @property numPrimitives  The number of primitives in the scene.
 * @property buttonRender   The [Button] which can start and stop the Ray Tracer engine.
 *   It is important to let the [RenderTask] update its state after the rendering process.
 */
@ConsistentCopyVisibility
data class ConfigRenderTask private constructor(
    val requestRender : Runnable,
    val finishRender : Runnable,
    val updateInterval : Long,
    val numLights : Long,
    val resolution : ConfigResolution,
    val samples : ConfigSamples,
    val textView : TextView,
    val numThreads : Int,
    val numPrimitives : Int,
    val buttonRender : Button,
) {

    init {
        require(updateInterval >= 0) { "The updateInterval must be >= 0." }
        require(numLights >= 0) { "The numLights must be >= 0." }
    }

    class Builder private constructor() {
        var requestRender : Runnable = Runnable { }
        var finishRender : Runnable = Runnable { }
        var updateInterval = 0L
        var numLights = 0L
        var resolution : ConfigResolution = ConfigResolution.Builder.create().build()
        var samples : ConfigSamples = ConfigSamples.Builder.create().build()
        lateinit var textView : TextView
        var numThreads = 0
        var numPrimitives = 0
        lateinit var buttonRender : Button

        companion object { fun create() = Builder() }

        fun build() = ConfigRenderTask(requestRender, finishRender, updateInterval, numLights, resolution, samples, textView, numThreads, numPrimitives, buttonRender)
    }
}
