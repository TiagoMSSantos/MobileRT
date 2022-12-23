package puscas.mobilertapp;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;

import org.jetbrains.annotations.Contract;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java8.util.Optional;
import kotlinx.coroutines.DelicateCoroutinesApi;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.java.Log;
import puscas.mobilertapp.configs.Config;
import puscas.mobilertapp.configs.ConfigGlAttribute;
import puscas.mobilertapp.configs.ConfigRenderTask;
import puscas.mobilertapp.configs.ConfigResolution;
import puscas.mobilertapp.configs.ConfigSamples;
import puscas.mobilertapp.constants.Constants;
import puscas.mobilertapp.constants.ConstantsMethods;
import puscas.mobilertapp.constants.ConstantsRenderer;
import puscas.mobilertapp.constants.ConstantsToast;
import puscas.mobilertapp.constants.State;
import puscas.mobilertapp.exceptions.LowMemoryException;
import puscas.mobilertapp.utils.AsyncTaskCoroutine;
import puscas.mobilertapp.utils.Utils;
import puscas.mobilertapp.utils.UtilsBuffer;
import puscas.mobilertapp.utils.UtilsGL;
import puscas.mobilertapp.utils.UtilsGlMatrices;
import puscas.mobilertapp.utils.UtilsShader;

/**
 * The OpenGL renderer that shows the Ray Tracer engine rendered image.
 */
@Log
public final class MainRenderer implements GLSurfaceView.Renderer {

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
     * The number of components in each vertex (X, Y, Z, W).
     */
    public static final int VERTEX_COMPONENTS = 4;

    /**
     * The number of components in each texture coordinate (X, Y).
     */
    public static final int TEXTURE_COMPONENTS = 2;

    /**
     * The number of color components in each pixel (RGBA).
     */
    public static final int PIXEL_COLORS = 4;

    /**
     * The default update interval in milliseconds of {@link RenderTask}.
     */
    private static final long DEFAULT_UPDATE_INTERVAL = 250L;

    /**
     * The vertices coordinates for the texture where the Ray Tracer
     * {@link Bitmap} will be applied.
     */
    private final float[] verticesTexture = {
        -1.0F, 1.0F, 0.0F, 1.0F,
        -1.0F, -1.0F, 0.0F, 1.0F,
        1.0F, -1.0F, 0.0F, 1.0F,
        1.0F, 1.0F, 0.0F, 1.0F,
    };

    /**
     * The texture coordinates of the texture containing the Ray Tracer
     * {@link Bitmap}.
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
     * The shaders' code for the Ray Tracing engine.
     */
    private Map<Integer, String> shadersCode;

    /**
     * The shaders' code for the OpenGL preview feature.
     */
    private Map<Integer, String> shadersPreviewCode;

    /**
     * The {@link ActivityManager} used to get information about the memory
     * state of the system.
     *
     * @see ActivityManager#getMemoryInfo(ActivityManager.MemoryInfo)
     */
    @Setter(AccessLevel.PACKAGE)
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
     * Whether should rasterize (render preview) or not.
     */
    private boolean rasterize = false;

    /**
     * The vertices positions of the {@link #verticesTexture}.
     */
    private FloatBuffer floatBufferVertices = null;

    /**
     * The texture coordinates of the {@link #texCoords}.
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
     * Configurator for the {@link RenderTask}.
     */
    private final ConfigRenderTask.ConfigRenderTaskBuilder configRenderTask = ConfigRenderTask.builder()
        .updateInterval(DEFAULT_UPDATE_INTERVAL)
        .finishRender(this::rtFinishRender);

    /**
     * The {@link ConfigResolution} of the {@link Bitmap} where the Ray Tracer engine will render
     * the scene. This represents the resolution of the desired {@link Bitmap}.
     */
    private ConfigResolution configResolution = ConfigResolution.builder().build();

    /**
     * The {@link ConfigResolution} of the {@link DrawView} where the Ray Tracer engine will render
     * the scene. This represents the resolution the {@link View} in the OpenGL context.
     */
    private ConfigResolution configResolutionView = ConfigResolution.builder().build();

