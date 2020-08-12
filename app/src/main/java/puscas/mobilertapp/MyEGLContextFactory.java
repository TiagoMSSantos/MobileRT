package puscas.mobilertapp;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import java.util.logging.Logger;
import java8.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import org.jetbrains.annotations.Contract;
import puscas.mobilertapp.utils.ConstantsError;

/**
 * A customized eglCreateContext and eglDestroyContext calls.
 */
public class MyEGLContextFactory implements GLSurfaceView.EGLContextFactory {

    /**
     * The "Embedded-System Graphics Library" version.
     */
    static final int EGL_CONTEXT_CLIENT_VERSION = 2;

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(MyEGLContextFactory.class.getName());

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
     * The constructor for this class.
     *
     * @param drawView The {@link GLSurfaceView} to be used.
     */
    @Contract(pure = true) MyEGLContextFactory(final DrawView drawView) {
        this.drawView = drawView;
        this.eglContext = null;
    }

    @Nullable
    @Override
    public final EGLContext createContext(@Nonnull final EGL10 egl,
                                          @Nonnull final EGLDisplay display,
                                          @Nonnull final EGLConfig eglConfig) {
        LOGGER.info("createContext");

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
    public final void destroyContext(@Nonnull final EGL10 egl,
                                     @Nonnull final EGLDisplay display,
                                     @Nonnull final EGLContext context) {
        LOGGER.info("destroyContext");

        if (this.drawView.isChangingConfigs()) {
            this.eglContext = context;
        } else if (!egl.eglDestroyContext(display, context)) {
            throw new UnsupportedOperationException(
                ConstantsError.EGL_DESTROY_CONTEXT_FAILED + egl.eglGetError());
        }
    }

}
