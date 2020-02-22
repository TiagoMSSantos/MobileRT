package puscas.mobilertapp;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static puscas.mobilertapp.ConstantsMethods.ON_CANCELLED;
import static puscas.mobilertapp.ConstantsRenderer.NUMBER_THREADS;

/**
 * An asynchronous task to render a frame and update the {@link TextView} text.
 * At the end of the task, it sets the render {@link Button} to "Render".
 */
final class RenderTask extends AsyncTask<Void, Void, Void> {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(RenderTask.class.getName());

    /**
     * The number of milliseconds in a second.
     */
    private static final float SECOND_IN_MS = 1000.0F;

    /**
     * The number of bytes in a mega byte.
     */
    private static final long MB_IN_BYTES = 1048576L;

    /**
     * An {@link ExecutorService} which schedules every {@link RenderTask#updateInterval}
     * {@code TimeUnit.MILLISECONDS} the {@link RenderTask#timer} {@link Runnable}.
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(NUMBER_THREADS);

    /**
     * A {@link Runnable} to the {@link DrawView#requestRender} method which is called in the {@link RenderTask#timer}.
     */
    private final Runnable requestRender;

    /**
     * A {@link Runnable} which updates the text to print in the {@link TextView}.
     */
    private final Runnable timer;

    /**
     * A {@link Runnable} to the {@link MainRenderer#RTFinishRender} method which stops the Ray Tracer engine and sets
     * the {@link RenderTask#stateT} to {@link State#IDLE}.
     */
    private final Runnable finishRender;

    /**
     * The interval in {@code TimeUnit.MILLISECONDS} between each call to the {@link RenderTask#timer}
     * {@link Runnable}.
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
     * The {@link TextView} which outputs debug information about the Ray Tracer engine like the current rendering
     * time and the fps.
     */
    private final WeakReference<TextView> textView;

    /**
     * The {@link Button} which starts and stops the rendering process.
     * This is needed in order to change its text at the end of the rendering process.
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
     * The width and height of the {@link Bitmap} where the Ray Tracer engine is rendering the scene.
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
     * The number of times that the {@link RenderTask#timer} was called in a second.
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
     * The current time, in seconds, that the Ray Tracer engine spent rendering a scene.
     */
    private String timeT = null;

    /**
     * A {@link String} containing the {@link RenderTask#fps} in order to print it in the {@link RenderTask#textView}.
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
     * A private constructor of this class to force using the {@link RenderTask.Builder}.
     *
     * @param builder The builder which contains all the parameters.
     */
    private RenderTask(final RenderTask.Builder builder) {
        super();
        LOGGER.info("RenderTask");

        this.requestRender = builder.getRequestRender();
        this.finishRender = builder.getFinishRender();
        this.updateInterval = builder.getUpdateInterval();
        this.primitivesT = ",p=" + builder.getNumPrimitives() + ",l=" + builder.getNumLights();
        this.resolutionT = ",r:" + builder.getWidth() + 'x' + builder.getHeight();
        this.threadsT = ",t:" + builder.getNumThreads();
        this.samplesPixelT = ",spp:" + builder.getSamplesPixel();
        this.samplesLightT = ",spl:" + builder.getSamplesLight();
        this.buttonRender = new WeakReference<>(builder.getButtonRender());
        this.textView = new WeakReference<>(builder.getTextView());

        this.startTimeStamp = SystemClock.elapsedRealtime();
        this.fpsT = String.format(Locale.US, "fps:%.2f", 0.0F);
        this.fpsRenderT = String.format(Locale.US, "[%.2f]", 0.0F);
        this.timeFrameT = String.format(Locale.US, ",t:%.2fs", 0.0F);
        this.timeT = String.format(Locale.US, "[%.2fs]", 0.0F);
        this.stateT = " " + State.IDLE.getId();
        this.allocatedT = ",m:" + Debug.getNativeHeapAllocatedSize() / MB_IN_BYTES + "mb";
        this.sampleT = ",0";

        this.timer = () -> {
            updateFps();

            this.fpsT = String.format(Locale.US, "fps:%.1f", RTGetFps());
            this.fpsRenderT = String.format(Locale.US, "[%.1f]", this.fps);
            final long timeRenderer = RTGetTimeRenderer();
            this.timeFrameT = String.format(Locale.US, ",t:%.2fs", (float) timeRenderer / SECOND_IN_MS);
            final long currentTime = SystemClock.elapsedRealtime();
            this.timeT = String.format(Locale.US, "[%.2fs]",(float) (currentTime - this.startTimeStamp) / SECOND_IN_MS);
            this.allocatedT = ",m:" + Debug.getNativeHeapAllocatedSize() / MB_IN_BYTES + "mb";
            this.sampleT = "," + RTGetSample();

            final State currentState = State.values()[RTGetState()];
            this.stateT = currentState.toString();
            this.requestRender.run();
            publishProgress();
            if (currentState != State.BUSY) {
                this.scheduler.shutdown();
            }
        };
    }