    /**
     * The OpenGL program shader for the 2 triangles containing a texture with
     * {@link Bitmap} for the rendered image.
     */
    private int shaderProgram = 0;

    /**
     * The OpenGL program shader for the rasterization of the scene (preview).
     */
    private int shaderProgramRaster = 0;

    /**
     * Determine if it is the first frame to render.
     * It is important because it should only call the Ray Tracer engine at the
     * first frame and the others just
     * update the texture with the {@link Bitmap}.
     */
    private boolean firstFrame = false;

    /**
     * The {@link TextView} which will output the debug information about the
     * Ray Tracer engine.
     */
    @Setter(AccessLevel.PACKAGE)
    private TextView textView = null;

    /**
     * The {@link Button} which can start and stop the Ray Tracer engine.
     * It is important to let the {@link RenderTask} update its state after the
     * rendering process.
     */
    @Setter(AccessLevel.PACKAGE)
    private Button buttonRender = null;

    /**
     * A custom {@link AsyncTaskCoroutine} which will update the {@link View} with the
     * updated {@link Bitmap} and debug information.
     */
    private RenderTask renderTask = null;

    /**
     * The OpenGL texture handle.
     * Useful in order to delete the allocated texture when the ray tracing engine is closed.
     */
    private int[] textureHandle = null;

    /**
     * The constructor for this class.
     */
    MainRenderer() {
        setBitmap();
    }

    /**
     * Converts a pixel from OpenGL format (ABGR) to a pixel of Android format
     * (ARGB).
     *
     * @param pixel A pixel from OpenGL format.
     * @return A pixel from Android format.
     * @implNote Converts OpenGL pixel, which is ABGR to Android pixel which is
     *     ARGB.
     */
    @Contract(pure = true)
    private static int convertPixelOpenGlToAndroid(final int pixel) {
        final int red = pixel & 0xFF;
        final int green = (pixel >> 8) & 0xFF;
        final int blue = (pixel >> (2 * 8)) & 0xFF;
        final int alpha = (pixel >> (3 * 8)) & 0xFF;
        final int newPixel = (red << (2 * 8)) | (green << 8) | blue;
        return alpha << (3 * 8) | newPixel;
    }

    /**
     * Helper method that defines an array of some vertex attribute data.
     *
     * @param byteBuffer The data to pass to OpenGL context.
     * @param attribute  The index of the generic vertex attribute to be modified.
     */
    private static void defineAttributeData(@NonNull final ByteBuffer byteBuffer,
                                            final int attribute) {
        UtilsGL.run(() -> GLES20.glVertexAttribPointer(attribute,
            VERTEX_COMPONENTS, GLES20.GL_FLOAT, false, 0, byteBuffer));
        UtilsGL.run(() -> GLES20.glEnableVertexAttribArray(attribute));
    }

    /**
     * Create the Model View Projection matrices and specify them as values for the uniform
     * variables in the shader program.
     *
     * @param bbCamera         The camera's position and vectors in the scene.
     * @param shaderProgram    The OpenGL shader program index to specify the
     *                         matrices.
     * @param configResolution The resolution of the {@link Bitmap} to render.
     */
    private static void createMatricesAsUniformVariables(@NonNull final ByteBuffer bbCamera,
                                                         final int shaderProgram,
                                                         final ConfigResolution configResolution) {
        final float[] projectionMatrix = UtilsGlMatrices.createProjectionMatrix(
            bbCamera, configResolution.getWidth(), configResolution.getHeight());
        final float[] viewMatrix = UtilsGlMatrices.createViewMatrix(bbCamera);
        final float[] modelMatrix = UtilsGlMatrices.createModelMatrix();

        final int handleModel = UtilsGL.<Integer, Integer, String>run(
            shaderProgram, "uniformModelMatrix", GLES20::glGetUniformLocation);
        final int handleView = UtilsGL.<Integer, Integer, String>run(
            shaderProgram, "uniformViewMatrix", GLES20::glGetUniformLocation);
        final int handleProjection = UtilsGL.<Integer, Integer, String>run(
            shaderProgram, "uniformProjectionMatrix", GLES20::glGetUniformLocation);

        UtilsGL.run(() -> GLES20.glUniformMatrix4fv(handleModel, 1, false, modelMatrix, 0));
        UtilsGL.run(() -> GLES20.glUniformMatrix4fv(handleView, 1, false, viewMatrix, 0));
        UtilsGL.run(() -> GLES20.glUniformMatrix4fv(handleProjection,
            1, false, projectionMatrix, 0));
    }

