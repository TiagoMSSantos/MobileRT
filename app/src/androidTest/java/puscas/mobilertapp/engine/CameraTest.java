package puscas.mobilertapp.engine;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeoutException;

import puscas.mobilertapp.AbstractTest;
import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.constants.Shader;
import puscas.mobilertapp.utils.UtilsContext;

/**
 * The test suite for the cameras used in Ray Tracing engine.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class CameraTest extends AbstractTest {

    /**
     * Tests rendering a scene with the Orthographic camera.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithOrthographic() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        assertRenderScene(numCores, Scene.SPHERES, Shader.WHITTED, Accelerator.BVH, 1, 1, false);
    }

    /**
     * Tests rendering a scene with the Perspective camera.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithPerspective() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        assertRenderScene(numCores, Scene.CORNELL, Shader.WHITTED, Accelerator.BVH, 1, 1, false);
    }

}
