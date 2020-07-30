package puscas.mobilertapp;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.Uninterruptibles;

import org.jetbrains.annotations.Contract;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java8.util.Optional;
import puscas.mobilertapp.exceptions.FailureException;
import puscas.mobilertapp.exceptions.LowMemoryException;
import puscas.mobilertapp.utils.ConstantsRenderer;
import puscas.mobilertapp.utils.State;
import puscas.mobilertapp.utils.Utils;
import puscas.mobilertapp.utils.UtilsGL;

/**
 * The OpenGL renderer that shows the Ray Tracer engine rendered image.
 */
public final class MainRenderer implements GLSurfaceView.Renderer {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(MainRenderer.class.getName());

    /**
     * The default update interval in milliseconds of {@link RenderTask}.
     */
    private static final long DEFAULT_UPDATE_INTERVAL = 250L;

    /**
     * The vertices coordinates for the texture where the Ray Tracer {@link Bitmap} will be applied.
     */
    private final float[] verticesTexture = {
            -1.0F, 1.0F, 0.0F, 1.0F,
            -1.0F, -1.0F, 0.0F, 1.0F,
            1.0F, -1.0F, 0.0F, 1.0F,
            1.0F, 1.0F, 0.0F, 1.0F,
    };

    /**
     * The texture coordinates of the texture containing the Ray Tracer {@link Bitmap}.
     */
    private final float[] texCoords = {
            0.0F, 0.0F,
            0.0F, 1.0F,
            1.0F, 1.0F,
            1.0F, 0.0F
    };

    /**
     * Some information about the memory like the available memory on the system.
     */
    private final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();

    /**
     * A {@link Lock} for the {@link ExecutorService} containing {@link ConstantsRenderer#NUMBER_THREADS} thread where
     * the {@link RenderTask} will be executed.
     */
    private final Lock lockExecutorService = new ReentrantLock();

    /**
     * The vertex shader code.
     */
    private String vertexShaderCode = null;

    /**
     * The fragment shader code.
     */
    private String fragmentShaderCode = null;

    /**
     * The vertex shader code for the rasterizer.
     */
    private String vertexShaderCodeRaster = null;

    /**
     * The fragment shader code for the rasterizer.
     */
    private String fragmentShaderCodeRaster = null;

    /**
     * The {@link ActivityManager} used to get information about the memory state of the system.
     *
     * @see ActivityManager#getMemoryInfo(ActivityManager.MemoryInfo)
     */
    private ActivityManager activityManager = null;

    /**
     * The {@link Bitmap} where the Ray Tracer engine will render the scene.
     */
    private Bitmap bitmap = null;

    /**
     * The number of threads to be used by the Ray Tracer engine.
     */
    private int numThreads = 0;

    /**
     * The number of primitives in the scene.
     */
    private int numPrimitives = 0;

    /**
     * The number of lights in the scene.
     */
    private int numLights = 0;

    /**
     * Whether should rasterize (render preview) or not.
     */
    private boolean rasterize = false;

    /**
     * The vertices positions of the {@link MainRenderer#verticesTexture}.
     */
    private FloatBuffer floatBufferVertices = null;

    /**
     * The texture coordinates of the {@link MainRenderer#texCoords}.
     */
    private FloatBuffer floatBufferTexture = null;

    /**
     * The vertices positions in the scene.
     */
    private ByteBuffer arrayVertices = null;

    /**
     * The vertices colors in the scene.
     */
    private ByteBuffer arrayColors = null;

    /**
     * The camera (e.g.: eye, direction, up and fov) in the scene.
     */
    private ByteBuffer arrayCamera = null;

    /**
     * A reference to the {@link DrawView#requestRender()} method.
     */
    private Runnable requestRender = null;

    /**
     * The width of the {@link Bitmap} where the Ray Tracer engine will render the scene.
     */
    private int width = 1;

    /**
     * The height of the {@link Bitmap} where the Ray Tracer engine will render the scene.
     */
    private int height = 1;

    /**
     * The width of the {@link DrawView}.
     */
    private int viewWidth = 1;

    /**
     * The height of the {@link DrawView}.
     */
    private int viewHeight = 1;

    /**
     * The OpenGL program shader for the 2 triangles containing a texture with {@link Bitmap} for the rendered image.
     */
    private int shaderProgram = 0;

    /**
     * The OpenGL program shader for the rasterization of the scene (preview).
     */
    private int shaderProgramRaster = 0;

    /**
     * Determine if it is the first frame to render.
     * It is important because it should only call the Ray Tracer engine at the first frame and the others just
     * update the texture with the {@link Bitmap}.
     */
    private boolean firstFrame = false;

    /**
     * The {@link TextView} which will output the debug information about the Ray Tracer engine.
     */
    private TextView textView = null;

    /**
     * The {@link Button} which can start and stop the Ray Tracer engine.
     * It is important to let the {@link RenderTask} update its state after the rendering process.
     */
    private Button buttonRender = null;

    /**
     * The number of samples per pixel.
     */
    private int samplesPixel = 0;

    /**
     * The number of samples per light.
     */
    private int samplesLight = 0;

