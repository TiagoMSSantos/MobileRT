package puscas.mobilertapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class DrawView extends GLSurfaceView {
    private static final int EGL_CONTEXT_CLIENT_VERSION_VALUE = 2;
    private static EGLContext retainedGlContext = null;
    int numThreads_ = 0;
    private MainActivity mainActivity_ = null;

    final ViewText viewText_ = new ViewText();
    MainRenderer renderer_ = null;
    RenderTask renderTask_ = null;
    ByteBuffer arrayVertices = null;
    ByteBuffer arrayColors = null;
    ByteBuffer arrayCamera = null;
    private boolean changingConfigurations = false;
    int numberPrimitives_ = 0;

    public boolean getFreeMemStatic(final int memoryNeed) {
        boolean res = true;
        if (memoryNeed >= 0) {
            final ActivityManager activityManager = (ActivityManager) mainActivity_.getSystemService(Context.ACTIVITY_SERVICE);
            final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            final long availMem = memoryInfo.availMem / 1048576L;
            final boolean lowMem = memoryInfo.lowMemory;
            final boolean notEnoughMem = availMem <= (1 + memoryNeed);
            res = notEnoughMem || lowMem;
        }
        return res;
    }

    public DrawView(final Context context) {
        super(context);
        viewText_.resetPrint(getWidth(), getHeight(), 0, 0, 0);
        init();
    }

    public DrawView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        viewText_.resetPrint(getWidth(), getHeight(), 0, 0, 0);
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

    private native int initialize(final int scene, final int shader, final int width, final int height, final int accelerator, final int samplesPixel, final int samplesLight, final String objFile, final String matText);

    private native ByteBuffer initVerticesArray();

    private native ByteBuffer initColorsArray();

    private native ByteBuffer initCameraArray();

    private native ByteBuffer freeNativeBuffer(final ByteBuffer bb);

    private native void stopRender();

    private native int getNumberOfLights();

    native void renderIntoBitmap(final Bitmap image, final int numThreads, final boolean async);

    native int resize(final int size);

    native void finishRender();

    void freeArrays() {
        arrayVertices = freeNativeBuffer(arrayVertices);
        arrayColors = freeNativeBuffer(arrayColors);
        arrayCamera = freeNativeBuffer(arrayCamera);
    }

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

    void setViewAndMainActivity(final TextView textView, final MainActivity mainActivity) {
        viewText_.textView_ = textView;
        viewText_.printText();
        mainActivity_ = mainActivity;
    }


    void stopDrawing() {
        this.setOnTouchListener(null);
        stopRender();
    }

    void startRender() {
        freeArrays();

        if (getFreeMemStatic(1)) {
            freeArrays();
        }

        arrayVertices = initVerticesArray();

        if (getFreeMemStatic(1)) {
            freeArrays();
        }

        arrayColors = initColorsArray();

        if (getFreeMemStatic(1)) {
            freeArrays();
        }

        arrayCamera = initCameraArray();

        if (getFreeMemStatic(1)) {
            freeArrays();
        }

        viewText_.period_ = 250;
        viewText_.buttonRender_.setText(R.string.stop);
        viewText_.start_ = 0;
        viewText_.printText();

        viewText_.start_ = SystemClock.elapsedRealtime();
        requestRender();
    }

    int createScene(final int scene, final int shader, final int numThreads, final int accelerator,
                    final int samplesPixel, final int samplesLight, final int width, final int height,
                    final String objFile, final String matText, final boolean rasterize) {
        freeArrays();
        viewText_.resetPrint(width, height, numThreads, samplesPixel, samplesLight);

        numberPrimitives_ = initialize(scene, shader, width, height, accelerator, samplesPixel, samplesLight, objFile, matText);
        if (numberPrimitives_ == -1) {
            Log.e("MobileRT", "Device without enough memory to render the scene.");
            for (int i = 0; i < 6; ++i) {
                Toast.makeText(getContext(), "Device without enough memory to render the scene.", Toast.LENGTH_LONG).show();
            }
            return -1;
        }
        viewText_.nPrimitivesT_ = ",p=" + numberPrimitives_ + ",l=" + getNumberOfLights();
        numThreads_ = numThreads;
        final int realWidth = getWidth();
        final int realHeight = getHeight();

        renderer_.setBitmap(width, height, realWidth, realHeight);

        renderer_.rasterize_ = rasterize;
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
