package puscas.mobilertapp;

import android.graphics.Bitmap;
import android.os.Debug;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;
import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.lang.ref.WeakReference;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.extern.java.Log;
import puscas.mobilertapp.configs.ConfigRenderTask;
import puscas.mobilertapp.constants.Constants;
import puscas.mobilertapp.constants.ConstantsMethods;
import puscas.mobilertapp.constants.ConstantsRenderer;
import puscas.mobilertapp.constants.ConstantsUI;
import puscas.mobilertapp.constants.State;
import puscas.mobilertapp.utils.AsyncTaskCoroutine;
import puscas.mobilertapp.utils.Utils;

/**
 * An asynchronous task to render a frame and update the {@link TextView} text.
 * At the end of the task, it sets the render {@link Button} to "Render".
 */
@Log
public final class RenderTask extends AsyncTaskCoroutine {

    /**
     * The number of milliseconds in a second.
     */
    private static final float MILLISECONDS_IN_SECOND = 1000.0F;

    /**
     * An {@link ExecutorService} which schedules every
     * {@link RenderTask#updateInterval} {@code TimeUnit.MILLISECONDS} the
     * {@link RenderTask#timer} {@link Runnable}.
     */
    private final ScheduledExecutorService executorService =
        Executors.newScheduledThreadPool(ConstantsRenderer.NUMBER_THREADS);

    /**
     * A {@link Runnable} to the {@link DrawView#requestRender} method which is
     * called in the {@link RenderTask#timer}.
     */
    private final Runnable requestRender;

    /**
     * A {@link Runnable} which updates the text to print in the {@link TextView}.
     */
    private final Runnable timer;

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
     * The timestamp of the start rendering process.
     */
    private final long startTimeStamp;

    /**
     * The selected number of samples per pixel.
     */
    private final String samplesPixelT;

    /**
     * The selected number of samples per light.
     */
    private final String samplesLightT;

    /**
     * The {@link TextView} which outputs debug information about the
     * Ray Tracer engine like the current rendering time and the fps.
     */
    private final WeakReference<TextView> textView;

    /**
     * The {@link Button} which starts and stops the rendering process.
     * This is needed in order to change its text at the end of the rendering
     * process.
     */
    private final WeakReference<Button> buttonRender;

    /**
     * The number of primitives and lights in the scene.
     */
    private final String primitivesT;

    /**
     * The number of threads in the Ray Tracer engine.
     */
    private final String threadsT;

    /**
     * The width and height of the {@link Bitmap} where the Ray Tracer engine is
     * rendering the scene.
     */
    private final String resolutionT;

    /**
     * The number of times that the {@link RenderTask#timer} is called.
     */
    private int frame = 0;

    /**
     * The timestamp at the start of every second.
     */
    private float timebase = 0.0F;

    /**
     * The number of times that the {@link RenderTask#timer} was called in a
     * second.
     */
    private float fps = 0.0F;

    /**
     * The current Ray Tracer engine {@link State}.
     */
    private String stateT = null;

    /**
     * The frames per second of the Ray Tracer engine.
     */
    private String fpsT = null;

    /**
     * The time, in seconds, spent constructing the Ray Tracer renderer.
     */
    private String timeFrameT = null;

    /**
     * The current time, in seconds, that the Ray Tracer engine spent rendering
     * a scene.
     */
    private String timeT = null;

    /**
     * A {@link String} containing the {@link RenderTask#fps} in order to print
     * it in the {@link RenderTask#textView}.
     */
    private String fpsRenderT = null;

    /**
     * The amount of allocated memory in the native heap (in MegaBytes).
     */
    private String allocatedT = null;

    /**
     * The current sample for all the pixels.
     */
    private String sampleT = null;

    /**
     * The {@link NumberFormat} to use when printing the {@link Float} values in the {@link TextView}.
     */
    private final NumberFormat formatter = NumberFormat.getInstance(Locale.US);

    /**
     * A private constructor of this class to force using the
     * RenderTask builder.
     *
     * @param config The configurator which contains all of the parameters.
     */
    @Builder
    private RenderTask(@NonNull final ConfigRenderTask config) {
        super();
        log.info("RenderTask");

        this.formatter.setMaximumFractionDigits(2);
        this.formatter.setMinimumFractionDigits(2);
        this.formatter.setRoundingMode(RoundingMode.HALF_UP);

        this.requestRender = config.getRequestRender();
        this.finishRender = config.getFinishRender();
        this.updateInterval = config.getUpdateInterval();
        this.primitivesT = ",p=" + config.getNumPrimitives() + ",l=" + config.getNumLights();
        this.resolutionT = ",r:" + config.getResolution().getWidth() + 'x' + config.getResolution().getHeight();
        this.threadsT = ",t:" + config.getNumThreads();
        this.samplesPixelT = ",spp:" + config.getSamples().getSamplesPixel();
        this.samplesLightT = ",spl:" + config.getSamples().getSamplesLight();
        this.buttonRender = new WeakReference<>(config.getButtonRender());
        this.textView = new WeakReference<>(config.getTextView());

        this.startTimeStamp = SystemClock.elapsedRealtime();
        resetTextStats();

        this.timer = () -> {
            log.info(ConstantsMethods.TIMER);
            updateFps();
            updateTextStats();

            final State currentState = State.values()[rtGetState()];
            this.stateT = currentState.toString();
            this.requestRender.run();
            publishProgress();

            if (currentState != State.BUSY) {
                this.executorService.shutdown();
            }

            log.info(ConstantsMethods.TIMER + ConstantsMethods.FINISHED);
        };

        checksArguments();
    }

