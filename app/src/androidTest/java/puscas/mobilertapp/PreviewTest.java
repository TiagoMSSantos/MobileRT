package puscas.mobilertapp;

import android.graphics.Bitmap;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.FlakyTest;

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
        Espresso.onView(ViewMatchers.withId(R.id.preview))
            .inRoot(RootMatchers.isTouchable())
            .check((view, exception) -> {
                UtilsT.rethrowException(exception);
                UiTest.assertPreviewCheckBox(view, true);
            });
        ViewActionWait.waitForButtonUpdate(0);
        UtilsContextT.resetPickerValues(Scene.SPHERES.ordinal(), Accelerator.NAIVE, 99, 1);

        ViewActionWait.waitForButtonUpdate(0);
        UtilsT.testStateAndBitmap(true);
        UtilsT.startRendering(false);
        UtilsContextT.waitUntil(this.testName.getMethodName(), activity, Constants.STOP, State.BUSY);
        Espresso.onView(ViewMatchers.withId(R.id.drawLayout))
            .inRoot(RootMatchers.isTouchable())
            .perform(new ViewActionWait<>(0, R.id.drawLayout))
            .check((view, exception) -> {
                UtilsT.rethrowException(exception);
                // TODO: Fix bitmap losing rendered pixels.
                final DrawView drawView = (DrawView) view;
                final MainRenderer renderer = drawView.getRenderer();
                final Bitmap bitmap = UtilsT.getPrivateField(renderer, "bitmap");
                UtilsT.assertRayTracingResultInBitmap(bitmap, false);
            });
        ViewActionWait.waitForButtonUpdate(0);

        UtilsT.stopRendering();
        Espresso.onView(ViewMatchers.withId(R.id.drawLayout))
            .inRoot(RootMatchers.isTouchable())
            .perform(new ViewActionWait<>(0, R.id.drawLayout))
            .check((view, exception) -> {
                UtilsT.rethrowException(exception);
                // TODO: Fix bitmap losing rendered pixels.
                final DrawView drawView = (DrawView) view;
                final MainRenderer renderer = drawView.getRenderer();
                final Bitmap bitmap = UtilsT.getPrivateField(renderer, "bitmap");
                UtilsT.assertRayTracingResultInBitmap(bitmap, false);
            });
        ViewActionWait.waitForButtonUpdate(0);
        UtilsContextT.waitUntil(this.testName.getMethodName(), activity, Constants.RENDER, State.IDLE, State.FINISHED);
        // TODO: Fix bitmap losing rendered pixels.
        UtilsT.testStateAndBitmap(false);

        UtilsT.assertRenderButtonText(Constants.RENDER);
        // TODO: Fix bitmap losing rendered pixels.
        UtilsT.testStateAndBitmap(false);

        logger.info("testPreviewSceneOrthographicCamera finished");
    }

}