    /**
     * A custom {@link AsyncTask} which will update the {@link View} with the updated {@link Bitmap} and debug
     * information.
     */
    private RenderTask renderTask = null;

    /**
     * A thread pool containing {@link ConstantsRenderer#NUMBER_THREADS} threads with the purpose of executing the
     * {@link MainRenderer#renderTask}.
     */
    private ExecutorService executorService = Executors.newFixedThreadPool(ConstantsRenderer.NUMBER_THREADS);

    /**
     * Sets the {@link MainRenderer#textView}.
     *
     * @param textView The new {@link TextView} to set.
     */
    void setTextView(final TextView textView) {
        this.textView = textView;
    }

    /**
     * Updates the text in the render {@link Button}.
     *
     * @param state The resource identifier of the string resource to be displayed.
     */
    void updateButton(final int state) {
        this.buttonRender.setText(state);
    }

    /**
     * Sets the {@link MainRenderer#buttonRender}.
     *
     * @param buttonRender The new {@link Button} to set.
     */
    void setButtonRender(final Button buttonRender) {
        this.buttonRender = buttonRender;
    }

    /**
     * Gets an {@code int} which represents the current Ray Tracer engine {@link State}.
     *
     * @return The current Ray Tracer engine {@link State}.
     */
    State getState() {
        LOGGER.info("getState");

        while (this.firstFrame) {
            LOGGER.info("Waiting for the onDraw to start the RT engine!!!");
            Uninterruptibles.sleepUninterruptibly(500L, TimeUnit.MILLISECONDS);
            LOGGER.info("Waited for the onDraw to start the RT engine!!!");
        }

        return Optional.ofNullable(this.renderTask)
            .map(task -> State.values()[task.rtGetState()])
            .orElse(State.BUSY);
    }

    /**
     * Resets some stats about the Ray Tracer engine.
     *
     * @param numThreads    The number of threads.
     * @param samplesPixel  The number of samples per pixel.
     * @param samplesLight  The number of samples per light.
     * @param numPrimitives The number of primitives in the scene.
     * @param numLights     The number of lights in the scene.
     */
    void resetStats(final int numThreads,
                    final int samplesPixel,
                    final int samplesLight,
                    final int numPrimitives,
                    final int numLights) {
        LOGGER.info("resetStats");
        final String numThreadsStr = String.format(Locale.US, "numThreads: %d", numThreads);
        final String numPrimitivesStr = String.format(Locale.US, "numPrimitives: %d", numPrimitives);
        final String numLightsStr = String.format(Locale.US, "numLights: %d", numLights);
        final String samplesPixelStr = String.format(Locale.US, "samplesPixel: %d", samplesPixel);
        final String samplesLightStr = String.format(Locale.US, "samplesLight: %d", samplesLight);
        LOGGER.info(numThreadsStr);
        LOGGER.info(numPrimitivesStr);
        LOGGER.info(numLightsStr);
        LOGGER.info(samplesPixelStr);
        LOGGER.info(samplesLightStr);

        this.numThreads = numThreads;
        this.samplesPixel = samplesPixel;
        this.samplesLight = samplesLight;
        this.numPrimitives = numPrimitives;
        this.numLights = numLights;
    }

    /**
     * Stops the Ray Tracer engine and updates its {@link State} to {@link State#IDLE}.
     */
    native void rtFinishRender();

    /**
     * Loads the scene and constructs the Ray Tracer renderer.
     *
     * @param config The ray tracer configuration.
     * @return The number of primitives or a negative value if an error occurs.
     */
    native int rtInitialize(Config config) throws LowMemoryException;

    /**
     * Let Ray Tracer engine start to render the scene.
     * It can render synchronously or asynchronously controlled by the {@code async} argument.
     *
     * @param image      The {@link Bitmap} where the Ray Tracer will render the scene into.
     * @param numThreads The number of threads to be used by the Ray Tracer engine.
     * @param async      If {@code true} let the Ray Tracer engine render the scene asynchronously or otherwise
     *                   synchronously.
     */
    private native void rtRenderIntoBitmap(Bitmap image, int numThreads, boolean async) throws LowMemoryException;

    /**
     * Creates a native array with all the positions of triangles in the scene.
     *
     * @return A new array with all the primitives' vertices.
     */
    private native ByteBuffer rtInitVerticesArray() throws LowMemoryException;

    /**
     * Creates a native array with all the colors of triangles in the scene.
     *
     * @return A new array with all the primitives' colors.
     */
    private native ByteBuffer rtInitColorsArray() throws LowMemoryException;

    /**
     * Creates a native array with the camera's position, direction, up and right vectors in the scene.
     *
     * @return A new array with the camera's position and vectors.
     */
    private native ByteBuffer rtInitCameraArray() throws LowMemoryException;

    /**
     * Free the memory of a native array.
     * The memory allocated with {@link MainRenderer#rtInitVerticesArray()},
     * {@link MainRenderer#rtInitColorsArray()} and {@link MainRenderer#rtInitCameraArray()} methods should be
     * free using this method.
     *
     * @param byteBuffer A reference to {@link ByteBuffer} to free its memory.
     * @return A {@code null} reference.
     */
    private native ByteBuffer rtFreeNativeBuffer(final ByteBuffer byteBuffer);

