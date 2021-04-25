package puscas.mobilertapp.utils;

import android.opengl.GLES20;
import android.opengl.GLUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import java8.util.function.BiFunction;
import java8.util.function.Function;
import java8.util.function.Supplier;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;
import puscas.mobilertapp.exceptions.FailureException;

/**
 * Utility class with some helper methods for OpenGL framework.
 */
@UtilityClass
@Log
public final class UtilsGL {

    /**
     * Helper method that will execute a GL method and then will check for
     * any error in the OpenGL framework.
     * It is recommended to use this method when the OpenGL method called
     * doesn't return a value since by using this it will check for any
     * error after the call.
     *
     * @param method The method to call.
     */
    public static void run(@NonNull final Runnable method) {
        log.info(ConstantsMethods.RUN);
        method.run();
        checksGlError();
    }

    /**
     * Helper method that will execute a GL method and then will check for
     * any error in the OpenGL framework.
     * It is recommended to use this method when the OpenGL method called
     * returns a value but doesn't need any arguments.
     *
     * @param method The {@link Supplier} to call.
     * @return The result of calling the provided {@link Supplier}.
     */
    @NonNull
    public static <T> T run(@NonNull final Supplier<T> method) {
        log.info(ConstantsMethods.RUN);
        final T result = method.get();
        checksGlError();
        return result;
    }

    /**
     * Helper method that will execute a GL method and then will check for
     * any error in the OpenGL framework.
     * It is recommended to use this method when the OpenGL method called
     * receives only 1 parameter.
     *
     * @param arg    The argument for {@link Function} method.
     * @param method The {@link Function} to call.
     * @return The result of calling the provided {@link Function}.
     */
    @NonNull
    public static <T, R> T run(@NonNull final R arg,
                               @NonNull final Function<R, T> method) {
        log.info(ConstantsMethods.RUN);
        final T result = method.apply(arg);
        checksGlError();
        return result;
    }

    /**
     * Helper method that will execute a GL method and then will check for
     * any error in the OpenGL framework.
     * It is recommended to use this method when the OpenGL method called
     * receives 2 parameters.
     *
     * @param arg1   The 1st argument for the {@link BiFunction}.
     * @param arg2   The 2nd argument for the {@link BiFunction}.
     * @param method The {@link BiFunction} to call.
     * @return The result of calling the provided {@link BiFunction}.
     */
    @NonNull
    public static <T, R, S> T run(@NonNull final R arg1,
                                  @NonNull final S arg2,
                                  @NonNull final BiFunction<R, S, T> method) {
        log.info(ConstantsMethods.RUN);
        final T result = method.apply(arg1, arg2);
        checksGlError();
        return result;
    }

    /**
     * Helper method which checks if the Android device has support for
     * OpenGL ES 2.0.
     *
     * @return {@code True} if the device has support for OpenGL ES 2.0 or
     *     {@code False} otherwise.
     */
    public static boolean checkGL20Support() {
        log.info("checkGL20Support");

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
    private static void checksGlError() {
        final int glError = GLES20.glGetError();
        if (glError != GLES20.GL_NO_ERROR) {
            final String msg = GLUtils.getEGLErrorString(glError);
            throw new FailureException(msg);
        }
    }

    /**
     * Helper method that clears the OpenGL buffers and enables some OpenGL
     * capabilities.
     */
    public static void resetOpenGlBuffers() {
        log.info("resetOpenGlBuffers");

        run(() -> GLES20.glClear(ConstantsRenderer.ALL_BUFFER_BIT));

        run(() -> GLES20.glEnable(GLES20.GL_CULL_FACE));
        run(() -> GLES20.glEnable(GLES20.GL_BLEND));
        run(() -> GLES20.glEnable(GLES20.GL_DEPTH_TEST));

        run(() -> GLES20.glCullFace(GLES20.GL_BACK));
        run(() -> GLES20.glFrontFace(GLES20.GL_CCW));
        run(() -> GLES20.glClearDepthf(1.0F));

        run(() -> GLES20.glDepthMask(true));
        run(() -> GLES20.glDepthFunc(GLES20.GL_LEQUAL));
        run(() -> GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F));
    }

    /**
     * Helper method that generates and binds a texture name in OpenGL.
     */
    public static void bindTexture() {
        log.info("bindTexture");

        final int numTextures = 1;
        final int[] textureHandle = new int[numTextures];
        run(() -> GLES20.glGenTextures(numTextures, textureHandle, 0));
        if (textureHandle[0] == 0) {
            final String msg = "Error loading texture.";
            throw new FailureException(msg);
        }

        run(() -> GLES20.glActiveTexture(GLES20.GL_TEXTURE0));
        run(() -> GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]));
        run(() -> GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR));
        run(() -> GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR));
    }

    /**
     * Helper method that disables the attributes data from indexes received via
     * argument.
     *
     * @param attributes The indexes of the attributes data.
     */
    public static void disableAttributeData(@NonNull final int... attributes) {
        log.info("disableAttributeData");

        for (final int attribute : attributes) {
            run(() -> GLES20.glDisableVertexAttribArray(attribute));
        }
    }

}
