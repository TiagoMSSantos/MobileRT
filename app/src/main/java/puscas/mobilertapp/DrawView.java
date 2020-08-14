package puscas.mobilertapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java8.util.Optional;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Contract;
import puscas.mobilertapp.exceptions.LowMemoryException;
import puscas.mobilertapp.utils.ConstantsError;
import puscas.mobilertapp.utils.ConstantsMethods;
import puscas.mobilertapp.utils.ConstantsRenderer;
import puscas.mobilertapp.utils.ConstantsToast;
import puscas.mobilertapp.utils.State;
import puscas.mobilertapp.utils.Utils;
import puscas.mobilertapp.utils.UtilsLogging;

/**
 * The {@link GLSurfaceView} to show the scene being rendered.
 */
public final class DrawView extends GLSurfaceView {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(DrawView.class.getName());

    /**
     * The {@link GLSurfaceView.Renderer}.
     */
    private final MainRenderer renderer = new MainRenderer();

    /**
     * The {@link ExecutorService} which holds
     * {@link ConstantsRenderer#NUMBER_THREADS} number of threads that will
     * create Ray Tracer engine renderer.
     */
    private final ExecutorService executorService =
        Executors.newFixedThreadPool(ConstantsRenderer.NUMBER_THREADS);

    /**
     * The changingConfigs.
     *
     * @see Activity#isChangingConfigurations()
     */
    private boolean changingConfigs = false;

    /**
     * The last task submitted to {@link ExecutorService}.
     */
    private Future<Boolean> lastTask = null;

    /**
     * The constructor for this class.
     *
     * @param context The context of the Android system.
     */
    public DrawView(@Nonnull final Context context) {
        super(context);

        this.renderer.prepareRenderer(this::requestRender);
        initEglContextFactory();
    }

    /**
     * The constructor for this class.
     *
     * @param context The context of the Android system.
     * @param attrs   The attributes of the Android system.
     */
    public DrawView(@Nonnull final Context context,
                    @Nonnull final AttributeSet attrs) {
        super(context, attrs);

        this.renderer.prepareRenderer(this::requestRender);
    }

    /**
     * Helper method which initiates the {@link GLSurfaceView.EGLContextFactory}.
     */
    private void initEglContextFactory() {
        this.changingConfigs = false;

        final GLSurfaceView.EGLContextFactory eglContextFactory = new MyEglContextFactory(this);
        setEGLContextClientVersion(MyEglContextFactory.EGL_CONTEXT_CLIENT_VERSION);
        setEGLContextFactory(eglContextFactory);
    }

    /**
     * Stops the Ray Tracer engine and sets its {@link State} to {@link State#STOPPED}.
     */
    private native void rtStopRender(boolean wait);

    /**
     * Sets the Ray Tracer engine {@link State} to {@link State#BUSY}.
     */
    private native void rtStartRender(boolean wait);

    /**
     * Gets the number of lights in the scene.
     *
     * @return The number of lights.
     */
    private native int rtGetNumberOfLights();

    /**
     * Helper method which gets the instance of the {@link Activity}.
     *
     * @return The current {@link Activity}.
     */
    @Nonnull
    private Activity getActivity() {
        Context context = getContext();
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        throw new IllegalStateException(ConstantsError.UNABLE_TO_FIND_AN_ACTIVITY + context);
    }

    /**
     * Sets the {@link DrawView#renderer} as the {@link GLSurfaceView.Renderer}
     * of this object.
     *
     * @param textView        The {@link TextView} to set in the
     *                        {@link DrawView#renderer}.
     * @param activityManager The {@link ActivityManager} to set in the
     *                        {@link DrawView#renderer}.
     */
    void setViewAndActivityManager(final TextView textView,
                                   final ActivityManager activityManager) {
        this.renderer.setTextView(textView);
        this.renderer.setActivityManager(activityManager);
        setRenderer(this.renderer);
    }

    /**
     * Stops the Ray Tracer engine and waits for it to stop rendering.
     */
    void stopDrawing() {
        LOGGER.info("stopDrawing");

        rtStopRender(true);
        Optional.ofNullable(this.lastTask)
            .ifPresent(task -> task.cancel(false));

        waitLastTask();
        this.renderer.updateButton(R.string.render);

        final String message = "stopDrawing" + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }

    /**
     * Asynchronously creates the requested scene and starts rendering it.
     *
     * @param config     The ray tracer configuration.
     * @param numThreads The number of threads to be used in the Ray Tracer
     *                   engine.
     * @param rasterize  Whether should show a preview (rasterize one frame) or
     *                   not.
     */
    void renderScene(@Nonnull final Config config,
                     final int numThreads,
                     final boolean rasterize) {
        LOGGER.info(ConstantsMethods.RENDER_SCENE);

        waitLastTask();
        rtStartRender(false);

        this.lastTask = this.executorService.submit(() -> {
            this.renderer.waitLastTask();
            rtStartRender(true);
            try {
                startRayTracing(config, numThreads, rasterize);
                return Boolean.TRUE;
            } catch (final LowMemoryException ex) {
                warningError(ex, ConstantsToast.DEVICE_WITHOUT_ENOUGH_MEMORY);
            } catch (final RuntimeException ex) {
                warningError(ex, ConstantsToast.COULD_NOT_LOAD_THE_SCENE);
            }

            final String messageFailed = ConstantsMethods.RENDER_SCENE + " executor failed";
            LOGGER.severe(messageFailed);
            this.renderer.rtFinishRender();

            // Only the UI thread can update the text in the Render button.
            post(() -> this.renderer.updateButton(R.string.render));

            return Boolean.FALSE;
        });

        // This should be executed by the UI thread, so it's good to go.
        this.renderer.updateButton(R.string.stop);

        final String messageFinished = ConstantsMethods.RENDER_SCENE + ConstantsMethods.FINISHED;
        LOGGER.info(messageFinished);
    }

