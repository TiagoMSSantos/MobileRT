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
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java8.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.java.Log;
import puscas.mobilertapp.configs.Config;
import puscas.mobilertapp.configs.ConfigResolution;
import puscas.mobilertapp.constants.ConstantsError;
import puscas.mobilertapp.constants.ConstantsMethods;
import puscas.mobilertapp.constants.ConstantsRenderer;
import puscas.mobilertapp.constants.ConstantsToast;
import puscas.mobilertapp.constants.State;
import puscas.mobilertapp.exceptions.FailureException;
import puscas.mobilertapp.exceptions.LowMemoryException;
import puscas.mobilertapp.utils.Utils;
import puscas.mobilertapp.utils.UtilsLogging;

/**
 * The {@link GLSurfaceView} to show the scene being rendered.
 */
@Log
public final class DrawView extends GLSurfaceView {

    /**
     * The {@link GLSurfaceView.Renderer}.
     */
    @VisibleForTesting
    @Getter(AccessLevel.PUBLIC)
    private final MainRenderer renderer = new MainRenderer();

    /**
     * The {@link ExecutorService} which holds
     * {@link ConstantsRenderer#NUMBER_THREADS} number of threads that will
     * create Ray Tracer engine renderer.
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(ConstantsRenderer.NUMBER_THREADS);

    /**
     * The changing configs.
     *
     * @see Activity#isChangingConfigurations()
     */
    @Getter(AccessLevel.PACKAGE)
    private boolean changingConfigs = false;

    /**
     * The last task submitted to {@link #executorService}.
     */
    private Future<Boolean> lastTask = null;

    /**
     * The constructor for this class.
     *
     * @param context The context of the Android system.
     */
    public DrawView(@NonNull final Context context) {
        super(context);
        log.info("DrawView start 1");

        this.renderer.prepareRenderer(this::requestRender);
        initEglContextFactory();

        log.info("DrawView finished 1");
    }

    /**
     * The constructor for this class.
     *
     * @param context The context of the Android system.
     * @param attrs   The attributes of the Android system.
     */
    public DrawView(@NonNull final Context context,
                    @NonNull final AttributeSet attrs) {
        super(context, attrs);
        log.info("DrawView start 2");

        this.renderer.prepareRenderer(this::requestRender);

        log.info("DrawView finished 2");
    }

    /**
     * Helper method which initiates the {@link GLSurfaceView.EGLContextFactory}.
     */
    private void initEglContextFactory() {
        log.info("initEglContextFactory start");
        this.changingConfigs = false;

        final GLSurfaceView.EGLContextFactory eglContextFactory = new MyEglContextFactory(this);
        setEGLContextClientVersion(MyEglContextFactory.EGL_CONTEXT_CLIENT_VERSION);
        setEGLContextFactory(eglContextFactory);

        log.info("initEglContextFactory finished");
    }

    /**
     * Stops the Ray Tracer engine and sets its {@link State} to {@link State#STOPPED}.
     *
     * @param wait Whether it should wait for the Ray Tracer engine to stop.
     */
    private native void rtStopRender(boolean wait);

    /**
     * Sets the Ray Tracer engine {@link State} to {@link State#BUSY}.
     *
     * @param wait Whether it should wait for the Ray Tracer engine to stop at the beginning.
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
    @NonNull
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
        try {
            this.renderer.checksFreeMemory(1, () -> {});
        } catch (final Exception ex) {
            throw new FailureException(ex);
        }

        setRenderer(this.renderer);
    }

    /**
     * Stops the Ray Tracer engine and waits for it to stop rendering.
     */
    void stopDrawing() {
        log.info("stopDrawing");

        rtStopRender(true);
        Optional.ofNullable(this.lastTask)
            .ifPresent(task -> task.cancel(false));

        waitLastTask();
        this.renderer.updateButton(R.string.render);

        final String message = "stopDrawing" + ConstantsMethods.FINISHED;
        log.info(message);
    }

    /**
     * Asynchronously creates the requested scene and starts rendering it.
     *
     * @param config The ray tracer configuration.
     */
    void renderScene(@NonNull final Config config) {
        log.info(ConstantsMethods.RENDER_SCENE);

        waitLastTask();
        MainActivity.resetErrno();
        rtStartRender(false);

        this.lastTask = this.executorService.submit(() -> {
            this.renderer.waitLastTask();
            rtStartRender(true);
            try {
                startRayTracing(config);
                return Boolean.TRUE;
            } catch (final LowMemoryException ex) {
                warningError(ex, ConstantsToast.DEVICE_WITHOUT_ENOUGH_MEMORY + ex.getMessage());
            } catch (final RuntimeException ex) {
                warningError(ex, ConstantsToast.COULD_NOT_LOAD_THE_SCENE + ex.getMessage());
            }

            final String messageFailed = ConstantsMethods.RENDER_SCENE + " executor failed";
            log.severe(messageFailed);
            this.renderer.rtFinishRender();

            // Only the UI thread can update the text in the Render button.
            post(() -> this.renderer.updateButton(R.string.render));

            return Boolean.FALSE;
        });

        // This should be executed by the UI thread, so it's good to go.
        this.renderer.updateButton(R.string.stop);

        final String messageFinished = ConstantsMethods.RENDER_SCENE + ConstantsMethods.FINISHED;
        log.info(messageFinished);
    }

