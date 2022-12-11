package puscas.mobilertapp;

import com.google.common.base.Preconditions;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.concurrent.TimeoutException;

import lombok.extern.java.Log;
import puscas.mobilertapp.constants.Constants;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.utils.UtilsContextT;
import puscas.mobilertapp.utils.UtilsT;

/**
 * The test suite for the preview feature (rasterize one frame of the scene).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Log
public final class PreviewTest extends AbstractTest {

    /**
     * Tests the preview feature in a scene.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 5L * 60L * 1000L)
    public void testPreviewScene() throws TimeoutException {
        log.info("testPreviewScene start");
        Preconditions.checkNotNull(this.activity, "Activity can't be null");

        UtilsContextT.resetPickerValues(this.activity, Scene.CORNELL2.ordinal());

        UtilsT.startRendering();
        UtilsT.stopRendering();

        UtilsContextT.waitUntilRenderingDone(this.activity);

        UtilsT.assertRenderButtonText(Constants.RENDER);

        UtilsT.testStateAndBitmap(false);
        log.info("testPreviewScene finished");
    }

}
