package puscas.mobilertapp;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.constants.Shader;
import puscas.mobilertapp.utils.UtilsContext;

/**
 * The test suite for the {@link Accelerator}s used in {@link MainActivity}.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class AcceleratorTest extends AbstractTest {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(AcceleratorTest.class.getSimpleName());

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
     * Tests rendering a scene without any {@link Accelerator}.
     * It shouldn't render anything and be just a black image.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Ignore("It's failing in CI for Linux machines")
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithoutAccelerator() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        assertRenderScene(numCores, Scene.CORNELL, Shader.WHITTED, Accelerator.NONE, 1, 1, true);
    }

    /**
     * Tests rendering a scene with the {@link Accelerator#NAIVE} accelerator.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithNaive() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        assertRenderScene(numCores, Scene.CORNELL, Shader.WHITTED, Accelerator.NAIVE, 1, 1, false);
    }

    /**
     * Tests rendering a scene with the {@link Accelerator#REG_GRID} accelerator.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithRegularGrid() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        assertRenderScene(numCores, Scene.CORNELL, Shader.WHITTED, Accelerator.REG_GRID, 1, 1, false);
    }

    /**
     * Tests rendering a scene with the {@link Accelerator#BVH} accelerator.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithBVH() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        assertRenderScene(numCores, Scene.CORNELL, Shader.WHITTED, Accelerator.BVH, 1, 1, false);
    }

}
