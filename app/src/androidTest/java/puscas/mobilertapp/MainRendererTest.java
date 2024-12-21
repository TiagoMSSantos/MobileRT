package puscas.mobilertapp;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.google.common.base.Preconditions;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import puscas.mobilertapp.exceptions.FailureException;
import puscas.mobilertapp.utils.UtilsGL;
import puscas.mobilertapp.utils.UtilsLogging;
import puscas.mobilertapp.utils.UtilsShader;
import puscas.mobilertapp.utils.UtilsT;

/**
 * The test suite for the {@link MainRenderer}.
 */
public final class MainRendererTest extends AbstractTest {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(MainRendererTest.class.getSimpleName());

    /**
     * Tests loading a Vertex GLSL shader for the Ray Tracing engine to output the rendered scene.
     *
     * @throws InterruptedException If any error occurs.
     */
    @Test
    public void testLoadVertexShader() throws InterruptedException {
        testLoadShader("Shaders/VertexShader.glsl", GLES20.GL_VERTEX_SHADER);
    }

    /**
     * Tests loading a Vertex GLSL shader for the preview (rasterization) feature.
     *
     * @throws InterruptedException If any error occurs.
     */
    @Test
    public void testLoadVertexShaderRaster() throws InterruptedException {
        testLoadShader("Shaders/VertexShaderRaster.glsl", GLES20.GL_VERTEX_SHADER);
    }

    /**
     * Tests loading a Fragment GLSL shader for the Ray Tracing engine to output the rendered scene.
     *
     * @throws InterruptedException If any error occurs.
     */
    @Test
    public void testLoadFragmentShader() throws InterruptedException {
        testLoadShader("Shaders/FragmentShader.glsl", GLES20.GL_FRAGMENT_SHADER);
    }

    /**
     * Tests loading a Fragment GLSL shader for the preview (rasterization) feature.
     *
     * @throws InterruptedException If any error occurs.
     */
    @Test
    public void testLoadFragmentShaderRaster() throws InterruptedException {
        testLoadShader("Shaders/FragmentShaderRaster.glsl", GLES20.GL_FRAGMENT_SHADER);
    }

    /**
     * Helper method that asserts the index of a GLSL shader with the expected.
     *
     * @param shaderIndex   The index of the shader.
     */
    private static void assertIndexOfShader(final int shaderIndex) {
        final int expectedIndex = 4;
        Assert.assertEquals(
            "Shader index should be " + expectedIndex + ".",
            expectedIndex,
            shaderIndex
        );
    }

    /**
     * Helper method that tests loading a GLSL shader and asserts the expected loaded index.
     *
     * @param shaderPath The path to a GLSL shader file.
     * @param shaderType The type of the shader (e.g.: vertex, fragment).
     * @throws InterruptedException If any error occurs.
     */
    private void testLoadShader(final String shaderPath, final int shaderType)
        throws InterruptedException {

        final InputStream inputStream = createInputStreamFromResource(shaderPath);
        final String shaderCode =
            puscas.mobilertapp.utils.Utils.readTextFromInputStream(inputStream);

        final int shaderIndex = createAndGetIndexOfShader(shaderCode, shaderType);
        assertIndexOfShader(shaderIndex);
    }

    /**
     * Creates an {@link InputStream} from a file in the resources.
     *
     * @param filePath The path to the file to read in the resources.
     * @return An {@link InputStream} from the file.
     */
    private InputStream createInputStreamFromResource(final String filePath) {
        final ClassLoader classLoader = getClass().getClassLoader();
        Preconditions.checkNotNull(classLoader, "classLoader shouldn't be null");
        return classLoader.getResourceAsStream(filePath);
    }

    /**
     * Loads a file with a GLSL shader code and creates a GLSL shader in OpenGL.
     *
     * @param shaderCode The GLSL shader code.
     * @param shaderType The type of the GLSL shader. It can be vertex or fragment shader.
     * @return The index of the new created GLSL shader.
     * @throws InterruptedException If the thread waiting for the {@link CountDownLatch}
     *                              was interrupted.
     * @implNote This method uses {@link UtilsShader#loadShader} to create
     *           the shader in the OpenGL framework. To do that, it uses a GL thread to
     *           execute the {@link UtilsShader#loadShader} method by placing its call
     *           in the {@link GLSurfaceView#queueEvent(java.lang.Runnable)}.
     */
    private int createAndGetIndexOfShader(final String shaderCode, final int shaderType)
        throws InterruptedException {

        final DrawView drawView = UtilsT.getPrivateField(activity, "drawView");
        Preconditions.checkNotNull(drawView, "drawView shouldn't be null");

        final AtomicInteger shaderIndex = new AtomicInteger(-1);
        final CountDownLatch latch = new CountDownLatch(2);
        drawView.requestRender();
        logger.info("Calling `UtilsShader#loadShader` method with the GL rendering thread.");
        drawView.queueEvent(() -> {
            latch.countDown();
            try {
                final int index = UtilsShader.loadShader(shaderType, shaderCode);
                shaderIndex.set(index);
                UtilsGL.run(() -> GLES20.glDeleteShader(index));
            } catch (final FailureException ex) {
                UtilsLogging.logThrowable(ex, this.testName.getMethodName() + ": MainRendererTest#createAndGetIndexOfShader");
            } finally {
                UtilsGL.run(GLES20::glReleaseShaderCompiler);
                latch.countDown();
            }
        });
        final String errorMessage = "UtilsShader#loadShader should be called by the GL rendering thread.";
        try {
            Assert.assertTrue(errorMessage, latch.await(20L, TimeUnit.SECONDS));
        } catch (final AssertionError ex) {
            throw new RuntimeException(errorMessage + " (" + latch + ")", ex);
        }

        return shaderIndex.get();
    }

}
