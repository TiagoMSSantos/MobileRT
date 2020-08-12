package puscas.mobilertapp;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runners.MethodSorters;
import puscas.mobilertapp.utils.UtilsGL;
import puscas.mobilertapp.utils.UtilsShader;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class MainRendererTest extends AbstractTest {

    /**
     * The {@link Logger} for this class.
     */
    @Nonnull
    private static final Logger LOGGER = Logger.getLogger(MainRendererTest.class.getName());

    /**
     * Helper method that asserts the index of a GLSL shader with the expected.
     *
     * @param shaderIndex   The index of the shader.
     * @param expectedIndex The expected index.
     */
    private static void assertIndexOfShader(final int shaderIndex, final int expectedIndex) {
        Assertions.assertEquals(
            expectedIndex,
            shaderIndex,
            "Shader index should be " + expectedIndex + "."
        );
    }

    /**
     * Setup method called before each test.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();

        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Tear down method called after each test.
     */
    @After
    @Override
    public void tearDown() {
        super.tearDown();

        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Tests loading a Vertex GLSL shader.
     */
    @Test
    public void testLoadVertexShader() throws InterruptedException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        testLoadShader("Shaders/VertexShader.glsl", GLES20.GL_VERTEX_SHADER);
    }

    /**
     * Tests loading a Vertex GLSL shader.
     */
    @Test
    public void testLoadVertexShaderRaster() throws InterruptedException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        testLoadShader("Shaders/VertexShaderRaster.glsl", GLES20.GL_VERTEX_SHADER);
    }

    /**
     * Tests loading a Fragment GLSL shader.
     */
    @Test
    public void testLoadFragmentShader() throws InterruptedException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        testLoadShader("Shaders/FragmentShader.glsl", GLES20.GL_FRAGMENT_SHADER);
    }

    /**
     * Tests loading a Fragment GLSL shader.
     */
    @Test
    public void testLoadFragmentShaderRaster() throws InterruptedException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        testLoadShader("Shaders/FragmentShaderRaster.glsl", GLES20.GL_FRAGMENT_SHADER);
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
        assertIndexOfShader(shaderIndex, 4);
    }

    /**
     * Creates an {@link InputStream} from a file in the resources.
     *
     * @param filePath The path to the file to read in the resources.
     * @return An {@link InputStream} from the file.
     */
    private InputStream createInputStreamFromResource(final String filePath) {
        final ClassLoader classLoader = getClass().getClassLoader();
        assert classLoader != null;
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
     * @implNote This method uses {@link UtilsGL#loadShader} to create
     * the shader in the OpenGL framework. To do that, it uses a GL thread to
     * execute the {@link UtilsGL#loadShader} method by placing its call
     * in the {@link GLSurfaceView#queueEvent(java.lang.Runnable)}.
     */
    private int createAndGetIndexOfShader(final String shaderCode, final int shaderType)
        throws InterruptedException {
        final DrawView drawView = Utils.getPrivateField(this.activity, "drawView");

        final AtomicInteger shaderIndex = new AtomicInteger(-1);
        final CountDownLatch latch = new CountDownLatch(1);
        // We need to call `loadShader` method with the GL rendering thread.
        drawView.queueEvent(() -> {
            final int index = UtilsShader.loadShader(shaderType, shaderCode);
            shaderIndex.set(index);
            latch.countDown();
        });
        latch.await(1L, TimeUnit.MINUTES);
        return shaderIndex.get();
    }

}
