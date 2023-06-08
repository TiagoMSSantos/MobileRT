package puscas.mobilertapp.engine;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeoutException;

import puscas.mobilertapp.AbstractTest;
import puscas.mobilertapp.ConstantsAndroidTests;
import puscas.mobilertapp.MainActivity;
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
     * Tests rendering a scene with the No Shadows shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithNoShadows() throws TimeoutException {
        mockFileManagerReply(false,
            ConstantsAndroidTests.CORNELL_BOX_WATER_OBJ,
            ConstantsAndroidTests.CORNELL_BOX_WATER_MTL,
            ConstantsAndroidTests.CORNELL_BOX_WATER_CAM
        );

        final int numCores = UtilsContext.getNumOfCores(this.activity);
        assertRenderScene(numCores, Scene.OBJ, Shader.NO_SHADOWS, Accelerator.BVH, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

    /**
     * Tests rendering a scene with the Whitted shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithWhitted() throws TimeoutException {
        mockFileManagerReply(false,
            ConstantsAndroidTests.CORNELL_BOX_WATER_OBJ,
            ConstantsAndroidTests.CORNELL_BOX_WATER_MTL,
            ConstantsAndroidTests.CORNELL_BOX_WATER_CAM
        );

        final int numCores = UtilsContext.getNumOfCores(this.activity);
        assertRenderScene(numCores, Scene.OBJ, Shader.WHITTED, Accelerator.BVH, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

    /**
     * Tests rendering a scene with the Path Tracing shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithPathTracing() throws TimeoutException {
        mockFileManagerReply(false,
            ConstantsAndroidTests.CORNELL_BOX_WATER_OBJ,
            ConstantsAndroidTests.CORNELL_BOX_WATER_MTL,
            ConstantsAndroidTests.CORNELL_BOX_WATER_CAM
        );

        final int numCores = UtilsContext.getNumOfCores(this.activity);
        assertRenderScene(numCores, Scene.OBJ, Shader.PATH_TRACING, Accelerator.BVH, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

    /**
     * Tests rendering a scene with the Depth Map shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithDepthMap() throws TimeoutException {
        mockFileManagerReply(false,
            ConstantsAndroidTests.CORNELL_BOX_WATER_OBJ,
            ConstantsAndroidTests.CORNELL_BOX_WATER_MTL,
            ConstantsAndroidTests.CORNELL_BOX_WATER_CAM
        );

        final int numCores = UtilsContext.getNumOfCores(this.activity);
        assertRenderScene(numCores, Scene.OBJ, Shader.DEPTH_MAP, Accelerator.BVH, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

    /**
     * Tests rendering a scene with the Diffuse shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithDiffuse() throws TimeoutException {
        mockFileManagerReply(false,
            ConstantsAndroidTests.CORNELL_BOX_WATER_OBJ,
            ConstantsAndroidTests.CORNELL_BOX_WATER_MTL,
            ConstantsAndroidTests.CORNELL_BOX_WATER_CAM
        );

        final int numCores = UtilsContext.getNumOfCores(this.activity);
        assertRenderScene(numCores, Scene.OBJ, Shader.DIFFUSE, Accelerator.BVH, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

}