    /**
     * Helper method that connects the vertices and colors in the GLSL attributes.
     *
     * @param shaderProgram The index of the shader program to connect the attributes.
     * @param bbVertices    The vertices' buffer.
     * @param bbColors      The colors' buffer.
     */
    private static void connectAttributes(final int shaderProgram,
                                          @NonNull final ByteBuffer bbVertices,
                                          @NonNull final ByteBuffer bbColors) {
        final ConfigGlAttribute verticesAttribute = ConfigGlAttribute.builder()
            .attributeName(VERTEX_POSITION)
            .buffer(bbVertices)
            .attributeLocation(0)
            .componentsInBuffer(VERTEX_COMPONENTS)
            .build();
        UtilsShader.connectOpenGlAttribute(shaderProgram, verticesAttribute);

        final ConfigGlAttribute colorsAttribute = ConfigGlAttribute.builder()
            .attributeName(VERTEX_COLOR)
            .buffer(bbColors)
            .attributeLocation(1)
            .componentsInBuffer(PIXEL_COLORS)
            .build();
        UtilsShader.connectOpenGlAttribute(shaderProgram, colorsAttribute);
    }

    /**
     * Helper method that calculates the number of vertices in a {@link ByteBuffer}.
     *
     * @param bbVertices The vertices' buffer.
     * @return The number of vertices in the {@link ByteBuffer}.
     */
    private static int getVertexCount(@NonNull final ByteBuffer bbVertices) {
        log.info("getVertexCount");

        return bbVertices.capacity() / (Constants.BYTES_IN_FLOAT * VERTEX_COMPONENTS);
    }

    /**
     * Updates the text in the render {@link Button}.
     * Note that only the UI thread can change the {@link #buttonRender} value.
     *
     * @param state The resource identifier of the string resource to be displayed.
     */
    void updateButton(final int state) {
        this.buttonRender.setText(state);
    }

    /**
     * Gets an {@code int} which represents the current Ray Tracer engine
     * {@link State}.
     *
     * @return The current Ray Tracer engine {@link State}.
     */
    @NonNull
    public State getState() {
        log.info("getState");

        while (this.firstFrame) {
            log.info("Waiting for the onDraw to start the RT engine!!!");
            Uninterruptibles.sleepUninterruptibly(500L, TimeUnit.MILLISECONDS);
            log.info("Waited for the onDraw to start the RT engine!!!");
        }

        MainActivity.resetErrno();
        return Optional.ofNullable(this.renderTask)
            .map(task -> State.values()[task.rtGetState()])
            .orElse(State.IDLE);
    }

    /**
     * Resets some stats about the Ray Tracer engine.
     */
    void resetStats() {
        resetStats(-1, ConfigSamples.builder().build(), -1, -1);
    }

    /**
     * Resets some stats about the Ray Tracer engine.
     *
     * @param numThreads    The number of threads.
     * @param configSamples The number of samples per pixel and light.
     * @param numPrimitives The number of primitives in the scene.
     * @param numLights     The number of lights in the scene.
     */
    void resetStats(final int numThreads,
                    final ConfigSamples configSamples,
                    final int numPrimitives,
                    final int numLights) {
        log.info("resetStats");

        Preconditions.checkArgument(numPrimitives >= -1, "numPrimitives shouldn't be negative");

        final String numThreadsStr = "numThreads: " + numThreads;
        final String numPrimitivesStr = "numPrimitives: " + numPrimitives;
        final String numLightsStr = "numLights: " + numLights;
        final String samplesPixelStr = "samplesPixel: " + configSamples.getSamplesPixel();
        final String samplesLightStr = "samplesLight: " + configSamples.getSamplesLight();
        log.info(numThreadsStr);
        log.info(numPrimitivesStr);
        log.info(numLightsStr);
        log.info(samplesPixelStr);
        log.info(samplesLightStr);

        this.numThreads = numThreads;
        this.numPrimitives = numPrimitives;

        this.configRenderTask
            .samples(configSamples)
            .numLights(numLights);
    }

