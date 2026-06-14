package puscas.mobilertapp;

import android.graphics.Bitmap;

import androidx.test.filters.FlakyTest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

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
@RunWith(OrderRunner.class)
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
        // Read the preview checkbox focus-free (see DirectInteraction).
        UiTest.assertPreviewCheckBoxDirect(true);
        ViewActionWait.waitForButtonUpdate(0);
        UtilsContextT.resetPickerValues(Scene.CORNELL.ordinal(), Accelerator.NAIVE, 99, 1);

        ViewActionWait.waitForButtonUpdate(0);
        UtilsT.startRendering(false);
        UtilsContextT.waitUntil(this.testName.getMethodName(), activity, Constants.STOP, State.BUSY);

        UtilsT.stopRendering();
        UtilsContextT.waitUntil(this.testName.getMethodName(), activity, Constants.RENDER, State.IDLE, State.FINISHED);

        UtilsT.assertRenderButtonText(Constants.RENDER);
        UtilsT.testStateAndBitmap(false);

        UtilsT.captureScreenshot("CornellBox2.png");
        logger.info("testPreviewScenePerspectiveCamera finished");
    }

    /**
     * Tests the preview feature in a scene which uses orthographic camera.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     * @implNote This test can take more than 1 minute in CI.
     */
    @Test
    @Order(order = 1)
    @FlakyTest
    public void testPreviewSceneOrthographicCamera() throws TimeoutException {
        logger.info("testPreviewSceneOrthographicCamera start");

        ViewActionWait.waitForButtonUpdate(0);
        // Read the preview checkbox focus-free (see DirectInteraction).
        UiTest.assertPreviewCheckBoxDirect(true);
        ViewActionWait.waitForButtonUpdate(0);
        UtilsContextT.resetPickerValues(Scene.SPHERES.ordinal(), Accelerator.NAIVE, 99, 1);

        ViewActionWait.waitForButtonUpdate(0);
        UtilsT.testStateAndBitmap(true);
        UtilsT.startRendering(false);
        UtilsContextT.waitUntil(this.testName.getMethodName(), activity, Constants.STOP, State.BUSY);
        assertRendererBitmap(false);
        ViewActionWait.waitForButtonUpdate(0);

        UtilsT.stopRendering();
        assertRendererBitmap(false);
        ViewActionWait.waitForButtonUpdate(0);
        UtilsContextT.waitUntil(this.testName.getMethodName(), activity, Constants.RENDER, State.IDLE, State.FINISHED);
        UtilsT.testStateAndBitmap(false);

        UtilsT.assertRenderButtonText(Constants.RENDER);
        UtilsT.testStateAndBitmap(false);

        UtilsT.captureScreenshot("Spheres.png");
        logger.info("testPreviewSceneOrthographicCamera finished");
    }

    /** Reads the {@link DrawView}'s renderer focus-free (see {@link DirectInteraction}) and asserts its bitmap. */
    private static void assertRendererBitmap(final boolean expectedSameValues) {
        final MainRenderer renderer = DirectInteraction.readRenderer(R.id.drawLayout);
        Assert.assertNotNull("DrawView/renderer could not be resolved", renderer);
        final Bitmap bitmap = UtilsT.getPrivateField(renderer, "bitmap");
        UtilsT.assertRayTracingResultInBitmap(bitmap, expectedSameValues);
    }

}
