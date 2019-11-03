package puscas.mobilertapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * OpenGL view to show the scene being rendered.
 */
public final class DrawView extends GLSurfaceView {
    private static final Logger LOGGER = Logger.getLogger(DrawView.class.getName());

    private static final int EGL_CONTEXT_CLIENT_VERSION_VALUE = 2;
    private static EGLContext retainedGLContext = null;

    final MainRenderer renderer_ = new MainRenderer();
    private boolean changingConfigurations = false;
    private ExecutorService executorService_ = Executors.newFixedThreadPool(1);

    public DrawView(final Context context) {
        super(context);
        this.renderer_.prepareRenderer(this::requestRender);
        this.renderer_.viewText_.resetPrint(this.getWidth(), this.getHeight(), 0, 0, 0);
        this.init();
    }

    public DrawView(@NonNull final Context context, @NonNull final AttributeSet attrs) {
        super(context, attrs);
        this.renderer_.prepareRenderer(this::requestRender);
        this.renderer_.viewText_.resetPrint(this.getWidth(), this.getHeight(), 0, 0, 0);
    }

    synchronized void onDestroy() {
        super.onDetachedFromWindow();

        LOGGER.log(Level.INFO, "onDestroy");
    }

    private synchronized void init() {
        this.changingConfigurations = false;

        final GLSurfaceView.EGLContextFactory eglContextFactory = new GLSurfaceView.EGLContextFactory() {
            private static final int EGL_CONTEXT_CLIENT_VERSION = 2;

            public EGLContext createContext(final EGL10 egl, final EGLDisplay display, final EGLConfig eglConfig) {
                if (retainedGLContext != null) {
                    final EGLContext eglContext = retainedGLContext;
                    retainedGLContext = null;
                    return eglContext;
                }

                final int[] attribList = {this.EGL_CONTEXT_CLIENT_VERSION, EGL_CONTEXT_CLIENT_VERSION_VALUE,
                        EGL10.EGL_NONE};
                return egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attribList);
            }

            public void destroyContext(final EGL10 egl, final EGLDisplay display, final EGLContext context) {
                if (DrawView.this.changingConfigurations) {
                    retainedGLContext = context;
                    return;
                }

                if (!egl.eglDestroyContext(display, context)) {
                    throw new RuntimeException("eglDestroyContext failed: error " + egl.eglGetError());
                }
            }
        };

        this.setEGLContextClientVersion(EGL_CONTEXT_CLIENT_VERSION_VALUE);
        this.setEGLContextFactory(eglContextFactory);
    }

    private synchronized native void stopRender();
    private synchronized native void startRender();
    private synchronized native int getNumberOfLights();

    @Override
    public synchronized void onPause() {
        super.onPause();
        this.changingConfigurations = this.getActivity().isChangingConfigurations();
        //setVisibility(View.GONE);

        LOGGER.log(Level.INFO, "onPause");
    }

    @Override
    public synchronized void onWindowFocusChanged(final boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus && this.getVisibility() == View.GONE) {
            this.setVisibility(View.VISIBLE);
        }
    }

    private synchronized Activity getActivity() {
        Context context = this.getContext();
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        throw new IllegalStateException("Unable to find an activity: " + context);
    }

    @Override
    public synchronized boolean performClick() {
        super.performClick();
        return true;
    }

    synchronized void setViewAndActivityManager(final TextView textView, final ActivityManager activityManager) {
        this.renderer_.viewText_.textView_ = textView;
        this.renderer_.viewText_.printText();
        this.renderer_.activityManager_ = activityManager;
        this.setRenderer(this.renderer_);
    }


    synchronized void stopDrawing() {
        Log.d("Test", "stopDrawing");
        this.setOnTouchListener(null);
        Log.d("Test", "stopRender");
        this.stopRender();

        this.waitForLastTask();

        this.renderer_.finishRender();
    }

    private synchronized void waitForLastTask() {
        this.renderer_.waitForLastTask();
        this.executorService_.shutdown();
        boolean running = true;
        do {
            try {
                running = !this.executorService_.awaitTermination(1L, TimeUnit.DAYS);
            } catch (final InterruptedException ex) {
                Log.e("InterruptedException", Objects.requireNonNull(ex.getMessage()));
            }
        } while (running);
        this.executorService_ = Executors.newFixedThreadPool(1);
    }

    synchronized void renderScene(final int scene, final int shader, final int numThreads, final int accelerator,
                            final int samplesPixel, final int samplesLight, final int width, final int height,
                            final String objFile, final String matText, final boolean rasterize) {
        Log.d("Test", "renderScene");
        this.renderer_.viewText_.start_ = 0L;
        this.renderer_.viewText_.printText();

        this.startRender();

        this.executorService_.submit(() -> {
            Log.d("Test executor", "renderScene");
            synchronized (this) {
                this.renderer_.waitForLastTask();

                final int ret = this.createScene(scene, shader, numThreads, accelerator, samplesPixel, samplesLight, width,
                        height, objFile, matText);
                Log.d("Test", "startRender");
                this.renderer_.freeArrays();
                this.renderer_.rasterize_ = rasterize;
                this.renderer_.viewText_.start_ = SystemClock.elapsedRealtime();
                if (ret != -1) {
                    this.requestRender();
                } else {
                    this.stopRender();
                    this.post(() -> this.renderer_.viewText_.buttonRender_.setText(R.string.render));
                    this.requestRender();
                    this.renderer_.finishRender();
                }
                Log.d("Test executor", "renderScene 2");
            }
        });
    }

    private synchronized int createScene(final int scene, final int shader, final int numThreads, final int accelerator,
                                         final int samplesPixel, final int samplesLight, final int width, final int height,
                                         final String objFile, final String matText) {
        Log.d("Test", "createScene");
        this.renderer_.freeArrays();
        this.renderer_.viewText_.resetPrint(width, height, numThreads, samplesPixel, samplesLight);

        this.renderer_.numberPrimitives_ = this.renderer_.initialize(scene, shader, width, height, accelerator, samplesPixel,
                samplesLight, objFile, matText);
        if (this.renderer_.numberPrimitives_ == -1) {
            Log.e("createScene", "Device without enough memory to render the scene.");
            this.post(() -> {
                for (int i = 0; i < 1; ++i) {
                    Toast.makeText(this.getContext(), "Device without enough memory to render the scene.",
                            Toast.LENGTH_LONG).show();
                }
            });

            //renderer_.viewText_.buttonRender_.setText(R.string.render);
            return -1;
        }
        if (this.renderer_.numberPrimitives_ == -2) {
            Log.e("createScene", "Could not load the scene.");
            this.post(() -> {
                for (int i = 0; i < 1; ++i) {
                    Toast.makeText(this.getContext(), "Could not load the scene.", Toast.LENGTH_LONG).show();
                }
            });
            //renderer_.viewText_.buttonRender_.setText(R.string.render);
            return -1;
        }
        this.renderer_.viewText_.nPrimitivesT_ = ",p=" + this.renderer_.numberPrimitives_ + ",l=" + this.getNumberOfLights();
        this.renderer_.numThreads_ = numThreads;
        final int realWidth = this.getWidth();
        final int realHeight = this.getHeight();

        this.renderer_.setBitmap(width, height, realWidth, realHeight);
        return 0;
    }

}