    /**
     * Stops the Ray Tracer engine and updates its {@link State} to
     * {@link State#IDLE}.
     */
    native void rtFinishRender();

    /**
     * Loads the scene and constructs the Ray Tracer renderer.
     *
     * @param config The ray tracer configuration.
     * @return The number of primitives or a negative value if an error occurs.
     * @throws LowMemoryException If the device has low free memory.
     */
    native int rtInitialize(Config config) throws LowMemoryException;

    /**
     * Let Ray Tracer engine start to render the scene.
     * It will render the scene asynchronously.
     *
     * @param image      The {@link Bitmap} where the Ray Tracer will render
     *                   the scene into.
     * @param numThreads The number of threads to be used by the Ray Tracer
     *                   engine.
     * @throws LowMemoryException If the device has low free memory.
     */
    private native void rtRenderIntoBitmap(Bitmap image,
                                           int numThreads) throws LowMemoryException;

    /**
     * Creates a native array with all the positions of triangles in the scene.
     *
     * @return A new array with all the primitives' vertices.
     * @throws LowMemoryException If the device has low free memory.
     */
    private native ByteBuffer rtInitVerticesArray() throws LowMemoryException;

    /**
     * Creates a native array with all the colors of triangles in the scene.
     *
     * @return A new array with all the primitives' colors.
     * @throws LowMemoryException If the device has low free memory.
     */
    private native ByteBuffer rtInitColorsArray() throws LowMemoryException;

    /**
     * Creates a native array with the camera's position, direction, up and
     * right vectors in the scene.
     *
     * @return A new array with the camera's position and vectors.
     * @throws LowMemoryException If the device has low free memory.
     */
    private native ByteBuffer rtInitCameraArray() throws LowMemoryException;

    /**
     * Free the memory of a native array.
     * The memory allocated with {@link #rtInitVerticesArray()},
     * {@link #rtInitColorsArray()} and
     * {@link #rtInitCameraArray()} methods should be free using
     * this method.
     *
     * @param byteBuffer A reference to {@link ByteBuffer} to free its memory.
     * @return A {@code null} reference.
     */
    private native ByteBuffer rtFreeNativeBuffer(final ByteBuffer byteBuffer);

    /**
     * Free the memory of {@link #arrayVertices},
     * {@link #arrayColors} and {@link #arrayCamera}
     * native arrays.
     */
    void freeArrays() {
        log.info("freeArrays");
        this.arrayVertices = rtFreeNativeBuffer(this.arrayVertices);
        this.arrayColors = rtFreeNativeBuffer(this.arrayColors);
        this.arrayCamera = rtFreeNativeBuffer(this.arrayCamera);
    }

