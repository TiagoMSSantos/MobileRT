package puscas.mobilertapp;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import com.google.common.base.Preconditions;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.Constants;
import puscas.mobilertapp.constants.ConstantsUI;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.utils.UtilsContext;
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
     * @implNote This test can take more than 2 mins in CI.
     */
    @Test(timeout = 3L * 60L * 1000L)
    public void testPreviewScenePerspectiveCamera() throws TimeoutException {
        logger.info("testPreviewScenePerspectiveCamera start");
        Preconditions.checkNotNull(this.activity, "Activity can't be null");

        // Mock the reply as the external file manager application, to select an OBJ file.
        final Intent resultData;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            resultData = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            resultData = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        }
        resultData.addCategory(Intent.CATEGORY_OPENABLE);
        resultData.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            resultData.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        final String sdCardPath = UtilsContext.getSdCardPath(this.activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final Uri objFile = Uri.fromFile(new File(sdCardPath + "/MobileRT/WavefrontOBJs/teapot/teapot.obj"));
            final Uri mtlFile = Uri.fromFile(new File(sdCardPath + "/MobileRT/WavefrontOBJs/teapot/teapot.mtl"));
            final Uri camFile = Uri.fromFile(new File(sdCardPath + "/MobileRT/WavefrontOBJs/teapot/teapot.cam"));
            final Uri textureFile = Uri.fromFile(new File(sdCardPath + "/MobileRT/WavefrontOBJs/teapot/default.png"));
            final ClipData clipData = new ClipData(new ClipDescription("Scene", new String[]{"*" + ConstantsUI.FILE_SEPARATOR + "*"}), new ClipData.Item(objFile));
            clipData.addItem(new ClipData.Item(mtlFile));
            clipData.addItem(new ClipData.Item(camFile));
            clipData.addItem(new ClipData.Item(textureFile));
            resultData.setClipData(clipData);
        } else {
            resultData.setData(Uri.fromFile(new File(sdCardPath + "/MobileRT/WavefrontOBJs/teapot/teapot.obj")));
        }
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result);

        UtilsContextT.resetPickerValues(this.activity, Scene.OBJ.ordinal(), Accelerator.NAIVE, 99, 99);

        UtilsT.startRendering(false);
        UtilsT.stopRendering();

        UtilsContextT.waitUntilRenderingDone(this.activity);

        UtilsT.assertRenderButtonText(Constants.RENDER);

        UtilsT.testStateAndBitmap(false);
        Intents.intended(IntentMatchers.anyIntent());
        logger.info("testPreviewScenePerspectiveCamera finished");
    }

    /**
     * Tests the preview feature in a scene which uses orthographic camera.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     * @implNote This test can take more than 1 min in CI.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testPreviewSceneOrthographicCamera() throws TimeoutException {
        logger.info("testPreviewSceneOrthographicCamera start");
        Preconditions.checkNotNull(this.activity, "Activity can't be null");

        UtilsContextT.resetPickerValues(this.activity, Scene.SPHERES.ordinal(), Accelerator.NAIVE, 99, 99);

        UtilsT.startRendering(false);
        UtilsT.stopRendering();

        UtilsContextT.waitUntilRenderingDone(this.activity);

        UtilsT.assertRenderButtonText(Constants.RENDER);

        UtilsT.testStateAndBitmap(false);
        logger.info("testPreviewSceneOrthographicCamera finished");
    }

}
