package puscas.mobilertapp.utils;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

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
import static puscas.mobilertapp.utils.ConstantsRenderer.FIX_ASPECT_ORTHOGRAPHIC;
import static puscas.mobilertapp.utils.ConstantsRenderer.FIX_ASPECT_PERSPECTIVE;
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
     * Helper method which loads an OpenGL shader.
     *
     * @param shaderType The type of the shader (vertex or fragment shader).
     * @param source     The code of the shader.
     * @return The OpenGL index of the shader.
     */
    public static int loadShader(final int shaderType,
                                 @Nonnull final String source) {
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
    public static void connectOpenGLAttribute(@Nonnull final Buffer buffer,
                                              final int attributeLocation,
                                              final int shaderProgram,
                                              final int componentsInBuffer,
                                              @Nonnull final String attributeName) {
        LOGGER.info("connectOpenGLAttribute");
        UtilsGL.run(() -> GLES20.glBindAttribLocation(
            shaderProgram, attributeLocation, attributeName));
        UtilsGL.run(() -> GLES20.glVertexAttribPointer(attributeLocation,
            componentsInBuffer, GLES20.GL_FLOAT, false, 0, buffer));
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
                                     @Nonnull final String vertexShaderCode,
                                     @Nonnull final String fragmentShaderCode) {
        LOGGER.info("attachShaders");
        final int vertexShader = UtilsGL.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        final int fragmentShader = UtilsGL.loadShader(GLES20.GL_FRAGMENT_SHADER,
            fragmentShaderCode);

        // Attach and link shaders to program
        run(() -> GLES20.glAttachShader(shaderProgram, vertexShader));
        run(() -> GLES20.glAttachShader(shaderProgram, fragmentShader));
        run(() -> GLES20.glLinkProgram(shaderProgram));

        final int[] attachedShaders = new int[1];
        run(() -> GLES20.glGetProgramiv(shaderProgram, GLES20.GL_ATTACHED_SHADERS,
            attachedShaders, 0));

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
    @Nonnull
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
    @Nonnull
    public static float[] createProjectionMatrix(@Nonnull final ByteBuffer bbCamera,
                                                 final int width,
                                                 final int height) {
        LOGGER.info("createProjectionMatrix");

        final float fovX = bbCamera.getFloat(16 * BYTES_IN_FLOAT) * FIX_ASPECT_PERSPECTIVE;
        final float fovY = bbCamera.getFloat(17 * BYTES_IN_FLOAT) * FIX_ASPECT_PERSPECTIVE;

        final float sizeH = bbCamera.getFloat(18 * BYTES_IN_FLOAT) * FIX_ASPECT_ORTHOGRAPHIC;
        final float sizeV = bbCamera.getFloat(19 * BYTES_IN_FLOAT) * FIX_ASPECT_ORTHOGRAPHIC;

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
    public static float[] createViewMatrix(@Nonnull final ByteBuffer bbCamera) {
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

    /**
     * Helper method which checks if the Android device has support for
     * OpenGL ES 2.0.
     *
     * @return {@code True} if the device has support for OpenGL ES 2.0 or
     *         {@code False} otherwise.
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
            LOGGER.severe(msg);
            throw new FailureException(msg);
        }
    }

    /**
     * Helper method that clears the OpenGL buffers and enables some OpenGL
     * capabilities.
     */
    public static void resetOpenGLBuffers() {
        LOGGER.info("resetOpenGLBuffers");

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

}