    /**
     * Helper method which initializes the {@link #arrayVertices},
     * {@link #arrayColors} and {@link #arrayCamera}
     * native arrays.
     * @throws LowMemoryException If the device has low free memory.
     */
    @VisibleForTesting
    void initPreviewArrays() throws LowMemoryException {
        log.info("initArrays");
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
     * @return {@code True} if the device doesn't have enough memory to be
     *     allocated, otherwise {@code false}.
     */
    private boolean isLowMemory(final int memoryNeeded) {
        Preconditions.checkArgument(memoryNeeded > 0,
            "The requested memory must be a positive value");

        this.activityManager.getMemoryInfo(this.memoryInfo);
        final long availMem = this.memoryInfo.availMem / Constants.BYTES_IN_MEGABYTE;
        final long totalMem = this.memoryInfo.totalMem / Constants.BYTES_IN_MEGABYTE;
        final boolean insufficientMem = availMem <= (1 + memoryNeeded);
        final String message = "MEMORY AVAILABLE: " + availMem + "MB (" + totalMem + "MB) [Needed " + memoryNeeded + "MB]";
        log.info(message);
        return insufficientMem || this.memoryInfo.lowMemory;
    }

    /**
     * Helper method which checks if the Android device has low free memory.
     *
     * @param memoryNeeded Number of MegaBytes needed to be allocated.
     * @param function     The function to execute if the device has low free
     *                     memory.
     * @throws LowMemoryException If the device has low free memory.
     */
    void checksFreeMemory(final int memoryNeeded,
                          final Runnable function) throws LowMemoryException {
        log.info("checksFreeMemory");
        if (isLowMemory(memoryNeeded)) {
            function.run();
            throw new LowMemoryException();
        }
    }

    /**
     * Prepares this object by setting up the {@link ConfigRenderTask#getRequestRender()}
     * and {@link #renderTask} fields.
     *
     * @param requestRender A {@link Runnable} of
     *                      {@link GLSurfaceView#requestRender()} method.
     */
    void prepareRenderer(final Runnable requestRender) {
        log.info("prepareRenderer");

        this.configRenderTask.requestRender(requestRender);
    }

    /**
     * Prepares this class with the OpenGL shaders' code.
     *
     * @param shadersCode        The shaders' code for the Ray Tracing engine.
     * @param shadersPreviewCode The shaders' code for the OpenGL preview feature.
     */
    void setUpShadersCode(final Map<Integer, String> shadersCode,
                          final Map<Integer, String> shadersPreviewCode) {
        this.shadersCode = shadersCode;
        this.shadersPreviewCode = shadersPreviewCode;
    }

    /**
     * Creates a new {@link Bitmap} with the size of {@code width} and
     * {@code height} and also sets the {@code #viewWidth} and
     * {@code #viewHeight} fields.
     */
    private void setBitmap() {
        log.info(ConstantsMethods.SET_BITMAP);

        setBitmap(ConfigResolution.builder().build(), ConfigResolution.builder().build(), false);

        final String message = ConstantsMethods.SET_BITMAP + ConstantsMethods.FINISHED;
        log.info(message);
    }

    /**
     * Creates a new {@link Bitmap} with the size of {@code width} and
     * {@code height} and also sets the {@code #viewWidth} and
     * {@code #viewHeight} fields.
     *
     * @param configResolution     The resolution of the new {@link Bitmap}.
     * @param configResolutionView The resolution of the {@link SurfaceView}.
     * @param rasterize            The new {@link #rasterize}.
     */
    void setBitmap(final ConfigResolution configResolution,
                   final ConfigResolution configResolutionView,
                   final boolean rasterize) {
        log.info(ConstantsMethods.SET_BITMAP);
        this.configResolution = configResolution;
        this.configResolutionView = configResolutionView;

        this.bitmap = Bitmap.createBitmap(configResolution.getWidth(), configResolution.getHeight(), Bitmap.Config.ARGB_8888);
        // For some reason, only from Android 4.2+, the method `Bitmap.createBitmap` sets the
        // `errno` to `ENOMEM` which means that the system didn't have enough memory to do some
        // operation, so we set the `errno` back to 0 here.
        MainActivity.resetErrno();
        this.bitmap.eraseColor(Color.BLACK);
        validateBitmap(this.bitmap);

        this.firstFrame = true;
        this.rasterize = rasterize;

        final String messageFinished = ConstantsMethods.SET_BITMAP + ConstantsMethods.FINISHED;
        log.info(messageFinished);
    }

    /**
     * Converts an index of a pixel from OpenGL format to an index of a pixel of
     * Android format.
     *
     * @param index An index of a pixel from OpenGL format.
     * @return An index of a pixel from Android format.
     */
    @Contract(pure = true)
    private int convertIndexOpenGlToAndroid(final int index) {
        final int column = index % this.configResolutionView.getWidth();
        final int line = index / this.configResolutionView.getWidth();
        return (this.configResolutionView.getHeight() - line - 1) * this.configResolutionView.getWidth() + column;
    }

    /**
     * Helper method that reads and copies the pixels in the OpenGL frame buffer
     * to a new {@link Bitmap}.
     *
     * @param configResolution     The resolution of the desired {@link Bitmap}.
     * @param configResolutionView The resolution the {@link View} in the OpenGL context.
     * @return A new {@link Bitmap} with the colors of the pixels in the OpenGL
     *     frame buffer.
     */
    private Bitmap copyGlFrameBufferToBitmap(final ConfigResolution configResolution,
                                             final ConfigResolution configResolutionView) {
        final int sizePixels = configResolutionView.getWidth() * configResolutionView.getHeight();
        final int[] arrayBytesPixels = new int[sizePixels];
        final int[] arrayBytesNewBitmap = new int[sizePixels];
        final IntBuffer intBuffer = IntBuffer.wrap(arrayBytesPixels);
        intBuffer.position(0);

        UtilsGL.run(() -> GLES20.glReadPixels(
            0, 0, configResolutionView.getWidth(), configResolutionView.getHeight(), GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, intBuffer
        ));

        int openGlIndex = 0;
        for (final int pixel : arrayBytesPixels) {
            final int androidIndex = convertIndexOpenGlToAndroid(openGlIndex);
            ++openGlIndex;
            arrayBytesNewBitmap[androidIndex] = convertPixelOpenGlToAndroid(pixel);
        }

        final Bitmap bitmapView = Bitmap.createBitmap(
            arrayBytesNewBitmap, configResolutionView.getWidth(), configResolutionView.getHeight(), Bitmap.Config.ARGB_8888
        );
        final Bitmap newBitmapWithPreviewScene = Bitmap.createScaledBitmap(
            bitmapView, configResolution.getWidth(), configResolution.getHeight(), true);
        Preconditions.checkArgument(bitmapView.getWidth() == configResolutionView.getWidth(),
                "viewWidth is not the expected one");
        Preconditions.checkArgument(bitmapView.getHeight() == configResolutionView.getHeight(),
                "viewHeight is not the expected one");
        return newBitmapWithPreviewScene;
    }

    /**
     * Waits for the last {@link RenderTask} to finish.
     */
    void waitLastTask() {
        log.info("waitLastTask");

        Optional.ofNullable(this.renderTask)
            .ifPresent(RenderTask::waitToFinish);

        final String messageFinished = "waitLastTask" + ConstantsMethods.FINISHED;
        log.info(messageFinished);
    }

    /**
     * Closes the Renderer.
     */
    void closeRenderer() {
        log.info("closeRenderer");

        if (this.textureHandle != null) {
            UtilsGL.run(() -> GLES20.glDeleteTextures(1, this.textureHandle, 0));
        }
        UtilsGL.run(() -> GLES20.glDeleteProgram(this.shaderProgram));
        UtilsGL.run(() -> GLES20.glDeleteProgram(this.shaderProgramRaster));

        final String messageFinished = "closeRenderer" + ConstantsMethods.FINISHED;
        log.info(messageFinished);
    }

    /**
     * Helper method which rasterizes the scene by using OpenGL rasterizer with
     * the camera and the primitives received by parameters.
     * After rendering the scene it reads the OpenGL frame buffer to copy the
     * rendered scene into an Android {@link Bitmap}.
     *
     * @param bbVertices    The primitives' vertices in the scene.
     * @param bbColors      The primitives' colors in the scene.
     * @param bbCamera      The camera's position and vectors in the scene.
     * @param numPrimitives The number of primitives in the scene.
     * @throws LowMemoryException If the device has low free memory.
     */
    @VisibleForTesting
    Bitmap renderSceneToBitmap(@NonNull final ByteBuffer bbVertices,
                               @NonNull final ByteBuffer bbColors,
                               @NonNull final ByteBuffer bbCamera,
                               final int numPrimitives) throws LowMemoryException {
        log.info("renderSceneToBitmap");

        if (UtilsBuffer.isAnyByteBufferEmpty(bbVertices, bbColors, bbCamera)
            || numPrimitives <= 0) {
            return this.bitmap;
        }
        UtilsGL.run(() -> GLES20.glClear(ConstantsRenderer.ALL_BUFFER_BIT));

        final int neededMemoryMb = Utils.calculateSceneSize(numPrimitives);
        checksFreeMemory(neededMemoryMb, () -> log.severe("SYSTEM WITH LOW MEMORY!!!"));

        UtilsBuffer.resetByteBuffers(bbVertices, bbColors, bbCamera);

        this.shaderProgramRaster = UtilsShader.reCreateProgram(this.shaderProgramRaster);

        connectAttributes(this.shaderProgramRaster, bbVertices, bbColors);

        UtilsShader.attachShaders(this.shaderProgramRaster, this.shadersPreviewCode);


        UtilsGL.run(() -> GLES20.glUseProgram(this.shaderProgramRaster));

        createMatricesAsUniformVariables(bbCamera, this.shaderProgramRaster, this.configResolution);

        defineAttributeData(bbVertices, 0);

        defineAttributeData(bbColors, 1);

        final int vertexCount = getVertexCount(bbVertices);
        UtilsGL.run(() -> GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount));

        UtilsGL.disableAttributeData(0, 1);

        return copyGlFrameBufferToBitmap(this.configResolution, this.configResolutionView);
    }

