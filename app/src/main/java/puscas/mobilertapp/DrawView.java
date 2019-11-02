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

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class DrawView extends GLSurfaceView {
    private static final int EGL_CONTEXT_CLIENT_VERSION_VALUE = 2;
    private static EGLContext retainedGlContext = null;

    MainRenderer renderer_ = new MainRenderer();
    private boolean changingConfigurations = false;
    private ExecutorService executorService_ = Executors.newFixedThreadPool(1);

    public DrawView(final Context context) {
        super(context);
        renderer_.prepareRenderer(this::requestRender);
        renderer_.viewText_.resetPrint(getWidth(), getHeight(), 0, 0, 0);
        init();
    }

    public DrawView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        renderer_.prepareRenderer(this::requestRender);
        renderer_.viewText_.resetPrint(getWidth(), getHeight(), 0, 0, 0);
    }

    synchronized public void onDestroy() {
        super.onDetachedFromWindow();
    }

    synchronized private void init() {
        changingConfigurations = false;

        EGLContextFactory eglContextFactory = new GLSurfaceView.EGLContextFactory() {
            private final int EGL_CONTEXT_CLIENT_VERSION = 2;

            public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
                if (retainedGlContext != null) {
                    final EGLContext eglContext = retainedGlContext;
                    retainedGlContext = null;
                    return eglContext;
                }

                int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, EGL_CONTEXT_CLIENT_VERSION_VALUE, EGL10.EGL_NONE};
                return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, attrib_list);
            }

            public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
                if (changingConfigurations) {
                    retainedGlContext = context;
                    return;
                }

                if (!egl.eglDestroyContext(display, context)) {
                    throw new RuntimeException("eglDestroyContext failed: error " + egl.eglGetError());
                }
            }
        };

        setEGLContextClientVersion(EGL_CONTEXT_CLIENT_VERSION_VALUE);
        setEGLContextFactory(eglContextFactory);
    }

    synchronized private native void stopRender();
    synchronized private native void startRender();
    synchronized private native int getNumberOfLights();

    @Override
    synchronized public void onPause() {
        super.onPause();
        changingConfigurations = getActivity().isChangingConfigurations();
        //setVisibility(View.GONE);
    }

    @Override
    synchronized public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && getVisibility() == View.GONE) {
            setVisibility(View.VISIBLE);
        }
    }

    synchronized private Activity getActivity() {
        Context context = getContext();
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        throw new IllegalStateException("Unable to find an activity: " + context);
    }

    @Override
    synchronized public boolean performClick() {
        super.performClick();
        return true;
    }

    synchronized void setViewAndActivityManager(final TextView textView, final ActivityManager activityManager) {
        renderer_.viewText_.textView_ = textView;
        renderer_.viewText_.printText();
        renderer_.activityManager_ = activityManager;
        setRenderer(renderer_);
    }


    synchronized void stopDrawing() {
        Log.d("Test", "stopDrawing");
        this.setOnTouchListener(null);
        Log.d("Test", "stopRender");
        stopRender();
        renderer_.waitForLastTask();
        executorService_.shutdown();
        boolean running = true;
        Log.d("Test", "waiting executorService_");
        do {
            try {
                running = !executorService_.awaitTermination(1, TimeUnit.DAYS);
            } catch (final InterruptedException ex) {
                Log.e("InterruptedException", Objects.requireNonNull(ex.getMessage()));
                //System.exit(1);
            }
        } while (running);
        Log.d("Test", "new executorService_");
        executorService_ = Executors.newFixedThreadPool(1);
        renderer_.finishRender();
    }

    synchronized void renderScene(final int scene, final int shader, final int numThreads, final int accelerator,
                            final int samplesPixel, final int samplesLight, final int width, final int height,
                            final String objFile, final String matText, final boolean rasterize) {
        Log.d("Test", "renderScene");
        renderer_.viewText_.start_ = 0;
        renderer_.viewText_.printText();

        renderer_.waitForLastTask();
        executorService_.shutdown();
        boolean running = true;
        do {
            try {
                running = !executorService_.awaitTermination(1, TimeUnit.DAYS);
            } catch (final InterruptedException ex) {
                Log.e("InterruptedException", Objects.requireNonNull(ex.getMessage()));
                //System.exit(1);
            }
        } while (running);
        executorService_ = Executors.newFixedThreadPool(1);
        startRender();

        executorService_.submit(() -> {
            Log.d("Test executor", "renderScene");
            synchronized (this) {
                renderer_.waitForLastTask();

                final int ret = createScene(scene, shader, numThreads, accelerator, samplesPixel, samplesLight, width,
                        height, objFile, matText);
                if (ret != -1) {
                    Log.d("Test", "startRender");
                    renderer_.freeArrays();
                    renderer_.rasterize_ = true;
                    renderer_.viewText_.start_ = SystemClock.elapsedRealtime();
                    requestRender();
                } else {
                    stopRender();
                    renderer_.freeArrays();
                    renderer_.rasterize_ = true;
                    renderer_.viewText_.start_ = SystemClock.elapsedRealtime();
                    new RenderTask(renderer_.viewText_, this::requestRender, renderer_::finishRender, 250).executeOnExecutor(executorService_);
                }
                Log.d("Test executor", "renderScene 2");
            }
        });
    }

    synchronized private int createScene(final int scene, final int shader, final int numThreads, final int accelerator,
                    final int samplesPixel, final int samplesLight, final int width, final int height,
                    final String objFile, final String matText) {
        Log.d("Test", "createScene");
        renderer_.freeArrays();
        renderer_.viewText_.resetPrint(width, height, numThreads, samplesPixel, samplesLight);

        renderer_.numberPrimitives_ = renderer_.initialize(scene, shader, width, height, accelerator, samplesPixel,
                samplesLight, objFile, matText);
        if (renderer_.numberPrimitives_ == -1) {
            Log.e("MobileRT", "Device without enough memory to render the scene.");
            /*for (int i = 0; i < 1; ++i) {
                Toast.makeText(getContext(), "Device without enough memory to render the scene.", Toast.LENGTH_LONG).show();
            }
            renderer_.viewText_.buttonRender_.setText(R.string.render);*/
            return -1;
        }
        if (renderer_.numberPrimitives_ == -2) {
            Log.e("MobileRT", "Could not load the scene.");
            /*for (int i = 0; i < 1; ++i) {
                Toast.makeText(getContext(), "Could not load the scene.", Toast.LENGTH_LONG).show();
            }
            renderer_.viewText_.buttonRender_.setText(R.string.render);*/
            return -1;
        }
        renderer_.viewText_.nPrimitivesT_ = ",p=" + renderer_.numberPrimitives_ + ",l=" + getNumberOfLights();
        renderer_.numThreads_ = numThreads;
        final int realWidth = getWidth();
        final int realHeight = getHeight();

        renderer_.setBitmap(width, height, realWidth, realHeight);
        return 0;
    }

    enum Stage {
        idle(0), busy(1), end(2), stop(3);
        final int id_;

        Stage(final int id) {
            this.id_ = id;
        }
    }
}
