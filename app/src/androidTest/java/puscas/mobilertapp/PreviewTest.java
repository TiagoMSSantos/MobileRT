package puscas.mobilertapp;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.Constants;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.constants.State;
import puscas.mobilertapp.utils.UtilsContextT;
import puscas.mobilertapp.utils.UtilsT;

/**
 * The test suite for the preview feature (rasterize one frame of the scene).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class PreviewTest extends AbstractTest {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(PreviewTest.class.getSimpleName());

    /**
     * Tests the preview feature in a scene which uses perspective camera.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     * @implNote This test can take more than 2 minutes in CI.
     */
    @Test(timeout = 3L * 60L * 1000L)
    public void testPreviewScenePerspectiveCamera() throws TimeoutException {
        logger.info("testPreviewScenePerspectiveCamera start");

        mockFileManagerReply(false,
            ConstantsAndroidTests.CORNELL_BOX_WATER_OBJ,
            ConstantsAndroidTests.CORNELL_BOX_WATER_MTL,
            ConstantsAndroidTests.CORNELL_BOX_WATER_CAM
        );

        UtilsContextT.resetPickerValues(this.activity, Scene.OBJ.ordinal(), Accelerator.NAIVE, 99, 99);

        UtilsT.startRendering(false);
        UtilsContextT.waitUntil(this.activity, Constants.STOP, State.BUSY);

        UtilsT.stopRendering();
        UtilsContextT.waitUntil(this.activity, Constants.RENDER, State.IDLE, State.FINISHED);
        UtilsT.waitForAppToIdle();

        UtilsT.assertRenderButtonText(Constants.RENDER);

        UtilsT.testStateAndBitmap(false);
        Intents.intended(IntentMatchers.anyIntent());
        logger.info("testPreviewScenePerspectiveCamera finished");
    }

    /**
     * Tests the preview feature in a scene which uses orthographic camera.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     * @implNote This test can take more than 1 minute in CI.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testPreviewSceneOrthographicCamera() throws TimeoutException {
        logger.info("testPreviewSceneOrthographicCamera start");

        UtilsContextT.resetPickerValues(this.activity, Scene.SPHERES.ordinal(), Accelerator.NAIVE, 99, 99);

        UtilsT.startRendering(false);
        UtilsContextT.waitUntil(this.activity, Constants.STOP, State.BUSY);

        UtilsT.stopRendering();
        UtilsContextT.waitUntil(this.activity, Constants.RENDER, State.IDLE, State.FINISHED);
        UtilsT.waitForAppToIdle();

        UtilsT.assertRenderButtonText(Constants.RENDER);

        UtilsT.testStateAndBitmap(false);
        logger.info("testPreviewSceneOrthographicCamera finished");
    }

}
