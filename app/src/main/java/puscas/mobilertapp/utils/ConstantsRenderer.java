package puscas.mobilertapp.utils;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import puscas.mobilertapp.DrawView;
import puscas.mobilertapp.MainRenderer;
import puscas.mobilertapp.RenderTask;

/**
 * Utility class with the constants for the {@link GLSurfaceView.Renderer}.
 */
public final class ConstantsRenderer {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(ConstantsRenderer.class.getName());

    /**
     * The name for the attribute location of vertex positions in {@link GLES20}.
     */
    public static final String VERTEX_POSITION = "vertexPosition";

    /**
     * The name for the attribute location of texture coordinates in {@link GLES20}.
     */
    public static final String VERTEX_TEX_COORD = "vertexTexCoord";

    /**
     * The name for the attribute location of texture colors in {@link GLES20}.
     */
    public static final String VERTEX_COLOR = "vertexColor";

    /**
     * The number of threads to be used by the {@link DrawView},
     * {@link MainRenderer} and {@link RenderTask} {@link ExecutorService}.
     */
    public static final int NUMBER_THREADS = 1;

    /**
     * The OpenGL ES version required to run this application.
     */
    public static final int REQUIRED_OPENGL_VERSION = 0x20000;

    /**
     * The number of color components in each pixel (RGBA).
     */
    public static final int PIXEL_COLORS = 4;

    /**
     * The number of components in each vertex (X, Y, Z, W).
     */
    public static final int VERTEX_COMPONENTS = 4;

    /**
     * The number of components in each texture coordinate (X, Y).
     */
    public static final int TEXTURE_COMPONENTS = 2;

    /**
     * The minimum clipping bounds of a scene.
     */
    public static final float Z_NEAR = 0.1F;

    /**
     * The maximum clipping bounds of a scene.
     */
    public static final float Z_FAR = 1.0e+30F;

    /**
     * Empirical value that makes the OpenGL perspective camera more similar
     * to the camera from the Ray Tracing engine.
     * This value is a multiplier of the FOV values.
     */
    public static final float FIX_ASPECT_PERSPECTIVE = 0.955F;

    /**
     * Empirical value that makes the OpenGL orthographic camera more similar
     * to the camera from the Ray Tracing engine.
     * This values is a multiplier of the size values.
     */
    public static final float FIX_ASPECT_ORTHOGRAPHIC = 0.5F;

    /**
     * All the buffer bits to clear all the buffers in OpenGL.
     */
    public static final int ALL_BUFFER_BIT = GLES20.GL_COLOR_BUFFER_BIT
        | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT;

    /**
     * A private constructor in order to prevent instantiating this helper class.
     */
    private ConstantsRenderer() {
        LOGGER.info("ConstantsRenderer");
    }
}
