package puscas.mobilertapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static puscas.mobilertapp.ConstantsError.UNABLE_TO_FIND_AN_ACTIVITY;
import static puscas.mobilertapp.ConstantsMethods.ON_DETACHED_FROM_WINDOW;
import static puscas.mobilertapp.ConstantsMethods.RENDER_SCENE;
import static puscas.mobilertapp.ConstantsRenderer.NUMBER_THREADS;
import static puscas.mobilertapp.ConstantsToast.COULD_NOT_LOAD_THE_SCENE;
import static puscas.mobilertapp.ConstantsToast.DEVICE_WITHOUT_ENOUGH_MEMORY;
import static puscas.mobilertapp.MyEGLContextFactory.EGL_CONTEXT_CLIENT_VERSION;

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
     * @see Activity#isChangingConfigurations()
     */
    private boolean changingConfigs = false;

    /**
     * The {@link ExecutorService} which holds {@link ConstantsRenderer#NUMBER_THREADS} number of threads that will
     * create Ray Tracer engine renderer.
     */
    private ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_THREADS);

    /**
     * The constructor for this class.
     *
     * @param context The context of the Android system.
     */
    public DrawView(@NonNull final Context context) {
        super(context);

        this.renderer.prepareRenderer(this::requestRender);
        initEGLContextFactory();
    }

    /**
     * The constructor for this class.
     *
     * @param context The context of the Android system.
     * @param attrs   The attributes of the Android system.
     */
    public DrawView(@NonNull final Context context, @NonNull final AttributeSet attrs) {
        super(context, attrs);

        this.renderer.prepareRenderer(this::requestRender);
    }

    /**
     * Helper method which initiates the {@link GLSurfaceView.EGLContextFactory}.
     */
    private void initEGLContextFactory() {
        this.changingConfigs = false;

        final GLSurfaceView.EGLContextFactory eglContextFactory = new MyEGLContextFactory(this);
        setEGLContextClientVersion(EGL_CONTEXT_CLIENT_VERSION);
        setEGLContextFactory(eglContextFactory);
    }

    /**
     * Stops the Ray Tracer engine and sets its {@link State} to {@link State#STOP}.
     */
    private native void RTStopRender();

    /**
     * Sets the Ray Tracer engine {@link State} to {@link State#BUSY}.
     */
    private native void RTStartRender();

    /**
     * Gets the number of lights in the scene.
     *
     * @return The number of lights.
     */
    private native int RTGetNumberOfLights();

    /**
     * Helper method which gets the instance of the {@link Activity}.
     *
     * @return The current {@link Activity}.
     */
    private Activity getActivity() {
        Context context = getContext();
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        throw new IllegalStateException(UNABLE_TO_FIND_AN_ACTIVITY + context);
    }

    /**
     * Sets the {@link DrawView#renderer} as the {@link GLSurfaceView.Renderer} of this object.
     *
     * @param textView        The {@link TextView} to set in the {@link DrawView#renderer}.
     * @param activityManager The {@link ActivityManager} to set in the {@link DrawView#renderer}.
     */
    void setViewAndActivityManager(final TextView textView, final ActivityManager activityManager) {
        this.renderer.setTextView(textView);
        this.renderer.setActivityManager(activityManager);
        setRenderer(this.renderer);
    }

    /**
     * Stops the Ray Tracer engine and waits for it to stop rendering.
     */
    void stopDrawing() {
        LOGGER.info("stopDrawing");

        this.renderer.updateButton(R.string.render);
        setOnTouchListener(null);
        RTStopRender();

        waitForLastTask();

        this.renderer.RTFinishRender();
    }

    /**
     * Waits for the Ray Tracer engine to stop rendering.
     */
    private void waitForLastTask() {
        this.renderer.waitForLastTask();
        this.executorService.shutdown();
        boolean running = true;
        do {
            try {
                running = !this.executorService.awaitTermination(1L, TimeUnit.DAYS);
            } catch (final InterruptedException ex) {
                LOGGER.warning(Objects.requireNonNull(ex.getMessage()));
            }
        } while (running);
        this.executorService = Executors.newFixedThreadPool(NUMBER_THREADS);
    }

    /**
     * Asynchronously creates the requested scene and starts rendering it.
     *
     * @param scene        The requested scene to render.
     * @param shader       The requested shader to use.
     * @param numThreads   The number of threads to be used in the Ray Tracer engine.
     * @param accelerator  The accelerator to use.
     * @param samplesPixel The requested number of samples per pixel.
     * @param samplesLight The requested number of samples per light.
     * @param width        The width of the {@link android.graphics.Bitmap} that holds the rendered image.
     * @param height       The height of the {@link android.graphics.Bitmap} that holds the rendered image.
     * @param objFile      The path to the OBJ file containing the scene.
     * @param matText      The path to the MAT file containing the materials of the scene.
     * @param rasterize    Whether should show a preview (rasterize one frame) or not.
     */
    void renderScene(
            final int scene,
            final int shader,
            final int numThreads,
            final int accelerator,
            final int samplesPixel,
            final int samplesLight,
            final int width,
            final int height,
            final String objFile,
            final String matText,
            final boolean rasterize
    ) {
        LOGGER.info(RENDER_SCENE);

        RTStartRender();
        this.renderer.updateButton(R.string.stop);

        this.executorService.submit(() -> {
            LOGGER.info(RENDER_SCENE);

            this.renderer.waitForLastTask();

            createScene(scene, shader, numThreads, accelerator, samplesPixel, samplesLight,
                    width, height, objFile, matText);
            this.renderer.freeArrays();
            this.renderer.setRasterize(rasterize);
            requestRender();
        });
    }

    /**
     * Loads the scene and creates the Ray Tracer renderer.
     *
     * @param scene        The requested scene to render.
     * @param shader       The requested shader to use.
     * @param numThreads   The number of threads to be used in the Ray Tracer engine.
     * @param accelerator  The accelerator to use.
     * @param samplesPixel The requested number of samples per pixel.
     * @param samplesLight The requested number of samples per light.
     * @param width        The width of the {@link android.graphics.Bitmap} that holds the rendered image.
     * @param height       The height of the {@link android.graphics.Bitmap} that holds the rendered image.
     * @param objFile      The path to the OBJ file containing the scene.
     * @param matText      The path to the MAT file containing the materials of the scene.
     */
    private void createScene(
            final int scene,
            final int shader,
            final int numThreads,
            final int accelerator,
            final int samplesPixel,
            final int samplesLight,
            final int width,
            final int height,
            final String objFile,
            final String matText
    ) {
        LOGGER.info("createScene");

        this.renderer.freeArrays();

        final int numberPrimitives = this.renderer.RTInitialize(scene, shader, width, height, accelerator,
                samplesPixel, samplesLight, objFile, matText);
        if (numberPrimitives == -1) {
            LOGGER.warning(DEVICE_WITHOUT_ENOUGH_MEMORY);

            post(() -> Toast.makeText(getContext(), DEVICE_WITHOUT_ENOUGH_MEMORY, Toast.LENGTH_LONG).show());
        }
        if (numberPrimitives == -2) {
            LOGGER.warning(COULD_NOT_LOAD_THE_SCENE);

            post(() -> Toast.makeText(getContext(), COULD_NOT_LOAD_THE_SCENE, Toast.LENGTH_LONG).show());
        }
        this.renderer.resetStats(numThreads, samplesPixel, samplesLight, numberPrimitives, RTGetNumberOfLights());

        final int widthView = getWidth();
        final int heightView = getHeight();

        this.renderer.setBitmap(width, height, widthView, heightView);
    }

    /**
     * Gets the {@link MainRenderer}.
     *
     * @return The {@link MainRenderer} of this object.
     */
    MainRenderer getRenderer() {
        return this.renderer;
    }

    /**
     * Gets the {@link Activity#isChangingConfigurations()}.
     *
     * @return The {@link DrawView#changingConfigs}.
     */
    boolean isChangingConfigs() {
        return this.changingConfigs;
    }

    @Override
    public void onPause() {
        super.onPause();
        LOGGER.info("onPause");

        final Activity activity = getActivity();
        this.changingConfigs = activity.isChangingConfigurations();
    }

    @Override
    public void onWindowFocusChanged(final boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        LOGGER.info("onWindowFocusChanged");

        if (hasWindowFocus && getVisibility() == View.GONE) {
            setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LOGGER.info(ON_DETACHED_FROM_WINDOW);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        LOGGER.info("performClick");

        return true;
    }
}
