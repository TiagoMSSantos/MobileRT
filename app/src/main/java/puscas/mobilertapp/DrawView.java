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

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class DrawView extends GLSurfaceView {
    private static final int EGL_CONTEXT_CLIENT_VERSION_VALUE = 2;
    private static EGLContext retainedGlContext = null;

    MainRenderer renderer_ = new MainRenderer();
    private boolean changingConfigurations = false;

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
        //init();
    }

    public void onDestroy() {
        super.onDetachedFromWindow();
    }

    private void init() {
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

    private native void stopRender();

    private native int getNumberOfLights();

    @Override
    public void onPause() {
        super.onPause();
        changingConfigurations = getActivity().isChangingConfigurations();
        //setVisibility(View.GONE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && getVisibility() == View.GONE) {
            setVisibility(View.VISIBLE);
        }
    }

    private Activity getActivity() {
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
    public boolean performClick() {
        super.performClick();
        return true;
    }

    void setViewAndActivityManager(final TextView textView, final ActivityManager activityManager) {
        renderer_.viewText_.textView_ = textView;
        renderer_.viewText_.printText();
        renderer_.activityManager_ = activityManager;
        setRenderer(renderer_);
    }


    void stopDrawing() {
        this.setOnTouchListener(null);
        stopRender();
    }

    void startRender(final boolean rasterize) {
        renderer_.freeArrays();

        if (rasterize) {
            renderer_.initArrays();
        }
        renderer_.viewText_.buttonRender_.setText(R.string.stop);
        renderer_.viewText_.start_ = 0;
        renderer_.viewText_.printText();

        renderer_.viewText_.start_ = SystemClock.elapsedRealtime();
        requestRender();
    }

    int createScene(final int scene, final int shader, final int numThreads, final int accelerator,
                    final int samplesPixel, final int samplesLight, final int width, final int height,
                    final String objFile, final String matText) {
        renderer_.freeArrays();
        renderer_.viewText_.resetPrint(width, height, numThreads, samplesPixel, samplesLight);

        renderer_.numberPrimitives_ = renderer_.initialize(scene, shader, width, height, accelerator, samplesPixel, samplesLight, objFile, matText);
        if (renderer_.numberPrimitives_ == -1) {
            Log.e("MobileRT", "Device without enough memory to render the scene.");
            for (int i = 0; i < 1; ++i) {
                Toast.makeText(getContext(), "Device without enough memory to render the scene.", Toast.LENGTH_LONG).show();
            }
            return -1;
        }
        if (renderer_.numberPrimitives_ == -2) {
            Log.e("MobileRT", "Could not load the scene.");
            for (int i = 0; i < 1; ++i) {
                Toast.makeText(getContext(), "Could not load the scene.", Toast.LENGTH_LONG).show();
            }
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