    /**
     * Free the memory of {@link MainRenderer#arrayVertices}, {@link MainRenderer#arrayColors} and
     * {@link MainRenderer#arrayCamera} native arrays.
     */
    void freeArrays() {
        LOGGER.info("freeArrays");
        this.arrayVertices = rtFreeNativeBuffer(this.arrayVertices);
        this.arrayColors = rtFreeNativeBuffer(this.arrayColors);
        this.arrayCamera = rtFreeNativeBuffer(this.arrayCamera);
    }

    /**
     * Helper method which initializes the {@link MainRenderer#arrayVertices}, {@link MainRenderer#arrayColors} and
     * {@link MainRenderer#arrayCamera} native arrays.
     */
    private void initArrays() throws LowMemoryException {
        LOGGER.info("initArrays");
        checksFreeMemory(1, this::freeArrays);

        this.arrayVertices = rtInitVerticesArray();
        checksFreeMemory(1, this::freeArrays);

        this.arrayColors = rtInitColorsArray();
        checksFreeMemory(1, this::freeArrays);

        this.arrayCamera = rtInitCameraArray();
        checksFreeMemory(1, this::freeArrays);

        validateArrays();
    }

    /**
     * Helper method which verifies if the Android device has low free memory.
     *
     * @param memoryNeeded Number of MegaBytes needed to be allocated.
     * @return {@code True} if the device doesn't have enough memory to be allocated, otherwise {@code false}.
     */
    private boolean isLowMemory(final int memoryNeeded) {
        Preconditions.checkArgument(memoryNeeded > 0, "The requested memory must be a positive value");

        this.activityManager.getMemoryInfo(this.memoryInfo);
        final long availMem = this.memoryInfo.availMem / 1048576L;
        final long totalMem = this.memoryInfo.totalMem / 1048576L;
        final boolean insufficientMem = availMem <= (long) (1 + memoryNeeded);
        final String message = String.format(Locale.US, "MEMORY AVAILABLE: %dMB (%dMB)", availMem, totalMem);
        LOGGER.info(message);
        return insufficientMem || this.memoryInfo.lowMemory;
    }

    /**
     * Helper method which checks if the Android device has low free memory.
     *
     * @param memoryNeeded Number of MegaBytes needed to be allocated.
     * @param function     The function to execute if the device has low free memory.
     * @throws LowMemoryException If the device has low free memory.
     */
    private void checksFreeMemory(final int memoryNeeded, final Runnable function) throws LowMemoryException {
        LOGGER.info("checksFreeMemory");
        if (isLowMemory(memoryNeeded)) {
            function.run();
            throw new LowMemoryException();
        }
    }

    /**
     * Prepares this object by setting up the {@link MainRenderer#requestRender} and
     * {@link MainRenderer#renderTask} fields.
     *
     * @param requestRender A {@link Runnable} of {@link GLSurfaceView#requestRender()} method.
     */
    void prepareRenderer(final Runnable requestRender) {
        LOGGER.info("prepareRenderer");

        this.requestRender = requestRender;
    }

    /**
     * Creates a new {@link Bitmap} with the size of {@code width} and {@code height} and also sets the
     * {@link MainRenderer#viewWidth} and {@link MainRenderer#viewHeight} fields.
     *
     * @param width      The width of the new {@link Bitmap}.
     * @param height     The height of the new {@link Bitmap}.
     * @param widthView  The width of the {@link GLSurfaceView}.
     * @param heightView The height of the {@link GLSurfaceView}.
     * @param rasterize  The new {@link MainRenderer#rasterize}.
     */
    void setBitmap(final int width, final int height, final int widthView, final int heightView, final boolean rasterize) {
        LOGGER.info("setBitmap");
        this.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        this.bitmap.eraseColor(Color.BLACK);
        this.width = width;
        this.height = height;
        this.viewWidth = widthView;
        this.viewHeight = heightView;
        this.firstFrame = true;
        this.rasterize = rasterize;
        validateBitmap();
        LOGGER.info("setBitmap finished");
    }

    /**
     * Converts a pixel from OpenGL format to a pixel of Android format.
     *
     * @param pixel A pixel from OpenGL format.
     * @return A pixel from Android format.
     */
    @Contract(pure = true)
    private static int convertPixelOpenGLToAndroid(final int pixel) {
        final int red = pixel & 0xFF;
        final int green = (pixel >> 8) & 0xFF;
        final int blue = (pixel >> (2 * 8)) & 0xFF;
        final int alpha = (pixel >> (3 * 8)) & 0xFF;
        final int newPixel = (red << (2 * 8)) | (green << 8) | blue;
        return alpha << (3 * 8) | newPixel;
    }

    /**
     * Converts an index of a pixel from OpenGL format to an index of a pixel of Android format.
     *
     * @param index An index of a pixel from OpenGL format.
     * @return An index of a pixel from Android format.
     */
    @Contract(pure = true)
    private int convertIndexOpenGLToAndroid(final int index) {
        final int column = index % this.viewWidth;
        final int line = index / this.viewWidth;
        return (this.viewHeight - line - 1) * this.viewWidth + column;
    }

