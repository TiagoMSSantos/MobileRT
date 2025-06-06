package puscas.mobilertapp.engine;

import org.junit.Test;

import java.util.concurrent.TimeoutException;

import puscas.mobilertapp.AbstractTest;
import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.constants.Shader;

/**
 * The test suite for the cameras used in Ray Tracing engine.
 */
public final class CameraTest extends AbstractTest {

    /**
     * Tests rendering a scene with the Orthographic camera.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test
    public void testRenderSceneWithOrthographic() throws TimeoutException {
        assertRenderScene(Scene.SPHERES, Shader.WHITTED, Accelerator.NAIVE, 20, 1, false, false);
    }

    /**
     * Tests rendering a scene with the Perspective camera.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test
    public void testRenderSceneWithPerspective() throws TimeoutException {
        assertRenderScene(Scene.CORNELL, Shader.WHITTED, Accelerator.NAIVE, 20, 1, false, false);
    }

}
