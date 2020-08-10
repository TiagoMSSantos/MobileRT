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
     * The number of threads to be used by the {@link DrawView},
     * {@link MainRenderer} and {@link RenderTask} {@link ExecutorService}.
     */
    public static final int NUMBER_THREADS = 1;
    /**
     * All the buffer bits to clear all the buffers in OpenGL.
     */
    public static final int ALL_BUFFER_BIT = GLES20.GL_COLOR_BUFFER_BIT
        | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT;
    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(ConstantsRenderer.class.getName());

    /**
     * A private constructor in order to prevent instantiating this helper class.
     */
    private ConstantsRenderer() {
        LOGGER.info("ConstantsRenderer");
    }
}
