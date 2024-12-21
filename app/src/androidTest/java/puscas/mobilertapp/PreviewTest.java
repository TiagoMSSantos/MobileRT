package puscas.mobilertapp;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.Test;

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
    @Test
    public void testPreviewScenePerspectiveCamera() throws TimeoutException {
        logger.info("testPreviewScenePerspectiveCamera start");

        ViewActionWait.waitForButtonUpdate(0);
        Espresso.onView(ViewMatchers.withId(R.id.preview))
            .inRoot(RootMatchers.isTouchable())
            .check((view, exception) -> {
                UtilsT.rethrowException(exception);
                UiTest.assertPreviewCheckBox(view, true);
            });
        ViewActionWait.waitForButtonUpdate(0);
        UtilsContextT.resetPickerValues(Scene.CORNELL.ordinal(), Accelerator.NAIVE, 99, 1);

        ViewActionWait.waitForButtonUpdate(0);
        UtilsT.startRendering(false);
        UtilsContextT.waitUntil(this.testName.getMethodName(), activity, Constants.STOP, State.BUSY);

        UtilsT.stopRendering();
        UtilsContextT.waitUntil(this.testName.getMethodName(), activity, Constants.RENDER, State.IDLE, State.FINISHED);

        UtilsT.assertRenderButtonText(Constants.RENDER);
        UtilsT.testStateAndBitmap(false);

        logger.info("testPreviewScenePerspectiveCamera finished");
    }

}
