package puscas.mobilertapp.utils;

import android.opengl.GLES20;
import android.opengl.GLUtils;

import androidx.annotation.NonNull;

import java.util.logging.Logger;

import java8.util.function.BiFunction;
import java8.util.function.Function;
import java8.util.function.Supplier;
import puscas.mobilertapp.exceptions.FailureException;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glShaderSource;

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
     * Helper method that initializes a GLSL program.
     * It deletes the previous GLSL program if it was created.
     */
    public static int reCreateProgram(final int shaderProgram) {
        if (shaderProgram != 0) {
            final String deleteProgramMessage = "Deleting GL program: " + shaderProgram;
            LOGGER.info(deleteProgramMessage);
            run(() -> GLES20.glDeleteProgram(shaderProgram));
        }
        final int newShaderProgram = GLES20.glCreateProgram();
        final String createProgramMessage = "Created GL program: " + newShaderProgram;
        LOGGER.info(createProgramMessage);
        checksGLError();
        return newShaderProgram;
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
    public static void run(@NonNull final Runnable method) {
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
    @NonNull
    public static <T> T run(@NonNull final Supplier<T> method) {
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
    @NonNull
    public static <T, R> T run(@NonNull final R arg, @NonNull final Function<R, T> method) {
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
    @NonNull
    public static <T, R, S> T run(@NonNull final R arg1, @NonNull final S arg2, @NonNull final BiFunction<R, S, T> method) {
        LOGGER.info(ConstantsMethods.RUN);
        final T result = method.apply(arg1, arg2);
        checksGLError();
        return result;
    }

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
            LOGGER.severe(msg);
            throw new FailureException(msg);
        }
    }

    /**
     * Helper method which loads an OpenGL shader.
     *
     * @param shaderType The type of the shader (vertex or fragment shader).
     * @param source     The code of the shader.
     * @return The OpenGL index of the shader.
     */
    public static int loadShader(final int shaderType, @NonNull final String source) {
        final int shader = glCreateShader(shaderType);
        checksGLError();
        if (shader == 0) {
            final String msg = "There was an error while creating the shader object.";
            LOGGER.severe(msg);
            throw new FailureException(msg);
        }

        run(() -> glShaderSource(shader, source));
        run(() -> glCompileShader(shader));
        final int[] compiled = new int[1];
        run(() -> glGetShaderiv(shader, GL_COMPILE_STATUS, compiled, 0));
        if (compiled[0] == 0) {
            final String informationLog = glGetShaderInfoLog(shader);
            checksGLError();
            final String msg = "Could not compile shader " + shaderType + ": " + informationLog;
            LOGGER.severe(msg);
            LOGGER.severe(source);
            run(() -> glDeleteShader(shader));
            throw new FailureException(informationLog);
        }

        return shader;
    }

}