    /**
     * Helper method that prepares the scene and starts the Ray Tracing engine
     * to render it.
     *
     * @param config     The ray tracer configuration.
     * @param numThreads The number of threads to be used in the Ray Tracer
     *                   engine.
     * @param rasterize  Whether should show a preview (rasterize one frame) or
     *                   not.
     * @throws LowMemoryException If the device has low free memory.
     */
    private void startRayTracing(@Nonnull final Config config,
                                 final int numThreads,
                                 final boolean rasterize) throws LowMemoryException {
        final String message = ConstantsMethods.RENDER_SCENE + " executor";
        LOGGER.info(message);

        createScene(config, numThreads, rasterize);
        requestRender();

        final String messageFinished = ConstantsMethods.RENDER_SCENE + " executor"
            + ConstantsMethods.FINISHED;
        LOGGER.info(messageFinished);
    }

    /**
     * Waits for the result of the last task submitted to the {@link ExecutorService}.
     */
    void waitLastTask() {
        LOGGER.info("waitLastTask");

        this.renderer.waitLastTask();
        Optional.ofNullable(this.lastTask)
            .ifPresent(task -> {
                try {
                    task.get(1L, TimeUnit.DAYS);
                } catch (final ExecutionException | TimeoutException | RuntimeException ex) {
                    UtilsLogging.logThrowable(ex, "DrawView#waitLastTask");
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    Utils.handleInterruption("DrawView#waitLastTask");
                }
            });

        final String message = "waitLastTask" + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }

    /**
     * Loads the scene and creates the Ray Tracer renderer.
     *
     * @param config     The ray tracer configuration.
     * @param numThreads The number of threads to be used in the Ray Tracer engine.
     * @param rasterize  Whether should show a preview (rasterize one frame) or not.
     */
    private void createScene(final Config config,
                             final int numThreads,
                             final boolean rasterize) throws LowMemoryException {
        LOGGER.info("createScene");
        final int numPrimitives = this.renderer.rtInitialize(config);
        this.renderer.resetStats(numThreads, config.getConfigSamples(),
            numPrimitives, rtGetNumberOfLights());
        final int widthView = getWidth();
        final int heightView = getHeight();
        queueEvent(() -> this.renderer.setBitmap(
            config.getConfigResolution(), widthView, heightView, rasterize));
    }

    /**
     * A helper method that warnings the user about a system error.
     *
     * @param exception    The exception caught.
     * @param errorMessage The error message.
     */
    private void warningError(@Nonnull final Exception exception,
                              final CharSequence errorMessage) {
        this.renderer.resetStats();
        final String message = exception.getClass() + ":" + exception.getMessage();
        LOGGER.severe(message);
        post(() -> Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show());
    }

    /**
     * Gets the {@link #renderer}.
     *
     * @return The {@link #renderer} of this object.
     */
    @Contract(pure = true)
    @Nonnull
    public MainRenderer getRenderer() {
        LOGGER.info("getRenderer");

        return this.renderer;
    }

    /**
     * Gets the {@link Activity#isChangingConfigurations()}.
     *
     * @return The {@link DrawView#changingConfigs}.
     */
    @Contract(pure = true)
    boolean isChangingConfigs() {
        return this.changingConfigs;
    }

    @Override
    public void onPause() {
        LOGGER.info("onPause");
        super.onPause();

        final Activity activity = getActivity();
        this.changingConfigs = activity.isChangingConfigurations();
        stopDrawing();

        final String message = "onPause" + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }

    @Override
    protected void onDetachedFromWindow() {
        LOGGER.info(ConstantsMethods.ON_DETACHED_FROM_WINDOW);
        super.onDetachedFromWindow();

        final String message = ConstantsMethods.ON_DETACHED_FROM_WINDOW + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        LOGGER.info("performClick");

        return true;
    }

    @Override
    public void onWindowFocusChanged(final boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        LOGGER.info("onWindowFocusChanged");

        if (hasWindowFocus && getVisibility() == View.GONE) {
            setVisibility(View.VISIBLE);
        }

        final String message = "onWindowFocusChanged" + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }

    /**
     * This is an auxiliary method that serves as a middle man to let outside
     * classes like {@link MainActivity} terminate properly the Ray Tracing
     * engine without having to not obey the law of Demeter.
     *
     * @implNote This method calls {@link MainRenderer#rtFinishRender()} and
     *     also {@link MainRenderer#freeArrays()}.
     * @see <a href="https://en.wikipedia.org/wiki/Law_of_Demeter">Law of Demeter</a>
     */
    void finishRenderer() {
        LOGGER.info("finishRenderer");

        this.renderer.rtFinishRender();
        this.renderer.freeArrays();
    }

    /**
     * This is an auxiliary method that serves as a middle man to let outside
     * classes like {@link MainActivity} get the current {@link State} of the
     * Ray Tracer engine without having to not obey the law of Demeter.
     *
     * @return The current Ray Tracer engine {@link State}.
     */
    State getRayTracerState() {
        LOGGER.info("getState");

        return this.renderer.getState();
    }

    /**
     * Prepares the {@link MainRenderer} with the OpenGL shaders' code and also
     * with the render button for the {@link android.os.AsyncTask}.
     *
     * @param shadersCode        The shaders' code for the Ray Tracing engine.
     * @param shadersPreviewCode The shaders' code for the OpenGL preview feature.
     * @param button             The render {@link Button}.
     */
    void prepareRenderer(final Map<Integer, String> shadersCode,
                         final Map<Integer, String> shadersPreviewCode,
                         final Button button) {
        this.renderer.prepareRenderer(shadersCode, shadersPreviewCode, button);
    }

}
