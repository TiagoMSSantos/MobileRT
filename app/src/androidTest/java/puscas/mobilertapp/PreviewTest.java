package puscas.mobilertapp;

import com.google.common.base.Preconditions;
import java.util.concurrent.TimeoutException;
import lombok.extern.java.Log;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import puscas.mobilertapp.constants.Constants;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.utils.UtilsContextT;
import puscas.mobilertapp.utils.UtilsT;

/**
 * The test suite for the preview feature (rasterize one frame of the scene).
 */
@TestMethodOrder(MethodOrderer.Random.class)
@Log
final class PreviewTest extends AbstractTest {

    /**
     * Tests the preview feature in a scene.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test
    void testPreviewScene() throws TimeoutException {
        Preconditions.checkNotNull(this.activity, "Activity can't be null");

        UtilsContextT.resetPickerValues(this.activity, Scene.CORNELL2.ordinal());

        UtilsT.startRendering();
        UtilsT.stopRendering();

        UtilsContextT.waitUntilRenderingDone(this.activity);

        UtilsT.assertRenderButtonText(Constants.RENDER);

        UtilsT.testStateAndBitmap(false);
    }

}
