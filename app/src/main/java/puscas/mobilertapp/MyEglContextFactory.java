package puscas.mobilertapp;

import android.app.Activity;
import android.opengl.GLSurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.logging.Logger;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import java8.util.Objects;
import puscas.mobilertapp.constants.ConstantsError;
import puscas.mobilertapp.exceptions.FailureException;

/**
 * A customized eglCreateContext and eglDestroyContext calls.
 */
public final class MyEglContextFactory implements GLSurfaceView.EGLContextFactory {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(MyEglContextFactory.class.getSimpleName());

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

    /**
     * The constructor.
     *
     * @param drawView The {@link GLSurfaceView} to be used to get the {@link Activity#isChangingConfigurations()}.
     */
    MyEglContextFactory(final DrawView drawView) {
        this.drawView = drawView;
    }

    @Nullable
    @Override
    public EGLContext createContext(@NonNull final EGL10 egl10,
                                    @NonNull final EGLDisplay display,
                                    @NonNull final EGLConfig eglConfig) {
        logger.info("createContext");

        final int eglError = egl10.eglGetError();
        if (eglError != EGL10.EGL_SUCCESS) {
            throw new FailureException("eglError: " + eglError);
        }

        if (Objects.nonNull(this.eglContext)) {
            logger.info("createContext delete older context");
            final EGLContext egl14Context = egl10.eglGetCurrentContext(); //get an EGL10 context representation of our EGL14 context
            destroyContext(egl10, display, egl14Context);
            destroyContext(egl10, display, this.eglContext);
            this.eglContext = null;
        } else {
            logger.info("createContext create new context");
            final int[] attribList = {
                0x3098, // the same value as EGL14.EGL_CONTEXT_CLIENT_VERSION
                EGL_CONTEXT_CLIENT_VERSION,
                EGL10.EGL_NONE
            };
            final EGLContext egl14Context = egl10.eglGetCurrentContext(); //get an EGL10 context representation of our EGL14 context
            this.eglContext = egl10.eglCreateContext(display, eglConfig, egl14Context, attribList);
        }

        logger.info("createContext finished");
        return this.eglContext;
    }

    @Override
    public void destroyContext(@NonNull final EGL10 egl10,
                               @NonNull final EGLDisplay display,
                               @NonNull final EGLContext context) {
        logger.info("destroyContext");

        if (this.drawView.isChangingConfigs()) {
            this.eglContext = context;
        } else if (!egl10.eglDestroyContext(display, context)) {
            throw new UnsupportedOperationException(
                ConstantsError.EGL_DESTROY_CONTEXT_FAILED + egl10.eglGetError());
        }

        logger.info("destroyContext finished");
    }

}
