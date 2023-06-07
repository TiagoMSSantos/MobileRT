package puscas.mobilertapp.engine;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeoutException;

import puscas.mobilertapp.AbstractTest;
import puscas.mobilertapp.MainActivity;
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
     * Tests rendering a scene with the {@link Accelerator#NAIVE} accelerator.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithNaive() throws TimeoutException {
        mockFileManagerReply(false,
            "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj",
            "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.mtl",
            "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.cam"
        );

        final int numCores = UtilsContext.getNumOfCores(this.activity);
        assertRenderScene(numCores, Scene.OBJ, Shader.WHITTED, Accelerator.NAIVE, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

    /**
     * Tests rendering a scene with the {@link Accelerator#REG_GRID} accelerator.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithRegularGrid() throws TimeoutException {
        mockFileManagerReply(false,
            "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj",
            "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.mtl",
            "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.cam"
        );

        final int numCores = UtilsContext.getNumOfCores(this.activity);
        assertRenderScene(numCores, Scene.OBJ, Shader.WHITTED, Accelerator.REG_GRID, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

    /**
     * Tests rendering a scene with the {@link Accelerator#BVH} accelerator.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithBVH() throws TimeoutException {
        mockFileManagerReply(false,
            "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj",
            "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.mtl",
            "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.cam"
        );

        final int numCores = UtilsContext.getNumOfCores(this.activity);
        assertRenderScene(numCores, Scene.OBJ, Shader.WHITTED, Accelerator.BVH, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

}
