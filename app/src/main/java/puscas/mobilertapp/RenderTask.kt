package puscas.mobilertapp

import android.graphics.Bitmap
import android.os.Debug
import android.os.SystemClock
import android.widget.Button
import android.widget.TextView
import com.google.common.base.Preconditions
import puscas.mobilertapp.configs.ConfigRenderTask
import puscas.mobilertapp.constants.Constants
import puscas.mobilertapp.constants.ConstantsMethods
import puscas.mobilertapp.constants.ConstantsRenderer
import puscas.mobilertapp.constants.ConstantsUI
import puscas.mobilertapp.constants.State
import puscas.mobilertapp.utils.AsyncTaskCoroutine
import puscas.mobilertapp.utils.Utils
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * An asynchronous task to render a frame and update the [TextView] text.
 * At the end of the task, it sets the render [Button] to "Render".
 *
 * @property requestRender  A [Runnable] to the [DrawView.requestRender] method which is used in the [RenderTask.timer].
 * @property finishRender   A [Runnable] method which stops the Ray Tracer engine and sets the [RenderTask.stateT] to [State.IDLE].
 * @property updateInterval The interval in `TimeUnit.MILLISECONDS` between each call to the [RenderTask.timer] [Runnable].
 * @property primitivesT    The number of primitives and lights in the scene.
 * @property resolutionT    The width and height of the [Bitmap] where the Ray Tracer engine is rendering the scene.
 * @property threadsT       The number of threads in the Ray Tracer engine.
 * @property samplesPixelT  The selected number of samples per pixel.
 * @property samplesLightT  The selected number of samples per light.
 * @property textView       The [TextView] which outputs debug information about the Ray Tracer engine like the current rendering time and the fps.
 * @property buttonRender   The [Button] which starts and stops the rendering process. This is needed in order to change its text at the end of the rendering process.
 */
