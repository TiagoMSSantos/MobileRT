package puscas.mobilertapp.engine;

import org.junit.Test;

import java.util.concurrent.TimeoutException;

import puscas.mobilertapp.AbstractTest;
import puscas.mobilertapp.ConstantsAndroidTests;
import puscas.mobilertapp.MainActivity;
import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.constants.Shader;

/**
 * The test suite for the {@link Shader}s used in {@link MainActivity}.
 */
public final class ShaderTest extends AbstractTest {

    /**
     * Tests rendering a scene with the No Shadows shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test
    public void testRenderSceneWithNoShadows() throws TimeoutException {
        mockFileManagerReply(false,
            ConstantsAndroidTests.CORNELL_BOX_WATER_OBJ,
            ConstantsAndroidTests.CORNELL_BOX_WATER_MTL,
            ConstantsAndroidTests.CORNELL_BOX_WATER_CAM
        );

        assertRenderScene(Scene.OBJ, Shader.NO_SHADOWS, Accelerator.BVH, 1, 1, false, false);
    }

    /**
     * Tests rendering a scene with the Whitted shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test
    public void testRenderSceneWithWhitted() throws TimeoutException {
        mockFileManagerReply(false,
            ConstantsAndroidTests.CORNELL_BOX_WATER_OBJ,
            ConstantsAndroidTests.CORNELL_BOX_WATER_MTL,
            ConstantsAndroidTests.CORNELL_BOX_WATER_CAM
        );

        assertRenderScene(Scene.OBJ, Shader.WHITTED, Accelerator.BVH, 1, 1, false, false);
    }

    /**
     * Tests rendering a scene with the Path Tracing shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test
    public void testRenderSceneWithPathTracing() throws TimeoutException {
        mockFileManagerReply(false,
            ConstantsAndroidTests.CORNELL_BOX_WATER_OBJ,
            ConstantsAndroidTests.CORNELL_BOX_WATER_MTL,
            ConstantsAndroidTests.CORNELL_BOX_WATER_CAM
        );

        assertRenderScene(Scene.OBJ, Shader.PATH_TRACING, Accelerator.BVH, 1, 1, false, false);
    }

    /**
     * Tests rendering a scene with the Depth Map shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test
    public void testRenderSceneWithDepthMap() throws TimeoutException {
        mockFileManagerReply(false,
            ConstantsAndroidTests.CORNELL_BOX_WATER_OBJ,
            ConstantsAndroidTests.CORNELL_BOX_WATER_MTL,
            ConstantsAndroidTests.CORNELL_BOX_WATER_CAM
        );

        assertRenderScene(Scene.OBJ, Shader.DEPTH_MAP, Accelerator.BVH, 1, 1, false, false);
    }

    /**
     * Tests rendering a scene with the Diffuse shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test
    public void testRenderSceneWithDiffuse() throws TimeoutException {
        mockFileManagerReply(false,
            ConstantsAndroidTests.CORNELL_BOX_WATER_OBJ,
            ConstantsAndroidTests.CORNELL_BOX_WATER_MTL,
            ConstantsAndroidTests.CORNELL_BOX_WATER_CAM
        );

        assertRenderScene(Scene.OBJ, Shader.DIFFUSE, Accelerator.BVH, 1, 1, false, false);
    }

}