    /**
     * Helper method that reads and copies the pixels in the OpenGL frame buffer to a new {@link Bitmap}.
     *
     * @return A new {@link Bitmap} with the colors of the pixels in the OpenGL frame buffer.
     */
    private Bitmap copyFrameBuffer() {
        final int sizePixels = this.viewWidth * this.viewHeight;
        final int[] arrayBytesPixels = new int[sizePixels];
        final int[] arrayBytesNewBitmap = new int[sizePixels];
        final IntBuffer intBuffer = IntBuffer.wrap(arrayBytesPixels);
        intBuffer.position(0);

        UtilsGL.run(() -> GLES20.glReadPixels(
            0, 0, this.viewWidth, this.viewHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, intBuffer
        ));

        int openGLIndex = 0;
        for (final int pixel : arrayBytesPixels) {
            final int androidIndex = convertIndexOpenGLToAndroid(openGLIndex);
            ++openGLIndex;
            arrayBytesNewBitmap[androidIndex] = convertPixelOpenGLToAndroid(pixel);
        }

        final Bitmap bitmapView = Bitmap.createBitmap(
            arrayBytesNewBitmap, this.viewWidth, this.viewHeight, Bitmap.Config.ARGB_8888
        );
        final Bitmap newBitmapWithPreviewScene = Bitmap.createScaledBitmap(bitmapView, this.width, this.height, true);
        Preconditions.checkArgument(bitmapView.getWidth() == this.viewWidth);
        Preconditions.checkArgument(bitmapView.getHeight() == this.viewHeight);
        return newBitmapWithPreviewScene;
    }

    /**
     * Shuts down and waits for the {@link MainRenderer#executorService} to terminate.
     * In the end, resets {@link MainRenderer#executorService} to a new thread pool with
     * {@link ConstantsRenderer#NUMBER_THREADS} threads.
     */
    void waitLastTask() {
        LOGGER.info("waitLastTask");

        if (this.renderTask != null) {
            try {
                this.renderTask.get(1L, TimeUnit.DAYS);
                this.renderTask.cancel(false);
            } catch (final ExecutionException | TimeoutException | CancellationException ex) {
                LOGGER.severe("waitLastTask exception 1: " + ex.getClass().getName());
                LOGGER.severe("waitLastTask exception 2: " + Strings.nullToEmpty(ex.getMessage()));
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                Utils.handleInterruption("MainRenderer#waitLastTask");
            }
        }

        LOGGER.info("waitLastTask renderTask cancelled");
        this.lockExecutorService.lock();
        try {
            this.executorService.shutdown();
            Utils.waitExecutorToFinish(this.executorService);
            this.executorService = Executors.newFixedThreadPool(ConstantsRenderer.NUMBER_THREADS);
        } finally {
            this.lockExecutorService.unlock();
        }

        LOGGER.info("waitLastTask finished");
    }

    /**
     * Helper method which rasterizes a frame by using the camera and the primitives received by parameters in the
     * OpenGL pipeline.
     *
     * @param bbVertices    The primitives' vertices in the scene.
     * @param bbColors      The primitives' colors in the scene.
     * @param bbCamera      The camera's position and vectors in the scene.
     * @param numPrimitives The number of primitives in the scene.
     * @throws LowMemoryException This {@link Exception} is thrown if the Android device has low free memory.
     */
    private Bitmap copyFrame(
            @Nonnull final ByteBuffer bbVertices,
            @Nonnull final ByteBuffer bbColors,
            @Nonnull final ByteBuffer bbCamera,
            final int numPrimitives
    ) throws LowMemoryException {
        LOGGER.info("copyFrame");
        if (bbVertices.capacity() <= 0 || bbColors.capacity() <= 0 || bbCamera.capacity() <= 0 || numPrimitives <= 0) {
            return this.bitmap;
        }
        UtilsGL.run(() -> GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT));

        final int neededMemoryMb = Utils.calculateSceneSize(numPrimitives);
        checksFreeMemory(neededMemoryMb, () -> { });

        bbVertices.order(ByteOrder.nativeOrder());
        bbVertices.position(0);
        checksFreeMemory(1, () -> { });

        bbColors.order(ByteOrder.nativeOrder());
        bbColors.position(0);
        checksFreeMemory(1, () -> { });

        bbCamera.order(ByteOrder.nativeOrder());
        bbCamera.position(0);
        checksFreeMemory(1, () -> { });

        this.shaderProgramRaster = UtilsGL.reCreateProgram(this.shaderProgramRaster);

        final int positionAttrib2 = 0;
        UtilsGL.run(() -> GLES20.glBindAttribLocation(this.shaderProgramRaster, positionAttrib2, ConstantsRenderer.VERTEX_POSITION));
        UtilsGL.run(() -> GLES20.glVertexAttribPointer(positionAttrib2, 4, GLES20.GL_FLOAT, false, 0, bbVertices));
        UtilsGL.run(() -> GLES20.glEnableVertexAttribArray(positionAttrib2));

