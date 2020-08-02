package puscas.mobilertapp.utils;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import androidx.annotation.NonNull;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

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
import static puscas.mobilertapp.utils.Constants.BYTES_IN_FLOAT;
import static puscas.mobilertapp.utils.ConstantsRenderer.Z_FAR;
import static puscas.mobilertapp.utils.ConstantsRenderer.Z_NEAR;

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
     *
     * @param shaderProgram The OpenGL shader program index to recreate.
     * @return A new created OpenGL shader program index.
     */
    public static int reCreateProgram(final int shaderProgram) {
        LOGGER.info("reCreateProgram");
        if (shaderProgram != 0) {
            final String deleteProgramMessage = "Deleting GL program: " + shaderProgram;
            LOGGER.info(deleteProgramMessage);
            run(() -> GLES20.glDeleteProgram(shaderProgram));
        }
        final int newShaderProgram = UtilsGL.<Integer>run(() -> GLES20.glCreateProgram());

        if (newShaderProgram == 0) {
            LOGGER.severe("Could not create program: ");
            final String programInfo = GLES20.glGetProgramInfoLog(0);
            throw new FailureException(programInfo);
        }

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
        LOGGER.info("loadShader");
        final int shader = run(() -> glCreateShader(shaderType));
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
            final String informationLog = run(() -> glGetShaderInfoLog(shader));
            final String msg = "Could not compile shader " + shaderType + ": " + informationLog;
            LOGGER.severe(msg);
            LOGGER.severe(source);
            run(() -> glDeleteShader(shader));
            throw new FailureException(informationLog);
        }

        return shader;
    }

    /**
     * Helper method that binds and enables an OpenGL attribute.
     *
     * @param buffer             The {@link Buffer} with the data to bind.
     * @param attributeLocation  The desired location of the OpenGL attribute.
     * @param shaderProgram      The shader program index.
     * @param componentsInBuffer The number of components in each cell in the
     *                           {@link ByteBuffer}.
     * @param attributeName      The name of the OpenGL attribute.
     */
    public static void connectOpenGLAttribute(@NonNull final Buffer buffer,
                                              final int attributeLocation,
                                              final int shaderProgram,
                                              final int componentsInBuffer,
                                              @NonNull final String attributeName) {
        LOGGER.info("connectOpenGLAttribute");
        UtilsGL.run(() -> GLES20.glBindAttribLocation(shaderProgram, attributeLocation, attributeName));
        UtilsGL.run(() -> GLES20.glVertexAttribPointer(attributeLocation, componentsInBuffer, GLES20.GL_FLOAT, false, 0, buffer));
        UtilsGL.run(() -> GLES20.glEnableVertexAttribArray(attributeLocation));
    }

    /**
     * Helper method that attaches some GLSL shaders into an OpenGL program.
     *
     * @param shaderProgram      The OpenGL shader program-
     * @param vertexShaderCode   The code for the Vertex shader.
     * @param fragmentShaderCode The code for the Fragment shader.
     */
    public static void attachShaders(final int shaderProgram,
                                     @NonNull final String vertexShaderCode,
                                     @NonNull final String fragmentShaderCode) {
        LOGGER.info("attachShaders");
        final int vertexShader = UtilsGL.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        final int fragmentShader = UtilsGL.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // Attach and link shaders to program
        run(() -> GLES20.glAttachShader(shaderProgram, vertexShader));
        run(() -> GLES20.glAttachShader(shaderProgram, fragmentShader));
        run(() -> GLES20.glLinkProgram(shaderProgram));

        final int[] attachedShaders = new int[1];
        run(() -> GLES20.glGetProgramiv(shaderProgram, GLES20.GL_ATTACHED_SHADERS, attachedShaders, 0));

        checksShaderLinkStatus(shaderProgram);
    }

    /**
     * Helper method that checks the OpenGL shader link status in a program.
     *
     * @param shaderProgram The OpenGL shader program index.
     */
    public static void checksShaderLinkStatus(final int shaderProgram) {
        LOGGER.info("checksShaderLinkStatus");
        final int[] linkStatus = new int[1];
        run(() -> GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0));

        if (linkStatus[0] != GLES20.GL_TRUE) {
            final String strError = run(shaderProgram, GLES20::glGetProgramInfoLog);
            final String msg = "Could not link program rasterizer: " + strError;
            LOGGER.severe(msg);
            run(() -> GLES20.glDeleteProgram(shaderProgram));
            throw new FailureException(strError);
        }
    }

    /**
     * Creates the model matrix and sets it as an identity matrix.
     *
     * @return A float array with the model matrix data.
     */
    @NonNull
    public static float[] createModelMatrix() {
        LOGGER.info("createModelMatrix");

        final float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
        return modelMatrix;
    }

    /**
     * Creates the projection matrix by using the camera's data from a
     * {@link ByteBuffer} read from the Ray Tracing engine.
     *
     * @param bbCamera The camera's data (like FOV or size).
     * @param width    The width of the {@link Bitmap} to render.
     * @param height   The height of the {@link Bitmap} to render.
     * @return A float array with the projection matrix data.
     */
    @NonNull
    public static float[] createProjectionMatrix(@NonNull final ByteBuffer bbCamera, final int width, final int height) {
        LOGGER.info("createProjectionMatrix");

        final float fovX = bbCamera.getFloat(16 * BYTES_IN_FLOAT) * ConstantsRenderer.FIX_ASPECT_PERSPECTIVE;
        final float fovY = bbCamera.getFloat(17 * BYTES_IN_FLOAT) * ConstantsRenderer.FIX_ASPECT_PERSPECTIVE;

        final float sizeH = bbCamera.getFloat(18 * BYTES_IN_FLOAT) * ConstantsRenderer.FIX_ASPECT_ORTHOGRAPHIC;
        final float sizeV = bbCamera.getFloat(19 * BYTES_IN_FLOAT) * ConstantsRenderer.FIX_ASPECT_ORTHOGRAPHIC;

        final float[] projectionMatrix = new float[16];

        // If the camera is a perspective camera.
        if (fovX > 0.0F && fovY > 0.0F) {
            final float aspectPerspective = (float) width / (float) height;
            Matrix.perspectiveM(projectionMatrix, 0, fovY, aspectPerspective, Z_NEAR, Z_FAR);
        }

        // If the camera is an orthographic camera.
        if (sizeH > 0.0F && sizeV > 0.0F) {
            Matrix.orthoM(projectionMatrix, 0, -sizeH, sizeH,
                -sizeV, sizeV, Z_NEAR, Z_FAR);
        }

        return projectionMatrix;
    }

    /**
     * Creates the view matrix by using the camera's data from a
     * {@link ByteBuffer} read from the Ray Tracing engine.
     *
     * @param bbCamera The camera's data (like eye, direction and up vector).
     * @return A float array with the view matrix data.
     */
    @Nonnull
    public static float[] createViewMatrix(@NonNull final ByteBuffer bbCamera) {
        LOGGER.info("createViewMatrix");

        final float eyeX = bbCamera.getFloat(0);
        final float eyeY = bbCamera.getFloat(BYTES_IN_FLOAT);
        final float eyeZ = -bbCamera.getFloat(2 * BYTES_IN_FLOAT);

        final float dirX = bbCamera.getFloat(4 * BYTES_IN_FLOAT);
        final float dirY = bbCamera.getFloat(5 * BYTES_IN_FLOAT);
        final float dirZ = -bbCamera.getFloat(6 * BYTES_IN_FLOAT);

        final float upX = bbCamera.getFloat(8 * BYTES_IN_FLOAT);
        final float upY = bbCamera.getFloat(9 * BYTES_IN_FLOAT);
        final float upZ = -bbCamera.getFloat(10 * BYTES_IN_FLOAT);

        final float centerX = eyeX + dirX;
        final float centerY = eyeY + dirY;
        final float centerZ = eyeZ + dirZ;

        final float[] viewMatrix = new float[16];

        Matrix.setLookAtM(
            viewMatrix, 0,
            eyeX, eyeY, eyeZ,
            centerX, centerY, centerZ,
            upX, upY, upZ
        );

        return viewMatrix;
    }

}
