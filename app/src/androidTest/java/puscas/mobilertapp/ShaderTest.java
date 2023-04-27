package puscas.mobilertapp;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.constants.Shader;
import puscas.mobilertapp.utils.UtilsContext;

/**
 * The test suite for the {@link Shader}s used in {@link MainActivity}.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class ShaderTest extends AbstractTest {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(ShaderTest.class.getSimpleName());

    /**
     * A setup method which is called first.
     */
    @BeforeClass
    public static void setUpAll() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info(methodName);
    }

    /**
     * A tear down method which is called last.
     */
    @AfterClass
    public static void tearDownAll() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info(methodName);
    }

    /**
     * Tests rendering a scene with the No Shadows shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithNoShadows() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        assertRenderScene(numCores, Scene.CORNELL, Shader.NO_SHADOWS, Accelerator.BVH, 1, 1, false);
    }

    /**
     * Tests rendering a scene with the Whitted shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithWhitted() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        assertRenderScene(numCores, Scene.CORNELL, Shader.WHITTED, Accelerator.BVH, 1, 1, false);
    }

    /**
     * Tests rendering a scene with the Path Tracing shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithPathTracing() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        assertRenderScene(numCores, Scene.CORNELL, Shader.PATH_TRACING, Accelerator.BVH, 1, 1, false);
    }

    /**
     * Tests rendering a scene with the Depth Map shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithDepthMap() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        assertRenderScene(numCores, Scene.CORNELL, Shader.DEPTH_MAP, Accelerator.BVH, 1, 1, false);
    }

    /**
     * Tests rendering a scene with the Diffuse shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithDiffuse() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        assertRenderScene(numCores, Scene.CORNELL, Shader.DIFFUSE, Accelerator.BVH, 1, 1, false);
    }

}