        final int colorAttrib2 = 1;
        UtilsGL.run(() -> GLES20.glBindAttribLocation(this.shaderProgramRaster, colorAttrib2, ConstantsRenderer.VERTEX_COLOR));
        UtilsGL.run(() -> GLES20.glVertexAttribPointer(colorAttrib2, 4, GLES20.GL_FLOAT, false, 0, bbColors));
        UtilsGL.run(() -> GLES20.glEnableVertexAttribArray(colorAttrib2));

        // Load shaders
        final int vertexShaderRaster = UtilsGL.loadShader(GLES20.GL_VERTEX_SHADER, this.vertexShaderCodeRaster);
        final int fragmentShaderRaster = UtilsGL.loadShader(GLES20.GL_FRAGMENT_SHADER, this.fragmentShaderCodeRaster);

        // Attach and link shaders to program
        UtilsGL.run(() -> GLES20.glAttachShader(this.shaderProgramRaster, vertexShaderRaster));
        UtilsGL.run(() -> GLES20.glAttachShader(this.shaderProgramRaster, fragmentShaderRaster));
        UtilsGL.run(() -> GLES20.glLinkProgram(this.shaderProgramRaster));

        final int[] attachedShadersRaster = new int[1];
        UtilsGL.run(() -> GLES20.glGetProgramiv(this.shaderProgramRaster, GLES20.GL_ATTACHED_SHADERS, attachedShadersRaster, 0));

        final int[] linkStatusRaster = new int[1];
        UtilsGL.run(() -> GLES20.glGetProgramiv(this.shaderProgramRaster, GLES20.GL_LINK_STATUS, linkStatusRaster, 0));

        checksFreeMemory(1, () -> { });

        if (linkStatusRaster[0] != GLES20.GL_TRUE) {
            final String strError = UtilsGL.run(this.shaderProgramRaster, GLES20::glGetProgramInfoLog);
            final String msg = "attachedShadersRaster = " + attachedShadersRaster[0];
            final String msg2 = "Could not link program rasterizer: " + strError;
            LOGGER.severe(msg);
            LOGGER.severe(msg2);
            UtilsGL.run(() -> GLES20.glDeleteProgram(this.shaderProgramRaster));
            throw new FailureException(strError);
        }


        UtilsGL.run(() -> GLES20.glUseProgram(this.shaderProgramRaster));

        final float zNear = 0.1F;
        final float zFar = 1.0e38F;

        final int floatSize = Float.SIZE / Byte.SIZE;
        final float eyeX = bbCamera.getFloat(0);
        final float eyeY = bbCamera.getFloat(floatSize);
        final float eyeZ = -bbCamera.getFloat(2 * floatSize);

        final float dirX = bbCamera.getFloat(4 * floatSize);
        final float dirY = bbCamera.getFloat(5 * floatSize);
        final float dirZ = -bbCamera.getFloat(6 * floatSize);

        final float upX = bbCamera.getFloat(8 * floatSize);
        final float upY = bbCamera.getFloat(9 * floatSize);
        final float upZ = -bbCamera.getFloat(10 * floatSize);

        final float centerX = eyeX + dirX;
        final float centerY = eyeY + dirY;
        final float centerZ = eyeZ + dirZ;

        final float aspect = (float) this.width / (float) this.height;
        final float fixAspect = 0.955F;
        final float fovX = bbCamera.getFloat(16 * floatSize) * fixAspect;
        final float fovY = bbCamera.getFloat(17 * floatSize) * fixAspect;

        final float sizeH = bbCamera.getFloat(18 * floatSize);
        final float sizeV = bbCamera.getFloat(19 * floatSize);

