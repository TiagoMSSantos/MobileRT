package puscas.mobilertapp;

import android.opengl.GLES20;

import androidx.test.rule.ActivityTestRule;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import puscas.mobilertapp.utils.ConstantsUI;

public final class MainRendererTest {

    /**
     * The {@link Logger} for this class.
     */
    @Nonnull
    private static final Logger LOGGER = Logger.getLogger(MainRendererTest.class.getName());

    /**
     * The rule to create the MainActivity.
     */
    @Nonnull
    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule =
        new ActivityTestRule<>(MainActivity.class, true, true);

    /**
     * The rule for the timeout for each test.
     */
    @Nonnull
    @Rule
    public final TestRule timeoutRule = new Timeout(1L, TimeUnit.MINUTES);

    /**
     * The rule for the timeout for all the tests.
     */
    @Nonnull
    @ClassRule
    public static final TestRule timeoutClassRule = new Timeout(1L, TimeUnit.MINUTES);

    /**
     * The MainActivity to test.
     */
    private MainActivity activity = null;

    /**
     * A setup method which is called first.
     */
    @BeforeClass
    public static void setUpAll() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * A tear down method which is called last.
     */
    @AfterClass
    public static void tearDownAll() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Setup method called before each test.
     */
    @Before
    public void setUp() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        this.activity = this.mainActivityActivityTestRule.getActivity();
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        this.activity.finish();
        this.mainActivityActivityTestRule.finishActivity();
        this.activity = null;
    }

    /**
     * Tests loading a Vertex GLSL shader.
     */
    @Test
    public void testLoadVertexShader () throws InterruptedException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final String shaderPath = ConstantsUI.PATH_SHADERS + ConstantsUI.FILE_SEPARATOR + "VertexShader.glsl";

        final int shaderIndex = getIndexOfShader(shaderPath, GLES20.GL_VERTEX_SHADER);
        Assertions.assertEquals(
            4,
            shaderIndex,
            "Shader index should be 4."
        );
    }

    /**
     * Tests loading a Fragment GLSL shader.
     */
    @Test
    public void testLoadFragmentShader () throws InterruptedException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final String shaderPath = ConstantsUI.PATH_SHADERS + ConstantsUI.FILE_SEPARATOR + "FragmentShader.glsl";

        final int shaderIndex = getIndexOfShader(shaderPath, GLES20.GL_FRAGMENT_SHADER);
        Assertions.assertEquals(
            4,
            shaderIndex,
            "Shader index should be 4."
        );
    }

    /**
     * Loads a file with a GLSL shader code and creates a GLSL shader in OpenGL.
     *
     * @implNote This method uses {@link MainActivity#readTextAsset} to read the
     * GLSL shader file and {@link MainRenderer#loadShader} to create the shader
     * in the OpenGL framework.
     * @param shaderPath The path to the file with GLSL shader code.
     * @param shaderType The type of the GLSL shader. It can be vertex or fragment shader.
     * @return The index of the new created GLSL shader.
     * @throws InterruptedException If the thread waiting for the {@link CountDownLatch} was interrupted.
     */
    private int getIndexOfShader(final String shaderPath, final int shaderType) throws InterruptedException {
        final DrawView drawView = Utils.getPrivateField(this.activity, "drawView");
        final MainRenderer renderer = drawView.getRenderer();

        final String shaderCode = Utils.invokePrivateMethod(this.activity, "readTextAsset",
            ImmutableList.of(String.class),
            ImmutableList.of(shaderPath)
        );

        final AtomicInteger shaderIndex = new AtomicInteger(-1);
        final CountDownLatch latch = new CountDownLatch(1);
        // We need to call `loadShader` method with the GL rendering thread.
        drawView.queueEvent(() -> {
            final int index = Utils.invokePrivateMethod(renderer, "loadShader",
                ImmutableList.of(int.class, String.class),
                ImmutableList.of(shaderType, shaderCode)
            );
            shaderIndex.set(index);
            latch.countDown();
        });
        latch.await(1L, TimeUnit.MINUTES);
        return shaderIndex.get();
    }
}