class RenderTask private constructor(
    private val requestRender : Runnable,
    private val finishRender: Runnable,
    private val updateInterval: Long,
    private val primitivesT: String,
    private val resolutionT: String,
    private val threadsT: String,
    private val samplesPixelT: String,
    private val samplesLightT: String,
    private val textView: TextView,
    private val buttonRender: Button,
) : AsyncTaskCoroutine() {

    /**
     * Logger for this class.
     */
    private val logger = Logger.getLogger(RenderTask::class.java.simpleName)

    /**
     * The number of milliseconds in a second.
     */
    private val millisecondsInSecond = 1000.0f

    /**
     * An [ExecutorService] which schedules every
     * [RenderTask.updateInterval] `TimeUnit.MILLISECONDS` the
     * [RenderTask.timer] [Runnable].
     */
    private val executorService = Executors.newScheduledThreadPool(ConstantsRenderer.NUMBER_THREADS)

    /**
     * A [Runnable] which updates the text to print in the [TextView].
     */
    private val timer: Runnable

    /**
     * The timestamp of the start rendering process.
     */
    private val startTimeStamp: Long

    /**
     * The number of times that the [RenderTask.timer] is called.
     */
    private var frame = 0

    /**
     * The timestamp at the start of every second.
     */
    private var timebase = 0.0f

    /**
     * The number of times that the [RenderTask.timer] was called in a
     * second.
     */
    private var fps = 0.0f

    /**
     * The current Ray Tracer engine [State].
     */
    private var stateT: String? = null

    /**
     * The frames per second of the Ray Tracer engine.
     */
    private var fpsT: String? = null

    /**
     * The time, in seconds, spent constructing the Ray Tracer renderer.
     */
    private var timeFrameT: String? = null

    /**
     * The current time, in seconds, that the Ray Tracer engine spent rendering
     * a scene.
     */
    private var timeT: String? = null

    /**
     * A [String] containing the [RenderTask.fps] in order to print
     * it in the [RenderTask.textView].
     */
    private var fpsRenderT: String? = null

    /**
     * The amount of allocated memory in the native heap (in MegaBytes).
     */
    private var allocatedT: String? = null

    /**
     * The current sample for all the pixels.
     */
    private var sampleT: String? = null

    /**
     * The [NumberFormat] to use when printing the [Float] values in the [TextView].
     */
    private val formatter = NumberFormat.getInstance(Locale.US)

    /**
     * A private constructor of this class to force using the
     * RenderTask builder.
     */
    init {
        logger.info("RenderTask")
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = 2
        formatter.roundingMode = RoundingMode.HALF_UP
        startTimeStamp = SystemClock.elapsedRealtime()
        resetTextStats()
        timer = Runnable {
            logger.info(ConstantsMethods.TIMER)
            updateFps()
            updateTextStats()
            val currentState = State.values()[rtGetState()]
            stateT = currentState.toString()
            requestRender.run()
            publishProgressAsync()
            if (currentState != State.BUSY) {
                logger.info("Stopping RenderTask")
                executorService.shutdown()
                logger.info("Stopped RenderTask")
            }
            logger.info(ConstantsMethods.TIMER + ConstantsMethods.FINISHED)
        }
        checksArguments()
    }

    /**
     * Helper method that updates some statistics in the fields of this class
     * that will be presented in the [TextView].
     */
    private fun updateTextStats() {
        fpsT = "fps:" + formatter.format(rtGetFps().toDouble())
        fpsRenderT = "[" + formatter.format(fps.toDouble()) + "]"
        timeFrameT =
            ",t:" + formatter.format(rtGetTimeRenderer().toDouble() / millisecondsInSecond.toDouble())
        val currentTime = SystemClock.elapsedRealtime()
        timeT = "[" + formatter.format(
            (currentTime - startTimeStamp).toDouble() / millisecondsInSecond.toDouble()
        ) + "]"
        allocatedT = ",m:" + Debug.getNativeHeapAllocatedSize() / Constants.BYTES_IN_MEGABYTE + "mb"
        sampleT = "," + rtGetSample()
    }

    /**
     * Helper method that resets some statistics in the fields of this class
     * that will be presented in the [TextView].
     */
    private fun resetTextStats() {
        fpsT = "fps:" + formatter.format(0.0)
        fpsRenderT = "[" + formatter.format(0.0) + "]"
        timeFrameT = ",t:" + formatter.format(0.0)
        timeT = "[" + formatter.format(0.0) + "]"
        stateT = " " + State.IDLE.id
        allocatedT = ",m:" + Debug.getNativeHeapAllocatedSize() / Constants.BYTES_IN_MEGABYTE + "mb"
        sampleT = ",0"
    }

    /**
     * Helper method that validates the fields of this class.
     */
    private fun checksArguments() {
        Preconditions.checkNotNull(requestRender, "requestRender shouldn't be null")
        Preconditions.checkNotNull(finishRender, "finishRender shouldn't be null")
        Preconditions.checkNotNull(textView, "textView shouldn't be null")
        Preconditions.checkNotNull(buttonRender, "buttonRender shouldn't be null")
    }

    /**
     * Gets the number of frames per second which the Ray Tracer engine could
     * render the scene.
     *
     * @return The number of frames per second.
     */
    private external fun rtGetFps(): Float

    /**
     * Gets the time, in milliseconds, spent constructing the Ray Tracer
     * renderer.
     *
     * @return The time spent constructing the Ray Tracer renderer.
     */
    private external fun rtGetTimeRenderer(): Long

    /**
     * Gets the current sample for all the pixels.
     *
     * @return The current sample for all the pixels.
     */
    private external fun rtGetSample(): Int

    /**
     * Gets an `int` which represents the current Ray Tracer engine
     * [State].
     *
     * @return The current Ray Tracer engine [State].
     */
    external fun rtGetState(): Int

    /**
     * Auxiliary method which calculates the number of times
     * [RenderTask.timer] was called and in each second.
     */
    private fun updateFps() {
        frame++
        val time = SystemClock.elapsedRealtime().toFloat()
        fps = frame * millisecondsInSecond / (time - timebase)
        if (time - timebase > millisecondsInSecond) {
            timebase = time
            frame = 0
        }
    }

    /**
     * Auxiliary method which sets the current debug information in the
     * [RenderTask.textView].
     */
    private fun printText() {
        val aux = (fpsT + fpsRenderT + resolutionT + threadsT + samplesPixelT
                + samplesLightT + sampleT + ConstantsUI.LINE_SEPARATOR
                + stateT + allocatedT + timeFrameT + timeT + primitivesT)
        textView.text = aux
    }

    override fun onPreExecute() {
        logger.info("onPreExecute")
    }

    override fun doInBackground() {
        logger.info("doInBackground")
        executorService.scheduleAtFixedRate(
            timer, 0L,
            updateInterval, TimeUnit.MILLISECONDS
        )
        Utils.waitExecutorToFinish(executorService)
        val message = "doInBackground" + ConstantsMethods.FINISHED
        logger.info(message)
    }

    override fun onProgressUpdate() {
        logger.info("onProgressUpdate")
        printText()
        val message = "onProgressUpdate" + ConstantsMethods.FINISHED
        logger.info(message)
    }

    override fun onPostExecute() {
        logger.info("onPostExecute")
        printText()
        requestRender.run()
        MainActivity.resetErrno()
        finishRender.run()
        buttonRender.setText(R.string.render)
        val message = "onPostExecute" + ConstantsMethods.FINISHED
        logger.info(message)
    }

    class Builder private constructor() {
        lateinit var config : ConfigRenderTask

        companion object { fun create() = Builder() }

        fun build() = RenderTask(
            config.requestRender,
            config.finishRender,
            config.updateInterval,
            ",p=" + config.numPrimitives + ",l=" + config.numLights,
            ",r:" + config.resolution.width + 'x' + config.resolution.height,
            ",t:" + config.numThreads,
            ",spp:" + config.samples.samplesPixel,
            ",spl:" + config.samples.samplesLight,
            config.textView,
            config.buttonRender,
        )
    }
}
