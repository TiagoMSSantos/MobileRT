package puscas.mobilertapp;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import org.junit.Test;

import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.constants.Shader;

/**
 * The test suite for the Ray Tracing engine used in {@link MainActivity}.
 */
public final class RayTracingTest extends AbstractTest {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(RayTracingTest.class.getSimpleName());

    /**
     * Tests render a scene from an OBJ file that doesn't exist.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test
    public void testRenderInvalidScene() throws TimeoutException {
        // Mock the reply as the external file manager application, to select an OBJ file that doesn't exist.
        mockFileManagerReply(false,
            "/path/to/OBJ/file/that/doesn't/exist.obj"
        );

        assertRenderScene(Scene.OBJ, Shader.WHITTED, Accelerator.NAIVE, 1, 1, true, true);
    }

    /**
     * Tests not selecting a file when choosing an OBJ scene in the file manager.
     *
     * @throws TimeoutException If there is a timeout while waiting for the engine to become idle.
     */
    @Test
    public void testNotSelectingScene() throws TimeoutException {
        logger.info("testNotSelectingScene started");
        // Mock the reply as the external file manager application, to not select anything.
        final Intent expectedIntent = MainActivity.createIntentToLoadFiles();
        logger.info("testNotSelectingScene created expectedIntent");
        final Intent resultIntent = MainActivity.createIntentToLoadFiles();
        logger.info("testNotSelectingScene created resultIntent");
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, resultIntent);
        logger.info("testNotSelectingScene created result");
        Intents.intending(IntentMatchers.filterEquals(expectedIntent)).respondWith(result);
        logger.info("testNotSelectingScene created expected Intent");

        assertRenderScene(Scene.OBJ, Shader.WHITTED, Accelerator.NAIVE, 1, 1, true, true);
        logger.info("testNotSelectingScene asserted");
        Intents.intended(IntentMatchers.filterEquals(expectedIntent));
        logger.info("testNotSelectingScene finished");
    }

    /**
     * Tests rendering an OBJ scene from an OBJ file which the path was loaded with an external file
     * manager application. The OBJ is in an internal storage.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     *
     * @implNote E.g. of an URL to file:<br>
     * file:///file/data/local/tmp/MobileRT/WavefrontOBJs/teapot/teapot.obj<br>
     */
    @Test
    public void testRenderSceneFromInternalStorageOBJ() throws TimeoutException {
        mockFileManagerReply(false,
            ConstantsAndroid.CORNELL_BOX_WATER_OBJ,
            ConstantsAndroid.CORNELL_BOX_WATER_MTL,
            ConstantsAndroid.CORNELL_BOX_WATER_CAM
        );

        assertRenderScene(Scene.OBJ, Shader.WHITTED, Accelerator.BVH, 1, 1, false, false);
    }

    /**
     * Tests rendering an OBJ scene from an OBJ file which the path was loaded with an external file
     * manager application. The OBJ is in an external SD card and the scene contains texture(s) in
     * order to also validate that they are properly read.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     *
     * @implNote E.g. of URLs to file:<br>
     * content://com.asus.filemanager.OpenFileProvider/file/storage/1CE6-261B/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj<br>
     * content://com.asus.filemanager.OpenFileProvider/file/mnt/sdcard/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj<br>
     */
    @Test
    public void testRenderSceneFromSDCardOBJ() throws TimeoutException {
        mockFileManagerReply(true,
            "/MobileRT/WavefrontOBJs/teapot/teapot.obj",
            "/MobileRT/WavefrontOBJs/teapot/teapot.mtl",
            "/MobileRT/WavefrontOBJs/teapot/teapot.cam",
            "/MobileRT/WavefrontOBJs/teapot/default.png"
        );

        assertRenderScene(Scene.OBJ, Shader.WHITTED, Accelerator.BVH, 1, 1, false, false);
    }

}
