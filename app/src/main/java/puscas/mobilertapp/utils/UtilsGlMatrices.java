package puscas.mobilertapp.utils;

import android.graphics.Bitmap;
import android.opengl.Matrix;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import puscas.mobilertapp.constants.Constants;

/**
 * Utility class with some helper methods for the matrices in OpenGL framework.
 */
public final class UtilsGlMatrices {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(UtilsGlMatrices.class.getSimpleName());

    /**
     * Empirical value that makes the OpenGL perspective camera more similar
     * to the camera from the Ray Tracing engine.
     * This value is a multiplier of the FOV values.
     */
    private static final float FIX_ASPECT_PERSPECTIVE = 0.955F;

    /**
     * Empirical value that makes the OpenGL orthographic camera more similar
     * to the camera from the Ray Tracing engine.
     * This values is a multiplier of the size values.
     */
    private static final float FIX_ASPECT_ORTHOGRAPHIC = 0.5F;

    /**
     * The minimum clipping bounds of a scene.
     */
    private static final float Z_NEAR = 0.1F;

    /**
     * The maximum clipping bounds of a scene.
     */
    private static final float Z_FAR = 1.0e+30F;

    /**
     * The index of FOV X in {@link ByteBuffer camera's data} of perspective camera.
     */
    @VisibleForTesting
    static final int INDEX_FOVX = 16 * Constants.BYTES_IN_FLOAT;

    /**
     * The index of FOV Y in {@link ByteBuffer camera's data} of perspective camera.
     */
    @VisibleForTesting
    static final int INDEX_FOVY = 17 * Constants.BYTES_IN_FLOAT;

    /**
     * The index of horizontal size in {@link ByteBuffer camera's data} of orthographic camera.
     */
    @VisibleForTesting
    static final int INDEX_SIZEH = 18 * Constants.BYTES_IN_FLOAT;

    /**
     * The index of vertical size in {@link ByteBuffer camera's data} of orthographic camera.
     */
    @VisibleForTesting
    static final int INDEX_SIZEY = 19 * Constants.BYTES_IN_FLOAT;

    /**
     * Private constructor to avoid creating instances.
     */
    private UtilsGlMatrices() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Creates the model matrix and sets it as an identity matrix.
     *
     * @return A float array with the model matrix data.
     */
    @NonNull
    public static float[] createModelMatrix() {
        logger.info("createModelMatrix");

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
    public static float[] createProjectionMatrix(@NonNull final ByteBuffer bbCamera,
                                                 final int width,
                                                 final int height) {
        logger.info("createProjectionMatrix");

        final float fovX = bbCamera.getFloat(INDEX_FOVX) * FIX_ASPECT_PERSPECTIVE;
        final float fovY = bbCamera.getFloat(INDEX_FOVY) * FIX_ASPECT_PERSPECTIVE;

        final float sizeH = bbCamera.getFloat(INDEX_SIZEH) * FIX_ASPECT_ORTHOGRAPHIC;
        final float sizeV = bbCamera.getFloat(INDEX_SIZEY) * FIX_ASPECT_ORTHOGRAPHIC;

        final float[] projectionMatrix = new float[16];

        if (fovX > 0.0F && fovY > 0.0F) {
            // If the camera is a perspective camera.
            final float aspectPerspective = (float) width / (float) height;
            Matrix.perspectiveM(projectionMatrix, 0, fovY, aspectPerspective, Z_NEAR, Z_FAR);
        } else if (sizeH > 0.0F && sizeV > 0.0F) {
            // If the camera is an orthographic camera.
            Matrix.orthoM(projectionMatrix, 0, -sizeH, sizeH, -sizeV, sizeV, Z_NEAR, Z_FAR);
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
    @NonNull
    public static float[] createViewMatrix(@NonNull final ByteBuffer bbCamera) {
        logger.info("createViewMatrix");

        final float eyeX = bbCamera.getFloat(0);
        final float eyeY = bbCamera.getFloat(Constants.BYTES_IN_FLOAT);
        final float eyeZ = -bbCamera.getFloat(2 * Constants.BYTES_IN_FLOAT);

        final float dirX = bbCamera.getFloat(4 * Constants.BYTES_IN_FLOAT);
        final float dirY = bbCamera.getFloat(5 * Constants.BYTES_IN_FLOAT);
        final float dirZ = -bbCamera.getFloat(6 * Constants.BYTES_IN_FLOAT);

        final float upX = bbCamera.getFloat(8 * Constants.BYTES_IN_FLOAT);
        final float upY = bbCamera.getFloat(9 * Constants.BYTES_IN_FLOAT);
        final float upZ = -bbCamera.getFloat(10 * Constants.BYTES_IN_FLOAT);

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
