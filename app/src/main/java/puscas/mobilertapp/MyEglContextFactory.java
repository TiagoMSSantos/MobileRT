package puscas.mobilertapp;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java8.util.Objects;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import puscas.mobilertapp.utils.ConstantsError;

/**
 * A customized eglCreateContext and eglDestroyContext calls.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Log
public class MyEglContextFactory implements GLSurfaceView.EGLContextFactory {

    /**
     * The "Embedded-System Graphics Library" version.
     */
    static final int EGL_CONTEXT_CLIENT_VERSION = 2;

    /**
     * The {@link GLSurfaceView} to be used to get the
     * {@link Activity#isChangingConfigurations()}.
     */
    private final DrawView drawView;

    /**
     * The {@link EGLContext} in order to prevent its destruction.
     */
    private EGLContext eglContext;

    @Nullable
    @Override
    public final EGLContext createContext(@NonNull final EGL10 egl,
                                          @NonNull final EGLDisplay display,
                                          @NonNull final EGLConfig eglConfig) {
        log.info("createContext");

        if (Objects.nonNull(this.eglContext)) {
            this.eglContext = null;
        } else {
            final int[] attribList = {
                EGL_CONTEXT_CLIENT_VERSION,
                EGL10.EGL_NONE
            };
            this.eglContext =
                egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attribList);
        }

        return this.eglContext;
    }

    @Override
    public final void destroyContext(@NonNull final EGL10 egl,
                                     @NonNull final EGLDisplay display,
                                     @NonNull final EGLContext context) {
        log.info("destroyContext");

        if (this.drawView.isChangingConfigs()) {
            this.eglContext = context;
        } else if (!egl.eglDestroyContext(display, context)) {
            throw new UnsupportedOperationException(
                ConstantsError.EGL_DESTROY_CONTEXT_FAILED + egl.eglGetError());
        }
    }

}
