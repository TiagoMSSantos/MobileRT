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

import androidx.annotation.Keep;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static puscas.mobilertapp.ConstantsRenderer.NUMBER_THREADS;
import static puscas.mobilertapp.ConstantsRenderer.VERTEX_COLOR;
import static puscas.mobilertapp.ConstantsRenderer.VERTEX_POSITION;
import static puscas.mobilertapp.ConstantsRenderer.VERTEX_TEX_COORD;

/**
 * The OpenGL renderer that shows the Ray Tracer engine rendered image.
 */
final class MainRenderer implements GLSurfaceView.Renderer {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(MainRenderer.class.getName());

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
    private ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_THREADS);

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
            LOGGER.severe(stringError.get() + ": " + GLUtils.getEGLErrorString(glError));
            System.exit(1);
        }
    }

    /**
     * Helper method which loads an OpenGL shader.
     *
     * @param shaderType The type of the shader (vertex or fragment shader).
     * @param source     The code of the shader.
     * @return The OpenGL index of the shader.
     */
    private static int loadShader(final int shaderType, final String source) {
        final int shader = GLES20.glCreateShader(shaderType);
        checksGLError();
        if (shader == 0) {
            LOGGER.severe("GLES20.glCreateShader = 0");
            System.exit(1);
        } else {
            GLES20.glShaderSource(shader, source);
            checksGLError();
            GLES20.glCompileShader(shader);
            checksGLError();
            final int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            checksGLError();
            if (compiled[0] == 0) {
                LOGGER.severe("Could not compile shader " + shaderType + ':');
                LOGGER.severe(GLES20.glGetShaderInfoLog(shader));
                checksGLError();
                LOGGER.severe(source);
                GLES20.glDeleteShader(shader);
                checksGLError();
                System.exit(1);
            }
        }
        return shader;
    }

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
        return State.values()[this.renderTask.RTGetState()];
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
    void resetStats(
            final int numThreads,
            final int samplesPixel,
            final int samplesLight,
            final int numPrimitives,
            final int numLights) {
        this.numThreads = numThreads;
        this.samplesPixel = samplesPixel;
        this.samplesLight = samplesLight;
        this.numPrimitives = numPrimitives;
        this.numLights = numLights;
    }

    /**
     * Stops the Ray Tracer engine and updates its {@link State} to {@link State#IDLE}.
     */
    native void RTFinishRender();

    /**
     * Loads the scene and constructs the Ray Tracer renderer.
     *
     * @param scene        The scene to load.
     * @param shader       The shader to use.
     * @param width        The width of the image plane.
     * @param height       The height of the image plane.
     * @param accelerator  The accelerator to use.
     * @param samplesPixel The number of samples per pixel.
     * @param samplesLight The number of samples per light.
     * @param objFilePath  The path to the OBJ file containing the scene.
     * @param matFilePath  The path to the MTL file containing the materials of the scene.
     * @param camFilePath  The path to the CAM file containing the camera in the scene.
     * @return The number of primitives or a negative value if an error occurs.
     */
    native int RTInitialize(
            final int scene,
            final int shader,
            final int width,
            final int height,
            final int accelerator,
            final int samplesPixel,
            final int samplesLight,
            final String objFilePath,
            final String matFilePath,
            final String camFilePath
    ) throws Exception;

    /**
     * Let Ray Tracer engine start to render the scene.
     * It can render synchronously or asynchronously controlled by the {@code async} argument.
     *
     * @param image      The {@link Bitmap} where the Ray Tracer will render the scene into.
     * @param numThreads The number of threads to be used by the Ray Tracer engine.
     * @param async      If {@code true} let the Ray Tracer engine render the scene asynchronously or otherwise
     *                   synchronously.
     */
    private native void RTRenderIntoBitmap(final Bitmap image, final int numThreads, final boolean async);

    /**
     * Creates a native array with all the positions of triangles in the scene.
     *
     * @return A new array with all the primitives' vertices.
     */
    private native ByteBuffer RTInitVerticesArray();

    /**
     * Creates a native array with all the colors of triangles in the scene.
     *
     * @return A new array with all the primitives' colors.
     */
    private native ByteBuffer RTInitColorsArray();

    /**
     * Creates a native array with the camera's position, direction, up and right vectors in the scene.
     *
     * @return A new array with the camera's position and vectors.
     */
    private native ByteBuffer RTInitCameraArray();

    /**
     * Free the memory of a native array.
     * The memory allocated with {@link MainRenderer#RTInitVerticesArray()},
     * {@link MainRenderer#RTInitColorsArray()} and {@link MainRenderer#RTInitCameraArray()} methods should be
     * free using this method.
     *
     * @param byteBuffer A reference to {@link ByteBuffer} to free its memory.
     * @return A {@code null} reference.
     */
    private native ByteBuffer RTFreeNativeBuffer(final ByteBuffer byteBuffer);

    /**
     * Free the memory of {@link MainRenderer#arrayVertices}, {@link MainRenderer#arrayColors} and
     * {@link MainRenderer#arrayCamera} native arrays.
     */
    void freeArrays() {
        this.arrayVertices = RTFreeNativeBuffer(this.arrayVertices);
        this.arrayColors = RTFreeNativeBuffer(this.arrayColors);
        this.arrayCamera = RTFreeNativeBuffer(this.arrayCamera);
    }

    /**
     * Helper method which initializes the {@link MainRenderer#arrayVertices}, {@link MainRenderer#arrayColors} and
     * {@link MainRenderer#arrayCamera} native arrays.
     */
    private void initArrays() {
        this.arrayVertices = RTInitVerticesArray();

        if (isLowMemory(1)) {
            freeArrays();
        }

        this.arrayColors = RTInitColorsArray();

        if (isLowMemory(1)) {
            freeArrays();
        }

        this.arrayCamera = RTInitCameraArray();

        if (isLowMemory(1)) {
            freeArrays();
        }
    }

    /**
     * Helper method which verifies if the Android device has low free memory.
     *
     * @param memoryNeeded Number of MegaBytes needed to be allocated.
     * @return {@code True} if the device doesn't have enough memory to be allocated, otherwise {@code false}.
     */
    @Keep
    private boolean isLowMemory(final int memoryNeeded) {
        Preconditions.checkArgument(memoryNeeded > 0, "The requested memory must be a positive value");

        this.activityManager.getMemoryInfo(this.memoryInfo);
        final long availMem = this.memoryInfo.availMem / 1048576L;
        final boolean insufficientMem = availMem <= (long) (1 + memoryNeeded);
        return insufficientMem || this.memoryInfo.lowMemory;
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
        this.renderTask = new RenderTask.Builder(() -> { }, () -> { }, this.textView, this.buttonRender).build();
    }

    /**
     * Creates a new {@link Bitmap} with the size of {@code width} and {@code height} and also sets the
     * {@link MainRenderer#viewWidth} and {@link MainRenderer#viewHeight} fields.
     *
     * @param width      The width of the new {@link Bitmap}.
     * @param height     The height of the new {@link Bitmap}.
     * @param widthView  The width of the {@link GLSurfaceView}.
     * @param heightView The height of the {@link GLSurfaceView}.
     */
    void setBitmap(final int width, final int height, final int widthView, final int heightView) {
        this.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        this.bitmap.eraseColor(Color.BLACK);
        this.width = width;
        this.height = height;
        this.viewWidth = widthView;
        this.viewHeight = heightView;
        this.firstFrame = true;
    }

    /**
     * Helper method that reads and copies the pixels in the frame buffer to a new {@link Bitmap}.
     *
     * @return A new {@link Bitmap} with the colors of the pixels in the OpenGL frame buffer.
     */
    private Bitmap copyFrameBuffer() {
        final int sizePixels = this.viewWidth * this.viewHeight;
        final int[] arrayBytesPixels = new int[sizePixels];
        final int[] arrayBytesNewBitmap = new int[sizePixels];
        final IntBuffer intBuffer = IntBuffer.wrap(arrayBytesPixels);
        intBuffer.position(0);

        GLES20.glReadPixels(0, 0, this.viewWidth, this.viewHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                intBuffer);
        checksGLError();

        // Remember, that OpenGL bitmap is incompatible with Android bitmap and so, some correction need.
        int id = 0;
        for (final int pixel : arrayBytesPixels) {
            final int column = id % this.viewWidth;
            final int line = id / this.viewWidth;
            final int newPixelId = (this.viewHeight - line - 1) * this.viewWidth + column;
            id++;

            final int red = pixel & 0xff;
            final int green = (pixel >> 8) & 0xff;
            final int blue = (pixel >> 16) & 0xff;
            final int alpha = (pixel >> 24) & 0xff;
            final int newPixel = (red << 16) | (green << 8) | blue;
            arrayBytesNewBitmap[newPixelId] = alpha << 24 | newPixel;
        }

        final Bitmap bitmapAux = Bitmap.createBitmap(arrayBytesNewBitmap, this.viewWidth, this.viewHeight,
                Bitmap.Config.ARGB_8888);
        return Bitmap.createScaledBitmap(bitmapAux, this.width, this.height, true);
    }

    /**
     * Shuts down and waits for the {@link MainRenderer#executorService} to terminate.
     * In the END, resets {@link MainRenderer#executorService} to a new thread pool with
     * {@link ConstantsRenderer#NUMBER_THREADS} threads.
     */
    void waitForLastTask() {
        LOGGER.info("WAITING");

        this.lockExecutorService.lock();
        try {
            this.executorService.shutdown();
            boolean running = true;
            do {
                running = !this.executorService.awaitTermination(1L, TimeUnit.DAYS);
            } while (running);
            this.executorService = Executors.newFixedThreadPool(NUMBER_THREADS);
        } catch (final InterruptedException ex) {
            LOGGER.warning(Objects.requireNonNull(ex.getMessage()));
        } finally {
            this.lockExecutorService.unlock();
        }

        LOGGER.info("WAITED");
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
    private void copyFrame(
            final ByteBuffer bbVertices,
            final ByteBuffer bbColors,
            final ByteBuffer bbCamera,
            final int numPrimitives
    ) throws LowMemoryException {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
        checksGLError();

        final int floatSize = Float.SIZE / Byte.SIZE;
        final int triangleMembers = floatSize * 9;
        final int triangleMethods = 8 * 11;
        final int triangleSize = triangleMembers + triangleMethods;
        final int neededMemoryMb = 1 + ((numPrimitives * triangleSize) / 1048576);

        if (isLowMemory(neededMemoryMb)) {
            throw new LowMemoryException();
        }

        bbVertices.order(ByteOrder.nativeOrder());
        bbVertices.position(0);

        if (isLowMemory(1)) {
            throw new LowMemoryException();
        }

        bbColors.order(ByteOrder.nativeOrder());
        bbColors.position(0);

        if (isLowMemory(1)) {
            throw new LowMemoryException();
        }

        bbCamera.order(ByteOrder.nativeOrder());
        bbCamera.position(0);

        if (isLowMemory(1)) {
            throw new LowMemoryException();
        }

        // Create Program
        if (this.shaderProgramRaster != 0) {
            GLES20.glDeleteProgram(this.shaderProgramRaster);
            checksGLError();
            this.shaderProgramRaster = 0;
        }
        this.shaderProgramRaster = GLES20.glCreateProgram();
        checksGLError();

        final int positionAttrib2 = 0;
        GLES20.glBindAttribLocation(this.shaderProgramRaster, positionAttrib2, VERTEX_POSITION);
        checksGLError();
        GLES20.glVertexAttribPointer(positionAttrib2, 4, GLES20.GL_FLOAT, false, 0, bbVertices);
        checksGLError();
        GLES20.glEnableVertexAttribArray(positionAttrib2);
        checksGLError();

        final int colorAttrib2 = 1;
        GLES20.glBindAttribLocation(this.shaderProgramRaster, colorAttrib2, VERTEX_COLOR);
        checksGLError();
        GLES20.glVertexAttribPointer(colorAttrib2, 4, GLES20.GL_FLOAT, false, 0, bbColors);
        checksGLError();
        GLES20.glEnableVertexAttribArray(colorAttrib2);
        checksGLError();

        // Load shaders
        final int vertexShaderRaster = loadShader(GLES20.GL_VERTEX_SHADER, this.vertexShaderCodeRaster);
        final int fragmentShaderRaster = loadShader(GLES20.GL_FRAGMENT_SHADER, this.fragmentShaderCodeRaster);

        // Attach and link shaders to program
        GLES20.glAttachShader(this.shaderProgramRaster, vertexShaderRaster);
        checksGLError();

        GLES20.glAttachShader(this.shaderProgramRaster, fragmentShaderRaster);
        checksGLError();

        GLES20.glLinkProgram(this.shaderProgramRaster);
        checksGLError();

        final int[] attachedShadersRaster = new int[1];
        GLES20.glGetProgramiv(this.shaderProgramRaster, GLES20.GL_ATTACHED_SHADERS, attachedShadersRaster, 0);
        checksGLError();

        final int[] linkStatusRaster = new int[1];
        GLES20.glGetProgramiv(this.shaderProgramRaster, GLES20.GL_LINK_STATUS, linkStatusRaster, 0);
        checksGLError();

        if (isLowMemory(1)) {
            throw new LowMemoryException();
        }

        if (linkStatusRaster[0] != GLES20.GL_TRUE) {
            final String strError = GLES20.glGetProgramInfoLog(this.shaderProgramRaster);
            LOGGER.severe("attachedShadersRaster = " + attachedShadersRaster[0]);
            LOGGER.severe("Could not link program rasterizer: " + strError);
            checksGLError();
            GLES20.glDeleteProgram(this.shaderProgramRaster);
            checksGLError();
            System.exit(1);
        }


        GLES20.glUseProgram(this.shaderProgramRaster);
        checksGLError();

        final float zNear = 0.1F;
        final float zFar = 1.0e38F;

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
            Matrix.orthoM(projectionMatrix, 0, -sizeH / 2.0F, sizeH / 2.0F,
                    -sizeV / 2.0F, sizeV / 2.0F, zNear, zFar);
        }

        Matrix.setLookAtM(viewMatrix, 0,
                eyeX, eyeY, eyeZ,
                centerX, centerY, centerZ,
                upX, upY, upZ);
        final int handleModel = GLES20.glGetUniformLocation(this.shaderProgramRaster, "uniformModelMatrix");
        checksGLError();
        final int handleView = GLES20.glGetUniformLocation(this.shaderProgramRaster, "uniformViewMatrix");
        checksGLError();
        final int handleProjection = GLES20.glGetUniformLocation(this.shaderProgramRaster, "uniformProjectionMatrix");
        checksGLError();

        GLES20.glUniformMatrix4fv(handleModel, 1, false, modelMatrix, 0);
        checksGLError();
        GLES20.glUniformMatrix4fv(handleView, 1, false, viewMatrix, 0);
        checksGLError();
        GLES20.glUniformMatrix4fv(handleProjection, 1, false, projectionMatrix, 0);
        checksGLError();

        if (isLowMemory(1)) {
            throw new LowMemoryException();
        }

        final int positionAttrib = GLES20.glGetAttribLocation(this.shaderProgramRaster, VERTEX_POSITION);
        checksGLError();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, positionAttrib);
        checksGLError();
        GLES20.glEnableVertexAttribArray(positionAttrib);
        checksGLError();
        GLES20.glVertexAttribPointer(positionAttrib, 4, GLES20.GL_FLOAT, false, 0, bbVertices);
        checksGLError();

        if (isLowMemory(1)) {
            throw new LowMemoryException();
        }

        final int colorAttrib = GLES20.glGetAttribLocation(this.shaderProgramRaster, VERTEX_COLOR);
        checksGLError();
        GLES20.glEnableVertexAttribArray(colorAttrib);
        checksGLError();
        GLES20.glVertexAttribPointer(colorAttrib, 4, GLES20.GL_FLOAT, false, 0, bbColors);
        checksGLError();

        if (isLowMemory(1)) {
            throw new LowMemoryException();
        }

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        checksGLError();

        final int vertexCount = bbVertices.capacity() / (floatSize << 2);

        if (!isLowMemory(1)) {
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
            checksGLError();
            LOGGER.info("glDrawArrays Complete");
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        checksGLError();

        GLES20.glDisableVertexAttribArray(positionAttrib);
        checksGLError();
        GLES20.glDisableVertexAttribArray(colorAttrib);
        checksGLError();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        checksGLError();

        this.bitmap = copyFrameBuffer();
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
     * Sets {@link MainRenderer#bitmap}.
     *
     * @param bitmap The new {@link MainRenderer#bitmap}.
     */
    void setBitmap(final Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    /**
     * Sets {@link MainRenderer#rasterize}.
     *
     * @param rasterize The new {@link MainRenderer#rasterize}.
     */
    void setRasterize(final boolean rasterize) {
        this.rasterize = rasterize;
    }

    @Override
    public void onDrawFrame(final GL10 gl) {
        if (this.firstFrame) {
            LOGGER.info("onDrawFrame");

            this.firstFrame = false;
            if (this.rasterize) {
                this.rasterize = false;
                initArrays();
            }
            waitForLastTask();
            if (this.arrayVertices != null && this.arrayColors != null && this.arrayCamera != null) {
                try {
                    copyFrame(this.arrayVertices, this.arrayColors, this.arrayCamera, this.numPrimitives);
                } catch (final LowMemoryException ex) {
                    LOGGER.warning("Low memory to rasterize a frame!!!");
                }
            }

            RTRenderIntoBitmap(this.bitmap, this.numThreads, true);

            final RenderTask.Builder renderTaskBuilder = new RenderTask.Builder(this.requestRender,
                    this::RTFinishRender, this.textView, this.buttonRender);

            this.renderTask = renderTaskBuilder
                    .withUpdateInterval(250L)
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
        }

        GLES20.glUseProgram(this.shaderProgram);
        checksGLError();


        final int positionAttrib = GLES20.glGetAttribLocation(this.shaderProgram, VERTEX_POSITION);
        checksGLError();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, positionAttrib);
        checksGLError();
        GLES20.glEnableVertexAttribArray(positionAttrib);
        checksGLError();
        GLES20.glVertexAttribPointer(positionAttrib, 4, GLES20.GL_FLOAT, false, 0,
                this.floatBufferVertices);
        checksGLError();

        final int texCoordAttrib = GLES20.glGetAttribLocation(this.shaderProgram, VERTEX_TEX_COORD);
        checksGLError();
        GLES20.glEnableVertexAttribArray(texCoordAttrib);
        checksGLError();
        GLES20.glVertexAttribPointer(texCoordAttrib, 2, GLES20.GL_FLOAT, false, 0,
                this.floatBufferTexture);
        checksGLError();


        final int vertexCount = this.verticesTexture.length / (Float.SIZE / Byte.SIZE);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, positionAttrib, vertexCount);
        checksGLError();

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, this.bitmap, GLES20.GL_UNSIGNED_BYTE, 0);
        checksGLError();

        GLES20.glDisableVertexAttribArray(positionAttrib);
        checksGLError();
        GLES20.glDisableVertexAttribArray(texCoordAttrib);
        checksGLError();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        checksGLError();
    }

    @Override
    public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
        LOGGER.info("onSurfaceChanged");

        GLES20.glViewport(0, 0, width, height);
        checksGLError();

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, this.bitmap, 0);
        checksGLError();
    }

    @Override
    public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
        LOGGER.info("onSurfaceCreated");

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
        checksGLError();

        // Enable culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        checksGLError();

        GLES20.glEnable(GLES20.GL_BLEND);
        checksGLError();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        checksGLError();

        GLES20.glCullFace(GLES20.GL_BACK);
        checksGLError();

        GLES20.glFrontFace(GLES20.GL_CCW);
        checksGLError();

        GLES20.glClearDepthf(1.0F);
        checksGLError();

        GLES20.glDepthMask(true);
        checksGLError();

        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        checksGLError();

        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        checksGLError();

        // Create geometry and texCoords buffers
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
        final int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, this.vertexShaderCode);
        final int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, this.fragmentShaderCode);

        // Create Program
        this.shaderProgram = GLES20.glCreateProgram();
        checksGLError();

        if (this.shaderProgram == 0) {
            LOGGER.severe("Could not create program: ");
            LOGGER.severe(GLES20.glGetProgramInfoLog(0));
            System.exit(1);
        }

        // Attach and link shaders to program
        GLES20.glAttachShader(this.shaderProgram, vertexShader);
        checksGLError();

        GLES20.glAttachShader(this.shaderProgram, fragmentShader);
        checksGLError();


        final int numTextures = 1;
        final int[] textureHandle = new int[numTextures];
        GLES20.glGenTextures(numTextures, textureHandle, 0);
        if (textureHandle[0] == 0) {
            LOGGER.severe("Error loading texture.");
            System.exit(1);
        }


        // Bind Attributes
        final int positionAttrib = 0;
        GLES20.glBindAttribLocation(this.shaderProgram, positionAttrib, VERTEX_POSITION);
        checksGLError();
        GLES20.glVertexAttribPointer(positionAttrib, 4, GLES20.GL_FLOAT, false, 0, this.floatBufferVertices);
        checksGLError();
        GLES20.glEnableVertexAttribArray(positionAttrib);
        checksGLError();

        final int texCoordAttrib = 1;
        GLES20.glBindAttribLocation(this.shaderProgram, texCoordAttrib, VERTEX_TEX_COORD);
        checksGLError();
        GLES20.glVertexAttribPointer(texCoordAttrib, 2, GLES20.GL_FLOAT, false, 0, this.floatBufferTexture);
        checksGLError();
        GLES20.glEnableVertexAttribArray(texCoordAttrib);
        checksGLError();


        GLES20.glLinkProgram(this.shaderProgram);
        checksGLError();

        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(this.shaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
        checksGLError();

        if (linkStatus[0] != GLES20.GL_TRUE) {
            LOGGER.severe(GLES20.glGetProgramInfoLog(this.shaderProgram));
            GLES20.glDeleteProgram(this.shaderProgram);
            System.exit(1);
        }

        // Shader program 1
        GLES20.glUseProgram(this.shaderProgram);
        checksGLError();

        // Bind to the texture in OpenGL
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        checksGLError();

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
        checksGLError();

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        checksGLError();

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        checksGLError();
    }
}