    /**
     * Gets the number of frames per second which the Ray Tracer engine could render the scene.
     *
     * @return The number of frames per second.
     */
    native float RTGetFps();

    /**
     * Gets the time, in milliseconds, spent constructing the Ray Tracer renderer.
     *
     * @return The time spent constructing the Ray Tracer renderer.
     */
    native long RTGetTimeRenderer();

    /**
     * Gets the current sample for all the pixels.
     *
     * @return The current sample for all the pixels.
     */
    native int RTGetSample();

    /**
     * Gets an {@code int} which represents the current Ray Tracer engine {@link State}.
     *
     * @return The current Ray Tracer engine {@link State}.
     */
    native int RTGetState();

    /**
     * Auxiliary method which calculates the number of times {@link RenderTask#timer} was called and in each second.
     */
    private void updateFps() {
        this.frame++;
        final float time = (float) SystemClock.elapsedRealtime();
        final float oneSecond = SECOND_IN_MS;
        if ((time - this.timebase) > oneSecond) {
            this.fps = ((float) this.frame * oneSecond) / (time - this.timebase);
            this.timebase = time;
            this.frame = 0;
        }
    }

    /**
     * Auxiliary method which sets the current debug information in the {@link RenderTask#textView}.
     */
    private void printText() {
        final String aux = this.fpsT + this.fpsRenderT + this.resolutionT + this.threadsT + this.samplesPixelT +
                this.samplesLightT + this.sampleT + System.getProperty("line.separator") +
                this.stateT + this.allocatedT + this.timeFrameT + this.timeT + this.primitivesT;
        this.textView.get().setText(aux);
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected final Void doInBackground(final Void... params) {
        LOGGER.info("doInBackground");

        this.scheduler.scheduleAtFixedRate(this.timer, 0L, this.updateInterval, TimeUnit.MILLISECONDS);
        boolean running = true;
        do {
            try {
                running = !this.scheduler.awaitTermination(1L, TimeUnit.DAYS);
            } catch (final InterruptedException ex) {
                LOGGER.severe(ex.getMessage());
                System.exit(1);
            }
        } while (running);
        return null;
    }

    @Override
    protected final void onProgressUpdate(final Void... values) {
        printText();
    }

    @Override
    protected final void onPostExecute(final Void result) {
        printText();

        this.buttonRender.get().setText(R.string.render);
        this.requestRender.run();
        this.finishRender.run();
    }

    @Override
    protected void onCancelled(final Void result) {
        super.onCancelled(result);
        LOGGER.info(ON_CANCELLED);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        LOGGER.info(ON_CANCELLED);
    }

    /**
     * The builder for this class.
     */
    static final class Builder {

        /**
         * The {@link Logger} for this class.
         */
        private static final Logger LOGGER_BUILDER = Logger.getLogger(RenderTask.Builder.class.getName());

        /**
         * @see RenderTask#requestRender
         */
        private final Runnable requestRender;

        /**
         * @see RenderTask#finishRender
         */
        private final Runnable finishRender;

        /**
         * @see RenderTask#textView
         */
        private final TextView textView;

        /**
         * @see RenderTask#buttonRender
         */
        private final Button buttonRender;

        /**
         * @see RenderTask.Builder#withUpdateInterval(long)
         */
        private long updateInterval = 0L;

        /**
         * @see RenderTask.Builder#withWidth(int)
         */
        private int width = 0;

        /**
         * @see RenderTask.Builder#withHeight(int)
         */
        private int height = 0;

        /**
         * @see RenderTask.Builder#withNumThreads(int)
         */
        private int numThreads = 0;

        /**
         * @see RenderTask.Builder#withNumPrimitives(int)
         */
        private int numPrimitives = 0;

        /**
         * @see RenderTask.Builder#withNumLights(int)
         */
        private int numLights = 0;

        /**
         * @see RenderTask.Builder#withSamplesPixel(int)
         */
        private int samplesPixel = 0;

        /**
         * @see RenderTask.Builder#withSamplesLight(int)
         */
        private int samplesLight = 0;

        /**
         * The constructor of the builder with some mandatory parameters to instantiate a {@link RenderTask}.
         *
         * @param requestRender The new value for {@link RenderTask#requestRender} field.
         * @param finishRender  The new value for {@link RenderTask#finishRender} field.
         * @param textView      The new value for {@link RenderTask#textView} field.
         * @param buttonRender  The new value for {@link RenderTask#buttonRender} field.
         */
        Builder(
                @NonNull final Runnable requestRender,
                @NonNull final Runnable finishRender,
                @NonNull final TextView textView,
                @NonNull final Button buttonRender
        ) {
            this.requestRender = requestRender;
            this.finishRender = finishRender;
            this.textView = textView;
            this.buttonRender = buttonRender;
        }

        /**
         * Sets the {@link RenderTask#updateInterval} field.
         *
         * @param updateInterval The new value for {@link RenderTask#updateInterval} field.
         * @return The builder with {@link RenderTask.Builder#updateInterval} already set.
         */
        @NonNull final RenderTask.Builder withUpdateInterval(final long updateInterval) {
            LOGGER_BUILDER.info("withUpdateInterval");

            this.updateInterval = updateInterval;
            return this;
        }

        /**
         * Sets width of {@link RenderTask#resolutionT}.
         *
         * @param width The new value for width in {@link RenderTask#resolutionT} field.
         * @return The builder with {@link RenderTask.Builder#width} already set.
         */
        @NonNull final RenderTask.Builder withWidth(final int width) {
            LOGGER_BUILDER.info("withWidth");

            this.width = width;
            return this;
        }

        /**
         * Sets height of {@link RenderTask#resolutionT}.
         *
         * @param height The new value for height in {@link RenderTask#resolutionT} field.
         * @return The builder with {@link RenderTask.Builder#height} already set.
         */
        @NonNull final RenderTask.Builder withHeight(final int height) {
            LOGGER_BUILDER.info("withHeight");

            this.height = height;
            return this;
        }

        /**
         * Sets {@link RenderTask#threadsT}.
         *
         * @param numThreads The new value for {@link RenderTask#threadsT} field.
         * @return The builder with {@link RenderTask.Builder#numThreads} already set.
         */
        @NonNull final RenderTask.Builder withNumThreads(final int numThreads) {
            LOGGER_BUILDER.info("withNumThreads");

            this.numThreads = numThreads;
            return this;
        }

        /**
         * Sets {@link RenderTask#samplesPixelT}.
         *
         * @param samplesPixel The new value for {@link RenderTask#samplesPixelT} field.
         * @return The builder with {@link RenderTask.Builder#samplesPixel} already set.
         */
        @NonNull final RenderTask.Builder withSamplesPixel(final int samplesPixel) {
            LOGGER_BUILDER.info("withSamplesPixel");

            this.samplesPixel = samplesPixel;
            return this;
        }

        /**
         * Sets {@link RenderTask#samplesLightT}.
         *
         * @param samplesLight The new value for {@link RenderTask#samplesLightT} field.
         * @return The builder with {@link RenderTask.Builder#samplesLight} already set.
         */
        @NonNull final RenderTask.Builder withSamplesLight(final int samplesLight) {
            LOGGER_BUILDER.info("withSamplesLight");

            this.samplesLight = samplesLight;
            return this;
        }

        /**
         * Sets the number of primitives in {@link RenderTask#primitivesT}.
         *
         * @param numPrimitives The new value for {@link RenderTask#primitivesT} field.
         * @return The builder with {@link RenderTask.Builder#numPrimitives} already set.
         */
        @NonNull final RenderTask.Builder withNumPrimitives(final int numPrimitives) {
            LOGGER_BUILDER.info("withNumPrimitives");

            this.numPrimitives = numPrimitives;
            return this;
        }

        /**
         * Sets the number of lights in {@link RenderTask#primitivesT}.
         *
         * @param numLights The new value for number of lights in {@link RenderTask#primitivesT} field.
         * @return The builder with {@link RenderTask.Builder#numLights} already set.
         */
        @NonNull final RenderTask.Builder withNumLights(final int numLights) {
            LOGGER_BUILDER.info("withNumLights");

            this.numLights = numLights;
            return this;
        }

        /**
         * Builds a new instance of {@link RenderTask}.
         *
         * @return A new instance of {@link RenderTask}.
         */
        @NonNull final RenderTask build() {
            LOGGER_BUILDER.info("build");

            return new RenderTask(this);
        }

        /**
         * @see RenderTask#requestRender
         */
        Runnable getRequestRender() {
            return this.requestRender;
        }

        /**
         * @see RenderTask#finishRender
         */
        Runnable getFinishRender() {
            return this.finishRender;
        }

        /**
         * @see RenderTask.Builder#withUpdateInterval(long)
         */
        long getUpdateInterval() {
            return this.updateInterval;
        }

        /**
         * @see RenderTask.Builder#withNumPrimitives(int)
         */
        int getNumPrimitives() {
            return this.numPrimitives;
        }

        /**
         * @see RenderTask.Builder#withNumLights(int)
         */
        int getNumLights() {
            return this.numLights;
        }

        /**
         * @see RenderTask.Builder#withWidth(int)
         */
        public int getWidth() {
            return this.width;
        }

        /**
         * @see RenderTask.Builder#withHeight(int)
         */
        public int getHeight() {
            return this.height;
        }

        /**
         * @see RenderTask.Builder#withNumThreads(int)
         */
        int getNumThreads() {
            return this.numThreads;
        }

        /**
         * @see RenderTask.Builder#withSamplesPixel(int)
         */
        public int getSamplesPixel() {
            return this.samplesPixel;
        }

        /**
         * @see RenderTask.Builder#withSamplesLight(int)
         */
        public int getSamplesLight() {
            return this.samplesLight;
        }

        /**
         * @see RenderTask#buttonRender
         */
        Button getButtonRender() {
            return this.buttonRender;
        }

        /**
         * @see RenderTask#textView
         */
        TextView getTextView() {
            return this.textView;
        }
    }
}
