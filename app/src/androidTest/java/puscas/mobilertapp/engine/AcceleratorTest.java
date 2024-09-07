package puscas.mobilertapp.engine;

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

/**
 * The test suite for the {@link Accelerator}s used in {@link MainActivity}.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class AcceleratorTest extends AbstractTest {

    /**
     * Tests rendering a scene with the {@link Accelerator#NAIVE} accelerator.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithNaive() throws TimeoutException {
        mockFileManagerReply(false,
            ConstantsAndroidTests.CORNELL_BOX_WATER_OBJ,
            ConstantsAndroidTests.CORNELL_BOX_WATER_MTL,
            ConstantsAndroidTests.CORNELL_BOX_WATER_CAM
        );

        assertRenderScene(Scene.OBJ, Shader.WHITTED, Accelerator.NAIVE, 1, 1, false, false);
    }

    /**
     * Tests rendering a scene with the {@link Accelerator#REG_GRID} accelerator.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithRegularGrid() throws TimeoutException {
        mockFileManagerReply(false,
            ConstantsAndroidTests.CORNELL_BOX_WATER_OBJ,
            ConstantsAndroidTests.CORNELL_BOX_WATER_MTL,
            ConstantsAndroidTests.CORNELL_BOX_WATER_CAM
        );

        assertRenderScene(Scene.OBJ, Shader.WHITTED, Accelerator.REG_GRID, 1, 1, false, false);
    }

    /**
     * Tests rendering a scene with the {@link Accelerator#BVH} accelerator.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithBVH() throws TimeoutException {
        mockFileManagerReply(false,
            ConstantsAndroidTests.CORNELL_BOX_WATER_OBJ,
            ConstantsAndroidTests.CORNELL_BOX_WATER_MTL,
            ConstantsAndroidTests.CORNELL_BOX_WATER_CAM
        );

        assertRenderScene(Scene.OBJ, Shader.WHITTED, Accelerator.BVH, 1, 1, false, false);
    }

}