    /**
     * Creates and launches the {@link RenderTask} field.
     */
    @OptIn(markerClass = DelicateCoroutinesApi.class)
    private void createAndLaunchRenderTask() {
        log.info("createAndLaunchRenderTask");

        waitLastTask();

        final ConfigRenderTask config = this.configRenderTask
            .textView(this.textView)
            .buttonRender(this.buttonRender)
            .numPrimitives(this.numPrimitives)
            .numThreads(this.numThreads)
            .resolution(this.configResolution)
            .build();

        this.renderTask = RenderTask.builder()
            .config(config)
            .build();

        this.renderTask.executeAsync();
        final String message = "createAndLaunchRenderTask" + ConstantsMethods.FINISHED;
        log.info(message);
    }

    /**
     * Draws the {@link Bitmap} field.
     *
     * @param bitmap The {@link Bitmap} to draw.
     */
    private void drawBitmap(final Bitmap bitmap) {
        log.info("drawBitmap");

        UtilsGL.run(() -> GLES20.glUseProgram(this.shaderProgram));

        final ConfigGlAttribute verticesAttribute = ConfigGlAttribute.builder()
            .attributeName(VERTEX_POSITION)
            .buffer(this.floatBufferVertices)
            .attributeLocation(0)
            .componentsInBuffer(VERTEX_COMPONENTS)
            .build();
        UtilsShader.connectOpenGlAttribute(this.shaderProgram, verticesAttribute);

        final ConfigGlAttribute textureAttribute = ConfigGlAttribute.builder()
            .attributeName(VERTEX_TEX_COORD)
            .buffer(this.floatBufferTexture)
            .attributeLocation(1)
            .componentsInBuffer(TEXTURE_COMPONENTS)
            .build();
        UtilsShader.connectOpenGlAttribute(this.shaderProgram, textureAttribute);

        final int vertexCount = this.verticesTexture.length / Constants.BYTES_IN_FLOAT;
        UtilsGL.run(() -> GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount));

