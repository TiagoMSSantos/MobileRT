package puscas.mobilertapp.utils;

import android.opengl.GLES20;
import androidx.annotation.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Map;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;
import puscas.mobilertapp.configs.ConfigGlAttribute;
import puscas.mobilertapp.exceptions.FailureException;
import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glShaderSource;

/**
 * Utility class with some helper methods to create GLSL programs and load GLSL shaders.
 */
@UtilityClass
@Log
public final class UtilsShader {

    /**
     * Helper method that attaches some GLSL shaders into an OpenGL program.
     *
     * @param shaderProgram The index of OpenGL shader program.
     * @param shadersCode   The shaders' code.
     */
    public static void attachShaders(final int shaderProgram,
                                     @NonNull final Map<Integer, String> shadersCode) {
        log.info("attachShaders");

        loadAndAttachShaders(shaderProgram, shadersCode);
        UtilsGL.run(() -> GLES20.glLinkProgram(shaderProgram));

        final int[] attachedShaders = new int[1];
        UtilsGL.run(() -> GLES20.glGetProgramiv(shaderProgram, GLES20.GL_ATTACHED_SHADERS, attachedShaders, 0));

        checksShaderLinkStatus(shaderProgram);
    }

    /**
     * Helper method that checks the OpenGL shader link status in a program.
     *
     * @param shaderProgram The OpenGL shader program index.
     */
    public static void checksShaderLinkStatus(final int shaderProgram) {
        log.info("checksShaderLinkStatus");
        final int[] linkStatus = new int[1];
        UtilsGL
            .run(() -> GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0));

        if (linkStatus[0] != GLES20.GL_TRUE) {
            final String strError = UtilsGL.run(shaderProgram, GLES20::glGetProgramInfoLog);
            final String msg = "Could not link program shader: " + strError;
            UtilsGL.run(() -> GLES20.glDeleteProgram(shaderProgram));
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
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public static int loadShader(final int shaderType,
                                 @NonNull final String source) {
        log.info("loadShader");
        final int shader = UtilsGL.run(() -> glCreateShader(shaderType));
        if (shader == 0) {
            final String msg = "There was an error while creating the shader object.";
            throw new FailureException(msg);
        }

        UtilsGL.run(() -> glShaderSource(shader, source));
        UtilsGL.run(() -> glCompileShader(shader));
        final int[] compiled = new int[1];
        UtilsGL.run(() -> glGetShaderiv(shader, GL_COMPILE_STATUS, compiled, 0));
        if (compiled[0] == 0) {
            final String informationLog = UtilsGL.run(() -> glGetShaderInfoLog(shader));
            final String msg = "Could not compile shader " + shaderType + ": " + informationLog;
            log.severe(msg);
            log.severe(source);
            UtilsGL.run(() -> glDeleteShader(shader));
            throw new FailureException(informationLog);
        }

        return shader;
    }

    /**
     * Helper method that initializes a GLSL program.
     * It deletes the previous GLSL program if it was created.
     *
     * @param shaderProgram The OpenGL shader program index to recreate.
     * @return A new created OpenGL shader program index.
     */
    public static int reCreateProgram(final int shaderProgram) {
        log.info("reCreateProgram");
        if (shaderProgram != 0) {
            final String deleteProgramMessage = "Deleting GL program: " + shaderProgram;
            log.info(deleteProgramMessage);
            UtilsGL.run(() -> GLES20.glDeleteProgram(shaderProgram));
        }
        final int newShaderProgram = UtilsGL.<Integer>run(GLES20::glCreateProgram);

        if (newShaderProgram == 0) {
            log.severe("Could not create GL program.");
            final String programInfo = GLES20.glGetProgramInfoLog(0);
            throw new FailureException(programInfo);
        }

        return newShaderProgram;
    }

    /**
     * Helper method that binds and enables an OpenGL attribute.
     *
     * @param shaderProgram The shader program index.
     * @param config        The {@link ConfigGlAttribute} configurator.
     */
    public static void connectOpenGlAttribute(final int shaderProgram,
                                              @NonNull final ConfigGlAttribute config) {
        log.info("connectOpenGlAttribute");
        UtilsGL.run(() -> GLES20.glBindAttribLocation(
            shaderProgram, config.getAttributeLocation(), config.getAttributeName()));
        UtilsGL.run(() -> GLES20.glVertexAttribPointer(config.getAttributeLocation(),
            config.getComponentsInBuffer(), GLES20.GL_FLOAT, false, 0, config.getBuffer()));
        UtilsGL.run(() -> GLES20.glEnableVertexAttribArray(config.getAttributeLocation()));
    }

    /**
     * Helper method that loads a vertex and a fragment shaders and attach those
     * to a GLSL program.
     *
     * @param shaderProgram The index of OpenGL shader program.
     * @param shadersCode   The shaders' code.
     */
    public static void loadAndAttachShaders(final int shaderProgram,
                                            @NonNull final Map<Integer, String> shadersCode) {
        log.info("loadAndAttachShaders");
        final int vertexShader = getShaderIndex(shadersCode, GLES20.GL_VERTEX_SHADER);
        final int fragmentShader =  getShaderIndex(shadersCode, GLES20.GL_FRAGMENT_SHADER);

        // Attach and link shaders to program
        UtilsGL.run(() -> GLES20.glAttachShader(shaderProgram, vertexShader));
        UtilsGL.run(() -> GLES20.glAttachShader(shaderProgram, fragmentShader));
    }

    /**
     * Helper method which loads an OpenGL shader and returns the OpenGL index of the shader.
     *
     * @param shadersCode The shaders' code.
     * @param shaderType  The type of the shader (vertex or fragment shader).
     * @return The OpenGL index of the shader.
     */
    private static int getShaderIndex(@NonNull final Map<Integer, String> shadersCode, final int shaderType) {
        final String shaderCode = shadersCode.get(shaderType);
        return loadShader(shaderType, shaderCode);
    }

}