    /**
     * Helper method that prepares the scene and starts the Ray Tracing engine
     * to render it.
     *
     * @param config The ray tracer configuration.
     * @throws LowMemoryException If the device has low free memory.
     */
    private void startRayTracing(@NonNull final Config config) throws LowMemoryException {
        final String message = ConstantsMethods.RENDER_SCENE + " executor";
        log.info(message);

        createScene(config);
        requestRender();

        final String messageFinished = ConstantsMethods.RENDER_SCENE + " executor"
            + ConstantsMethods.FINISHED;
        log.info(messageFinished);
    }

    /**
     * Waits for the result of the last task submitted to the {@link ExecutorService}.
     */
    void waitLastTask() {
        log.info("waitLastTask");

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
        log.info(message);
    }

    /**
     * Loads the scene and creates the Ray Tracer renderer.
     *
     * @param config The ray tracer configuration.
     * @throws LowMemoryException If the device has low free memory.
     */
    private void createScene(final Config config) throws LowMemoryException {
        log.info("createScene");

        MainActivity.resetErrno();
        final int numPrimitives = this.renderer.rtInitialize(config);

        this.renderer.resetStats(config.getThreads(), config.getConfigSamples(),
            numPrimitives, rtGetNumberOfLights());
        final int widthView = getWidth();
        final int heightView = getHeight();
        queueEvent(() -> this.renderer.setBitmap(
            config.getConfigResolution(),
            ConfigResolution.builder().width(widthView).height(heightView).build(),
            config.isRasterize()
        ));
    }

    /**
     * A helper method that warnings the user about a system error.
     *
     * @param exception    The exception caught.
     * @param errorMessage The error message.
     */
    private void warningError(@NonNull final Exception exception,
                              final CharSequence errorMessage) {
        this.renderer.resetStats();
        final String message = exception.getClass() + ": " + exception.getMessage();
        log.severe(message);
        post(() -> Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show());
    }

    @Override
    public void onPause() {
        log.info("onPause");
        super.onPause();

        final Activity activity = getActivity();
        this.changingConfigs = activity.isChangingConfigurations();

        MainActivity.resetErrno();
        stopDrawing();
        setVisibility(View.GONE);

        final String message = "onPause" + ConstantsMethods.FINISHED;
        log.info(message);
    }

    @Override
    protected void onDetachedFromWindow() {
        log.info(ConstantsMethods.ON_DETACHED_FROM_WINDOW);
        super.onDetachedFromWindow();
        this.finishRenderer();
        setVisibility(View.GONE);

        final String message = ConstantsMethods.ON_DETACHED_FROM_WINDOW + ConstantsMethods.FINISHED;
        log.info(message);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        log.info("performClick");

        return true;
    }

    @Override
    public void onWindowFocusChanged(final boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        log.info("onWindowFocusChanged");

        if (hasWindowFocus && getVisibility() == View.GONE) {
            setVisibility(View.VISIBLE);
        }

        final String message = "onWindowFocusChanged" + ConstantsMethods.FINISHED;
        log.info(message);
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
        log.info("finishRenderer");

        MainActivity.resetErrno();
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
        log.info("getRayTracerState");
        MainActivity.resetErrno();

        return this.renderer.getState();
    }

    /**
     * Prepares the {@link MainRenderer} with the OpenGL shaders' code.
     *
     * @param shadersCode        The shaders' code for the Ray Tracing engine.
     * @param shadersPreviewCode The shaders' code for the OpenGL preview feature.
     */
    void setUpShadersCode(final Map<Integer, String> shadersCode,
                          final Map<Integer, String> shadersPreviewCode) {
        this.renderer.setUpShadersCode(shadersCode, shadersPreviewCode);
    }

    /**
     * Prepares the {@link MainRenderer} with the render button for the {@link RenderTask}.
     *
     * @param button The render {@link Button}.
     */
    void setUpButtonRender(final Button button) {
        this.renderer.setButtonRender(button);
    }

}
