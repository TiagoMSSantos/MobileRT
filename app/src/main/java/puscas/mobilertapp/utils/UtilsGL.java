package puscas.mobilertapp.utils;

import android.opengl.GLES20;
import android.opengl.GLUtils;
import androidx.annotation.NonNull;
import java.util.logging.Logger;
import java8.util.function.BiFunction;
import java8.util.function.Function;
import java8.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import puscas.mobilertapp.exceptions.FailureException;

/**
 * Utility class with some helper methods for OpenGL framework.
 */
public final class UtilsGL {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(UtilsGL.class.getName());

    /**
     * A private constructor in order to prevent instantiating this helper class.
     */
    private UtilsGL() {
    }

    /**
     * Helper method that will execute a GL method and then will check for
     * any error in the OpenGL framework.
     * It is recommended to use this method when the OpenGL method called
     * doesn't return a value since by using this it will check for any
     * error after the call.
     *
     * @param method The method to call.
     */
    public static void run(@Nonnull final Runnable method) {
        LOGGER.info(ConstantsMethods.RUN);
        method.run();
        checksGLError();
    }

    /**
     * Helper method that will execute a GL method and then will check for
     * any error in the OpenGL framework.
     * It is recommended to use this method when the OpenGL method called
     * returns a value but doesn't need any arguments.
     *
     * @param method The method to call.
     */
    @Nonnull
    public static <T> T run(@Nonnull final Supplier<T> method) {
        LOGGER.info(ConstantsMethods.RUN);
        final T result = method.get();
        checksGLError();
        return result;
    }

    /**
     * Helper method that will execute a GL method and then will check for
     * any error in the OpenGL framework.
     * It is recommended to use this method when the OpenGL method called
     * receives only 1 parameter.
     *
     * @param arg    The argument for the method.
     * @param method The method to call.
     */
    @Nonnull
    public static <T, R> T run(@Nonnull final R arg,
                               @Nonnull final Function<R, T> method) {
        LOGGER.info(ConstantsMethods.RUN);
        final T result = method.apply(arg);
        checksGLError();
        return result;
    }

    /**
     * Helper method that will execute a GL method and then will check for
     * any error in the OpenGL framework.
     * It is recommended to use this method when the OpenGL method called
     * receives 2 parameters.
     *
     * @param arg1   The 1st argument for the method.
     * @param arg2   The 2nd argument for the method.
     * @param method The method to call.
     */
    @Nonnull
    public static <T, R, S> T run(@Nonnull final R arg1,
                                  @Nonnull final S arg2,
                                  @Nonnull final BiFunction<R, S, T> method) {
        LOGGER.info(ConstantsMethods.RUN);
        final T result = method.apply(arg1, arg2);
        checksGLError();
        return result;
    }

    /**
     * Helper method which checks if the Android device has support for
     * OpenGL ES 2.0.
     *
     * @return {@code True} if the device has support for OpenGL ES 2.0 or
     * {@code False} otherwise.
     */
    public static boolean checkGL20Support() {
        LOGGER.info("checkGL20Support");

        final EGL10 egl = (EGL10) EGLContext.getEGL();
        final EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        final int[] version = new int[2];
        egl.eglInitialize(display, version);

        final int[] configAttribs = {
            EGL10.EGL_RED_SIZE, 4,
            EGL10.EGL_GREEN_SIZE, 4,
            EGL10.EGL_BLUE_SIZE, 4,
            EGL10.EGL_RENDERABLE_TYPE, 4,
            EGL10.EGL_NONE
        };

        final EGLConfig[] configs = new EGLConfig[10];
        final int[] numConfig = new int[1];
        egl.eglChooseConfig(display, configAttribs, configs, 10, numConfig);
        egl.eglTerminate(display);
        return numConfig[0] > 0;
    }


    // Private methods

    /**
     * Helper method which checks and prints errors in the OpenGL framework.
     */
    private static void checksGLError() {
        final int glError = GLES20.glGetError();
        if (glError != GLES20.GL_NO_ERROR) {
            final Supplier<String> stringError = () -> {
                switch (glError) {
                    case GLES20.GL_INVALID_ENUM:
                        return "GL_INVALID_ENUM";

                    case GLES20.GL_INVALID_VALUE:
                        return "GL_INVALID_VALUE";

                    case GLES20.GL_INVALID_OPERATION:
                        return "GL_INVALID_OPERATION";

                    case GLES20.GL_OUT_OF_MEMORY:
                        return "GL_OUT_OF_MEMORY";

                    default:
                        return "GL_UNKNOWN_ERROR";
                }
            };
            final String msg = stringError.get() + ": " + GLUtils.getEGLErrorString(glError);
            throw new FailureException(msg);
        }
    }

    /**
     * Helper method that clears the OpenGL buffers and enables some OpenGL
     * capabilities.
     */
    public static void resetOpenGlBuffers() {
        LOGGER.info("resetOpenGlBuffers");

        UtilsGL.run(() -> GLES20.glClear(ConstantsRenderer.ALL_BUFFER_BIT));

        UtilsGL.run(() -> GLES20.glEnable(GLES20.GL_CULL_FACE));
        UtilsGL.run(() -> GLES20.glEnable(GLES20.GL_BLEND));
        UtilsGL.run(() -> GLES20.glEnable(GLES20.GL_DEPTH_TEST));

        UtilsGL.run(() -> GLES20.glCullFace(GLES20.GL_BACK));
        UtilsGL.run(() -> GLES20.glFrontFace(GLES20.GL_CCW));
        UtilsGL.run(() -> GLES20.glClearDepthf(1.0F));

        UtilsGL.run(() -> GLES20.glDepthMask(true));
        UtilsGL.run(() -> GLES20.glDepthFunc(GLES20.GL_LEQUAL));
        UtilsGL.run(() -> GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F));
    }

    /**
     * Helper method that generates and binds a texture name in OpenGL.
     */
    public static void bindTexture() {
        LOGGER.info("bindTexture");

        final int numTextures = 1;
        final int[] textureHandle = new int[numTextures];
        UtilsGL.run(() -> GLES20.glGenTextures(numTextures, textureHandle, 0));
        if (textureHandle[0] == 0) {
            final String msg = "Error loading texture.";
            throw new FailureException(msg);
        }

        UtilsGL.run(() -> GLES20.glActiveTexture(GLES20.GL_TEXTURE0));
        UtilsGL.run(() -> GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]));
        UtilsGL.run(() -> GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR));
        UtilsGL.run(() -> GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR));
    }

    /**
     * Helper method that disables the attributes data from indexes received via
     * argument.
     *
     * @param attributes The indexes of the attributes data.
     */
    public static void disableAttributeData(@NonNull final int... attributes) {
        LOGGER.info("disableAttributeData");

        for (final int attribute : attributes) {
            run(() -> GLES20.glDisableVertexAttribArray(attribute));
        }
    }

}
