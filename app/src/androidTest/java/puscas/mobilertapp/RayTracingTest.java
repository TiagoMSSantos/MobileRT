package puscas.mobilertapp;

import android.graphics.Bitmap;
import android.os.Build;
import java.util.concurrent.TimeoutException;
import lombok.extern.java.Log;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import puscas.mobilertapp.utils.Constants;
import puscas.mobilertapp.utils.ConstantsUI;
import puscas.mobilertapp.utils.Scene;
import puscas.mobilertapp.utils.UtilsContext;
import puscas.mobilertapp.utils.UtilsContextT;
import puscas.mobilertapp.utils.UtilsPickerT;
import puscas.mobilertapp.utils.UtilsT;

/**
 * The test suite for the Ray Tracing engine used in {@link MainActivity}.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Log
public final class RayTracingTest extends AbstractTest {

    /**
     * A setup method which is called first.
     */
    @BeforeClass
    public static void setUpAll() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.info(methodName);

        log.info("---------------------------------------------------");
        final String messageDevice = "Device: " + Build.DEVICE;
        log.info(messageDevice);
        final String messageUser = "User: " + Build.USER;
        log.info(messageUser);
        final String messageType = "Type: " + Build.TYPE;
        log.info(messageType);
        final String messageTags = "Tags: " + Build.TAGS;
        log.info(messageTags);
        final String messageHost = "Host: " + Build.HOST;
        log.info(messageHost);
        final String messageFingerPrint = "Fingerprint: " + Build.FINGERPRINT;
        log.info(messageFingerPrint);
        final String messageDisplay = "Display: " + Build.DISPLAY;
        log.info(messageDisplay);
        final String messageBrand = "Brand: " + Build.BRAND;
        log.info(messageBrand);
        final String messageModel = "Model: " + Build.MODEL;
        log.info(messageModel);
        final String messageProduct = "Product: " + Build.PRODUCT;
        log.info(messageProduct);
        log.info("---------------------------------------------------");
    }

    /**
     * A tear down method which is called last.
     */
    @AfterClass
    public static void tearDownAll() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.info(methodName);
    }

    /**
     * Tests render a scene from an invalid OBJ file.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderInvalidScene() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);
        final int scene = Scene.WRONG_FILE.ordinal();

        assertRenderScene(numCores, scene, true);
    }

    /**
     * Tests rendering a scene.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderScene() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);
        final int scene = Scene.CORNELL2.ordinal();

        assertRenderScene(numCores, scene, false);
    }

    /**
     * Tests rendering an OBJ scene in the internal storage.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneFromInternalStorageOBJ() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);
        final int scene = Scene.TEST_INTERNAL_STORAGE.ordinal();

        assertRenderScene(numCores, scene, false);
    }

    /**
     * Tests rendering an OBJ scene in the SD card.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneFromSDCardOBJ() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);
        final int scene = Scene.TEST_SD_CARD.ordinal();

        assertRenderScene(numCores, scene, false);
    }

    /**
     * Helper method that clicks the Render {@link android.widget.Button} and waits for the
     * Ray Tracing engine to render the whole scene and then checks if the resulted image in the
     * {@link Bitmap} has different values.
     *
     * @param numCores           The number of CPU cores to use in the Ray Tracing process.
     * @param scene              The desired scene to render.
     * @param expectedSameValues Whether the {@link Bitmap} should have have only one color.
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    private void assertRenderScene(final int numCores, final int scene, final boolean expectedSameValues)
        throws TimeoutException {

        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SCENE, R.id.pickerScene, scene);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_THREADS, R.id.pickerThreads, numCores);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SIZE, R.id.pickerSize, 1);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_PIXEL, R.id.pickerSamplesPixel, 1);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_LIGHT, R.id.pickerSamplesLight, 1);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_ACCELERATOR, R.id.pickerAccelerator, 3);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SHADER, R.id.pickerShader, 1);

        UtilsT.startRendering();
        if (!expectedSameValues) {
            UtilsT.assertRenderButtonText(Constants.STOP);
        }
        UtilsContextT.waitUntilRenderingDone(this.activity);

        UtilsT.assertRenderButtonText(Constants.RENDER);
        UtilsT.testStateAndBitmap(expectedSameValues);
    }

}
