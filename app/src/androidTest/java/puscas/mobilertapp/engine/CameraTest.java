package puscas.mobilertapp.engine;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeoutException;

import puscas.mobilertapp.AbstractTest;
import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.constants.Shader;

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
    @Test(timeout = 3L * 60L * 1000L)
    public void testRenderSceneWithOrthographic() throws TimeoutException {
        assertRenderScene(Scene.SPHERES, Shader.WHITTED, Accelerator.NAIVE, 6, 1, false, false);
    }

    /**
     * Tests rendering a scene with the Perspective camera.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 3L * 60L * 1000L)
    public void testRenderSceneWithPerspective() throws TimeoutException {
        assertRenderScene(Scene.CORNELL, Shader.WHITTED, Accelerator.NAIVE, 6, 1, false, false);
    }

}