        final float[] projectionMatrix = new float[16];
        final float[] viewMatrix = new float[16];
        final float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);

        if (fovX > 0.0F && fovY > 0.0F) {
            Matrix.perspectiveM(projectionMatrix, 0, fovY, aspect, zNear, zFar);
        }

        if (sizeH > 0.0F && sizeV > 0.0F) {
            final float correction = 2.0F;
            Matrix.orthoM(projectionMatrix, 0, -sizeH / correction, sizeH / correction,
            -sizeV / correction, sizeV / correction, zNear, zFar);
        }

        Matrix.setLookAtM(
            viewMatrix, 0,
            eyeX, eyeY, eyeZ,
            centerX, centerY, centerZ,
            upX, upY, upZ
        );
        final int handleModel = UtilsGL.<Integer, Integer, String>run(this.shaderProgramRaster, "uniformModelMatrix", GLES20::glGetUniformLocation);
        final int handleView = UtilsGL.<Integer, Integer, String>run(this.shaderProgramRaster, "uniformViewMatrix", GLES20::glGetUniformLocation);
        final int handleProjection = UtilsGL.<Integer, Integer, String>run(this.shaderProgramRaster, "uniformProjectionMatrix", GLES20::glGetUniformLocation);

        UtilsGL.run(() -> GLES20.glUniformMatrix4fv(handleModel, 1, false, modelMatrix, 0));
        UtilsGL.run(() -> GLES20.glUniformMatrix4fv(handleView, 1, false, viewMatrix, 0));
        UtilsGL.run(() -> GLES20.glUniformMatrix4fv(handleProjection, 1, false, projectionMatrix, 0));

        checksFreeMemory(1, () -> { });

        final int positionAttrib = UtilsGL.<Integer, Integer, String>run(this.shaderProgramRaster, ConstantsRenderer.VERTEX_POSITION, GLES20::glGetAttribLocation);
        UtilsGL.run(() -> GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, positionAttrib));
        UtilsGL.run(() -> GLES20.glEnableVertexAttribArray(positionAttrib));
        UtilsGL.run(() -> GLES20.glVertexAttribPointer(positionAttrib, 4, GLES20.GL_FLOAT, false, 0, bbVertices));

        checksFreeMemory(1, () -> { });

        final int colorAttrib = UtilsGL.<Integer, Integer, String>run(this.shaderProgramRaster, ConstantsRenderer.VERTEX_COLOR, GLES20::glGetAttribLocation);
        UtilsGL.run(() -> GLES20.glEnableVertexAttribArray(colorAttrib));
        UtilsGL.run(() -> GLES20.glVertexAttribPointer(colorAttrib, 4, GLES20.GL_FLOAT, false, 0, bbColors));

        checksFreeMemory(1, () -> { });

        UtilsGL.run(() -> GLES20.glEnable(GLES20.GL_DEPTH_TEST));

        final int vertexCount = bbVertices.capacity() / (floatSize << 2);
        final String msg = String.format(Locale.US, "vertexCount: %d", vertexCount);
        LOGGER.info(msg);

        checksFreeMemory(1, () -> { });

        UtilsGL.run(() -> GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount));
        LOGGER.info("glDrawArrays Complete");

        UtilsGL.run(() -> GLES20.glDisable(GLES20.GL_DEPTH_TEST));

        UtilsGL.run(() -> GLES20.glDisableVertexAttribArray(positionAttrib));
        UtilsGL.run(() -> GLES20.glDisableVertexAttribArray(colorAttrib));
        UtilsGL.run(() -> GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0));

        final Bitmap newBitmapWithPreviewScene = copyFrameBuffer();
        LOGGER.info("copyFrame finished");
        return newBitmapWithPreviewScene;
    }

    /**
     * Sets {@link MainRenderer#vertexShaderCode}.
     *
     * @param vertexShaderCode The new {@link MainRenderer#vertexShaderCode}.
     */
    void setVertexShaderCode(final String vertexShaderCode) {
        this.vertexShaderCode = vertexShaderCode;
    }

    /**
     * Sets {@link MainRenderer#fragmentShaderCode}.
     *
     * @param fragmentShaderCode The new {@link MainRenderer#fragmentShaderCode}.
     */
    void setFragmentShaderCode(final String fragmentShaderCode) {
        this.fragmentShaderCode = fragmentShaderCode;
    }

    /**
     * Sets {@link MainRenderer#vertexShaderCodeRaster}.
     *
     * @param vertexShaderCode The new {@link MainRenderer#vertexShaderCodeRaster}.
     */
    void setVertexShaderCodeRaster(final String vertexShaderCode) {
        this.vertexShaderCodeRaster = vertexShaderCode;
    }

    /**
     * Sets {@link MainRenderer#fragmentShaderCodeRaster}.
     *
     * @param fragmentShaderCode The new {@link MainRenderer#fragmentShaderCodeRaster}.
     */
    void setFragmentShaderCodeRaster(final String fragmentShaderCode) {
        this.fragmentShaderCodeRaster = fragmentShaderCode;
    }

    /**
     * Sets {@link MainRenderer#activityManager}.
     *
     * @param activityManager The new {@link MainRenderer#activityManager}.
     */
    void setActivityManager(final ActivityManager activityManager) {
        this.activityManager = activityManager;
    }

    /**
     * Creates and launches the {@link RenderTask} field.
     */
    private void createAndLaunchRenderTask() {
        LOGGER.info("createAndLaunchRenderTask");
        final RenderTask.Builder renderTaskBuilder = new RenderTask.Builder(
            this.requestRender, this::rtFinishRender, this.textView, this.buttonRender
        );

        waitLastTask();

        this.renderTask = renderTaskBuilder
            .withUpdateInterval(DEFAULT_UPDATE_INTERVAL)
            .withWidth(this.width)
            .withHeight(this.height)
            .withNumThreads(this.numThreads)
            .withSamplesPixel(this.samplesPixel)
            .withSamplesLight(this.samplesLight)
            .withNumPrimitives(this.numPrimitives)
            .withNumLights(this.numLights)
            .build();

        this.lockExecutorService.lock();
        try {
            this.renderTask.executeOnExecutor(this.executorService);
        } finally {
            this.lockExecutorService.unlock();
        }
        LOGGER.info("createAndLaunchRenderTask finished");
    }

    /**
     * Draws the {@link Bitmap} field.
     */
    private void drawBitmap() {
        LOGGER.info("drawBitmap");
        UtilsGL.run(() -> GLES20.glUseProgram(this.shaderProgram));

        final int positionAttrib = UtilsGL.<Integer, Integer, String>run(this.shaderProgram, ConstantsRenderer.VERTEX_POSITION, GLES20::glGetAttribLocation);
        UtilsGL.run(() -> GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, positionAttrib));
        UtilsGL.run(() -> GLES20.glEnableVertexAttribArray(positionAttrib));
        UtilsGL.run(() -> GLES20.glVertexAttribPointer(
            positionAttrib, 4, GLES20.GL_FLOAT, false, 0, this.floatBufferVertices
        ));

        final int texCoordAttrib = UtilsGL.<Integer, Integer, String>run(this.shaderProgram, ConstantsRenderer.VERTEX_TEX_COORD, GLES20::glGetAttribLocation);
        UtilsGL.run(() -> GLES20.glEnableVertexAttribArray(texCoordAttrib));
        UtilsGL.run(() -> GLES20.glVertexAttribPointer(
            texCoordAttrib, 2, GLES20.GL_FLOAT, false, 0, this.floatBufferTexture
        ));


        final int vertexCount = this.verticesTexture.length / (Float.SIZE / Byte.SIZE);
        UtilsGL.run(() -> GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, positionAttrib, vertexCount));

        validateBitmap();
        UtilsGL.run(() -> GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, this.bitmap, GLES20.GL_UNSIGNED_BYTE, 0));
        validateBitmap();

        UtilsGL.run(() -> GLES20.glDisableVertexAttribArray(positionAttrib));
        UtilsGL.run(() -> GLES20.glDisableVertexAttribArray(texCoordAttrib));
        UtilsGL.run(() -> GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0));
        LOGGER.info("drawBitmap finished");
    }

    @Override
    public void onDrawFrame(@Nonnull final GL10 gl) {
        try {
            checksFreeMemory(2, () -> { });
        } catch (final LowMemoryException ex) {
            LOGGER.severe("SYSTEM WITH LOW MEMORY!!!");
            throw new FailureException(ex);
        }

        validateBitmap();
        if (this.firstFrame) {
            LOGGER.info("onDrawFirstFrame");

            if (this.rasterize) {
                this.rasterize = false;
                try {
                    initArrays();

                    validateArrays();
                    Preconditions.checkArgument(this.numPrimitives > 0);
                    validateBitmap();

                    this.bitmap = copyFrame(this.arrayVertices, this.arrayColors, this.arrayCamera, this.numPrimitives);

                    validateArrays();
                    Preconditions.checkArgument(this.numPrimitives > 0);
                    validateBitmap();
                } catch (final LowMemoryException ex) {
                    LOGGER.severe("onDrawFrame exception 1: " + ex.getClass().getName());
                    LOGGER.severe("onDrawFrame exception 2: " + Strings.nullToEmpty(ex.getMessage()));
                    LOGGER.severe("Low memory to rasterize a frame!!!");
                }
                validateArrays();
                Preconditions.checkArgument(this.numPrimitives > 0);
                validateBitmap();
            }

            try {
                LOGGER.info("rtRenderIntoBitmap started");
                validateBitmap();
                if (this.numThreads > 0) {
                    rtRenderIntoBitmap(this.bitmap, this.numThreads, true);
                }
                validateBitmap();
                LOGGER.info("rtRenderIntoBitmap finished");
            } catch (final LowMemoryException ex) {
                LOGGER.severe("onDrawFrame exception: " + ex.getClass().getName());
                LOGGER.severe("onDrawFrame exception: " + Strings.nullToEmpty(ex.getMessage()));
                LOGGER.severe("rtRenderIntoBitmap finished with error");
            }
            validateBitmap();
            createAndLaunchRenderTask();
            validateBitmap();
            LOGGER.info("onDrawFirstFrame finished");
        }
        validateBitmap();
        drawBitmap();
        validateBitmap();
        this.firstFrame = false;
    }

    /**
     * Helper method that validates the native arrays.
     */
    private void validateArrays() {
        Preconditions.checkArgument(this.arrayVertices != null);
        Preconditions.checkArgument(this.arrayColors != null);
        Preconditions.checkArgument(this.arrayCamera != null);
    }

    /**
     * Helper method that validates the {@link Bitmap}.
     */
    private void validateBitmap() {
        Preconditions.checkArgument(this.bitmap != null);
        Preconditions.checkArgument(!this.bitmap.isRecycled());
        Preconditions.checkArgument(this.bitmap.getWidth() == this.width);
        Preconditions.checkArgument(this.bitmap.getHeight() == this.height);
    }

    @Override
    public void onSurfaceChanged(@Nonnull final GL10 gl, final int width, final int height) {
        LOGGER.info("onSurfaceChanged");

        UtilsGL.run(() -> GLES20.glViewport(0, 0, width, height));

        LOGGER.info("onSurfaceChanged finished");
    }

    @Override
    public void onSurfaceCreated(@Nonnull final GL10 gl, @Nonnull final EGLConfig config) {
        LOGGER.info("onSurfaceCreated");

        UtilsGL.run(() -> GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT));

        // Enable culling
        UtilsGL.run(() -> GLES20.glEnable(GLES20.GL_CULL_FACE));
        UtilsGL.run(() -> GLES20.glEnable(GLES20.GL_BLEND));
        UtilsGL.run(() -> GLES20.glEnable(GLES20.GL_DEPTH_TEST));

        UtilsGL.run(() -> GLES20.glCullFace(GLES20.GL_BACK));
        UtilsGL.run(() -> GLES20.glFrontFace(GLES20.GL_CCW));
        UtilsGL.run(() -> GLES20.glClearDepthf(1.0F));

        UtilsGL.run(() -> GLES20.glDepthMask(true));

        UtilsGL.run(() -> GLES20.glDepthFunc(GLES20.GL_LEQUAL));

        UtilsGL.run(() -> GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F));

        // Create geometry and texture coordinates buffers
        final int byteBufferVerticesSize = this.verticesTexture.length * (Float.SIZE / Byte.SIZE);
        final ByteBuffer bbVertices = ByteBuffer.allocateDirect(byteBufferVerticesSize);
        bbVertices.order(ByteOrder.nativeOrder());
        this.floatBufferVertices = bbVertices.asFloatBuffer();
        this.floatBufferVertices.put(this.verticesTexture);
        this.floatBufferVertices.position(0);

        final int byteBufferTexCoordsSize = this.texCoords.length * (Float.SIZE / Byte.SIZE);
        final ByteBuffer byteBufferTexCoords = ByteBuffer.allocateDirect(byteBufferTexCoordsSize);
        byteBufferTexCoords.order(ByteOrder.nativeOrder());
        this.floatBufferTexture = byteBufferTexCoords.asFloatBuffer();
        this.floatBufferTexture.put(this.texCoords);
        this.floatBufferTexture.position(0);


        // Load shaders
        final int vertexShader = UtilsGL.loadShader(GLES20.GL_VERTEX_SHADER, this.vertexShaderCode);
        final int fragmentShader = UtilsGL.loadShader(GLES20.GL_FRAGMENT_SHADER, this.fragmentShaderCode);

        // Create Program
        this.shaderProgram = UtilsGL.<Integer>run(GLES20::glCreateProgram);

        if (this.shaderProgram == 0) {
            LOGGER.severe("Could not create program: ");
            final String programInfo = GLES20.glGetProgramInfoLog(0);
            LOGGER.severe(programInfo);
            throw new FailureException(GLES20.glGetProgramInfoLog(0));
        }

        // Attach and link shaders to program
        UtilsGL.run(() -> GLES20.glAttachShader(this.shaderProgram, vertexShader));
        UtilsGL.run(() -> GLES20.glAttachShader(this.shaderProgram, fragmentShader));

        final int numTextures = 1;
        final int[] textureHandle = new int[numTextures];
        UtilsGL.run(() -> GLES20.glGenTextures(numTextures, textureHandle, 0));
        if (textureHandle[0] == 0) {
            final String msg = "Error loading texture.";
            LOGGER.severe(msg);
            throw new FailureException(msg);
        }


        // Bind Attributes
        final int positionAttrib = 0;
        UtilsGL.run(() -> GLES20.glBindAttribLocation(this.shaderProgram, positionAttrib, ConstantsRenderer.VERTEX_POSITION));
        UtilsGL.run(() -> GLES20.glVertexAttribPointer(
            positionAttrib, 4, GLES20.GL_FLOAT, false, 0, this.floatBufferVertices
        ));
        UtilsGL.run(() -> GLES20.glEnableVertexAttribArray(positionAttrib));

        final int texCoordAttrib = 1;
        UtilsGL.run(() -> GLES20.glBindAttribLocation(this.shaderProgram, texCoordAttrib, ConstantsRenderer.VERTEX_TEX_COORD));
        UtilsGL.run(() -> GLES20.glVertexAttribPointer(
            texCoordAttrib, 2, GLES20.GL_FLOAT, false, 0, this.floatBufferTexture
        ));
        UtilsGL.run(() -> GLES20.glEnableVertexAttribArray(texCoordAttrib));


        UtilsGL.run(() -> GLES20.glLinkProgram(this.shaderProgram));

        final int[] linkStatus = new int[1];
        UtilsGL.run(() -> GLES20.glGetProgramiv(this.shaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0));

        if (linkStatus[0] != GLES20.GL_TRUE) {
            final String programLog = GLES20.glGetProgramInfoLog(this.shaderProgram);
            LOGGER.severe(programLog);
            GLES20.glDeleteProgram(this.shaderProgram);
            throw new FailureException(GLES20.glGetProgramInfoLog(this.shaderProgram));
        }

        // Shader program 1
        UtilsGL.run(() -> GLES20.glUseProgram(this.shaderProgram));

        // Bind to the texture in OpenGL
        UtilsGL.run(() -> GLES20.glActiveTexture(GLES20.GL_TEXTURE0));

        UtilsGL.run(() -> GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]));

        UtilsGL.run(() -> GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR));

        UtilsGL.run(() -> GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR));

        LOGGER.info("onSurfaceCreated finished");
    }
}