        UtilsGL.run(() -> GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0,
            GLES20.GL_RGBA, bitmap, GLES20.GL_UNSIGNED_BYTE, 0));

        final String message = "drawBitmap" + ConstantsMethods.FINISHED;
        log.info(message);
    }

    /**
     * Helper method that validates the native arrays.
     */
    private void validateArrays() {
        Preconditions.checkNotNull(this.arrayVertices, "arrayVertices shouldn't be null");
        Preconditions.checkNotNull(this.arrayColors, "arrayColors shouldn't be null");
        Preconditions.checkNotNull(this.arrayCamera, "arrayCamera shouldn't be null");
    }

    /**
     * Helper method that validates the {@link Bitmap}.
     *
     * @param bitmap The {@link Bitmap} to validate.
     */
    private void validateBitmap(final Bitmap bitmap) {
        Preconditions.checkNotNull(bitmap, "arrayVertices shouldn't be null");
        Preconditions.checkArgument(!bitmap.isRecycled(), "bitmap shouldn't been recycled");
        Preconditions.checkArgument(bitmap.getWidth() == this.configResolution.getWidth(),
                "bitmap width is not the expected");
        Preconditions.checkArgument(bitmap.getHeight() == this.configResolution.getHeight(),
                "bitmap height is not the expected");
    }

    @Override
    public void onSurfaceCreated(@NonNull final GL10 gl, @NonNull final EGLConfig config) {
        log.info("onSurfaceCreated");

        UtilsGL.resetOpenGlBuffers();

        this.shaderProgram = UtilsShader.reCreateProgram(this.shaderProgram);

        UtilsShader.loadAndAttachShaders(this.shaderProgram, this.shadersCode);

        // Create geometry and texture coordinates buffers
        this.floatBufferVertices = UtilsBuffer.allocateBuffer(this.verticesTexture);
        this.floatBufferTexture = UtilsBuffer.allocateBuffer(this.texCoords);

        // Bind Attributes
        final ConfigGlAttribute verticesAttribute = ConfigGlAttribute.builder()
            .attributeName(VERTEX_POSITION)
            .buffer(this.floatBufferVertices)
            .attributeLocation(0)
            .componentsInBuffer(VERTEX_COMPONENTS)
            .build();
        UtilsShader.connectOpenGlAttribute(this.shaderProgram, verticesAttribute);

        final ConfigGlAttribute textureAttribute = ConfigGlAttribute.builder()
            .attributeName(VERTEX_TEX_COORD)
            .buffer(this.floatBufferTexture)
            .attributeLocation(1)
            .componentsInBuffer(TEXTURE_COMPONENTS)
            .build();
        UtilsShader.connectOpenGlAttribute(this.shaderProgram, textureAttribute);

        UtilsGL.run(() -> GLES20.glLinkProgram(this.shaderProgram));
        UtilsShader.checksShaderLinkStatus(this.shaderProgram);

        // Shader program 1
        UtilsGL.run(() -> GLES20.glUseProgram(this.shaderProgram));

        this.textureHandle = UtilsGL.bindTexture();

        final String message = "onSurfaceCreated" + ConstantsMethods.FINISHED;
        log.info(message);
    }

    @Override
    public void onSurfaceChanged(@NonNull final GL10 gl, final int width, final int height) {
        log.info("onSurfaceChanged");

        UtilsGL.run(() -> GLES20.glViewport(0, 0, width, height));

        final String message = "onSurfaceChanged" + ConstantsMethods.FINISHED;
        log.info(message);
    }

    @Override
    public void onDrawFrame(@NonNull final GL10 gl) {
        if (this.firstFrame) {
            log.info("onDrawFirstFrame");
            UtilsGL.resetOpenGlBuffers();

            try {
                if (this.rasterize) {
                    this.rasterize = false;
                    this.bitmap = renderSceneIntoBitmap();
                }

                MainActivity.resetErrno();
                rtRenderIntoBitmap(this.bitmap, this.numThreads);
            } catch (final Throwable ex) {
                MainActivity.showUiMessage(ConstantsToast.COULD_NOT_RENDER_THE_SCENE + ex.getMessage());
                MainActivity.resetRenderButton();
                return;
            } finally {
                this.firstFrame = false;
            }

            createAndLaunchRenderTask();

            final String message = "onDrawFirstFrame" + ConstantsMethods.FINISHED;
            log.info(message);
        }

        drawBitmap(this.bitmap);
    }

    /**
     * Helper method that renders the scene with OpenGL to a {@link Bitmap}.
     *
     * @return A {@link Bitmap} with the scene rendered.
     * @throws LowMemoryException If the device has low free memory.
     */
    private Bitmap renderSceneIntoBitmap() throws LowMemoryException {
        initPreviewArrays();
        return renderSceneToBitmap(this.arrayVertices, this.arrayColors, this.arrayCamera, this.numPrimitives);
    }

}