    /**
     * Helper method that updates some statistics in the fields of this class
     * that will be presented in the {@link TextView}.
     */
    private void updateTextStats() {
        this.fpsT = "fps:" + this.formatter.format((double) rtGetFps());
        this.fpsRenderT = "[" + this.formatter.format((double) this.fps) + "]";
        this.timeFrameT = ",t:" + this.formatter.format((double) rtGetTimeRenderer() / (double) MILLISECONDS_IN_SECOND);
        final long currentTime = SystemClock.elapsedRealtime();
        this.timeT = "[" + this.formatter.format(
                (double) (currentTime - this.startTimeStamp) / (double) MILLISECONDS_IN_SECOND) + "]";
        this.allocatedT = ",m:" + Debug.getNativeHeapAllocatedSize() / (long) Constants.BYTES_IN_MEGABYTE + "mb";
        this.sampleT = "," + rtGetSample();
    }

    /**
     * Helper method that resets some statistics in the fields of this class
     * that will be presented in the {@link TextView}.
     */
    private void resetTextStats() {
        this.fpsT = "fps:" + this.formatter.format(0.0);
        this.fpsRenderT = "[" + this.formatter.format(0.0) + "]";
        this.timeFrameT = ",t:" + this.formatter.format(0.0);
        this.timeT = "[" + this.formatter.format(0.0) + "]";
        this.stateT = " " + State.IDLE.getId();
        this.allocatedT = ",m:" + Debug.getNativeHeapAllocatedSize() / (long) Constants.BYTES_IN_MEGABYTE + "mb";
        this.sampleT = ",0";
    }

    /**
     * Helper method that validates the fields of this class.
     */
    private void checksArguments() {
        Preconditions.checkNotNull(this.requestRender, "requestRender shouldn't be null");
        Preconditions.checkNotNull(this.finishRender, "finishRender shouldn't be null");
        Preconditions.checkNotNull(this.textView, "textView shouldn't be null");
        Preconditions.checkNotNull(this.buttonRender, "buttonRender shouldn't be null");
    }

    /**
     * Gets the number of frames per second which the Ray Tracer engine could
     * render the scene.
     *
     * @return The number of frames per second.
     */
    private native float rtGetFps();

    /**
     * Gets the time, in milliseconds, spent constructing the Ray Tracer
     * renderer.
     *
     * @return The time spent constructing the Ray Tracer renderer.
     */
    private native long rtGetTimeRenderer();

    /**
     * Gets the current sample for all the pixels.
     *
     * @return The current sample for all the pixels.
     */
    private native int rtGetSample();

    /**
     * Gets an {@code int} which represents the current Ray Tracer engine
     * {@link State}.
     *
     * @return The current Ray Tracer engine {@link State}.
     */
    native int rtGetState();

    /**
     * Auxiliary method which calculates the number of times
     * {@link RenderTask#timer} was called and in each second.
     */
    private void updateFps() {
        this.frame++;
        final float time = (float) SystemClock.elapsedRealtime();
        this.fps = ((float) this.frame * MILLISECONDS_IN_SECOND) / (time - this.timebase);
        if ((time - this.timebase) > MILLISECONDS_IN_SECOND) {
            this.timebase = time;
            this.frame = 0;
        }
    }

    /**
     * Auxiliary method which sets the current debug information in the
     * {@link RenderTask#textView}.
     */
    private void printText() {
        final String aux =
            this.fpsT + this.fpsRenderT + this.resolutionT + this.threadsT + this.samplesPixelT
                + this.samplesLightT + this.sampleT + ConstantsUI.LINE_SEPARATOR
                + this.stateT + this.allocatedT + this.timeFrameT + this.timeT + this.primitivesT;
        this.textView.get().setText(aux);
    }

    @Override
    protected void onPreExecute() {
        log.info("onPreExecute");
    }

    @Override
    protected void doInBackground() {
        log.info("doInBackground");

        this.executorService.scheduleAtFixedRate(this.timer, 0L,
            this.updateInterval, TimeUnit.MILLISECONDS);
        Utils.waitExecutorToFinish(this.executorService);

        final String message = "doInBackground" + ConstantsMethods.FINISHED;
        log.info(message);
    }

    @Override
    protected void onProgressUpdate() {
        log.info("onProgressUpdate");
        printText();

        final String message = "onProgressUpdate" + ConstantsMethods.FINISHED;
        log.info(message);
    }

    @Override
    protected void onPostExecute() {
        log.info("onPostExecute");

        printText();
        this.requestRender.run();

        MainActivity.resetErrno();
        this.finishRender.run();

        this.buttonRender.get().setText(R.string.render);

        final String message = "onPostExecute" + ConstantsMethods.FINISHED;
        log.info(message);
    }

}
