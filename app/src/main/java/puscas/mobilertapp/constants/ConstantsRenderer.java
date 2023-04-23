package puscas.mobilertapp.constants;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.util.concurrent.ExecutorService;

/**
 * Utility class with the constants for the {@link GLSurfaceView.Renderer}.
 */
public final class ConstantsRenderer {

    /**
     * Private constructor to avoid creating instances.
     */
    private ConstantsRenderer() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * The number of threads to be used by the DrawView,
     * MainRenderer and  RenderTask {@link ExecutorService}s.
     */
    public static final int NUMBER_THREADS = 1;

    /**
     * All the buffer bits to clear all the buffers in OpenGL.
     */
    public static final int ALL_BUFFER_BIT = GLES20.GL_COLOR_BUFFER_BIT
        | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT;

}
