package puscas.mobilertapp;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.concurrent.TimeoutException;

import lombok.extern.java.Log;
import puscas.mobilertapp.constants.Constants;
import puscas.mobilertapp.constants.ConstantsUI;
import puscas.mobilertapp.constants.Scene;
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
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderScene() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);
        final int scene = Scene.CORNELL2.ordinal();

        assertRenderScene(numCores, scene, false);
    }

    /**
     * Tests rendering an OBJ scene from an OBJ file which the path was loaded with an external file
     * manager application. The OBJ is in an internal storage.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     *
     * @implNote E.g. of an URL to file:<br>
     * file:///file/data/local/tmp/MobileRT/WavefrontOBJs/teapot/teapot.obj
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneFromInternalStorageOBJ() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);
        final int scene = Scene.OBJ.ordinal();

        // Mock the reply for the external file manager application.
        final String internalStoragePath = UtilsContext.getInternalStoragePath(this.activity);
        final File fileToObj = new File("/file" + internalStoragePath + "/MobileRT/WavefrontOBJs/teapot/teapot.obj");
        final Intent resultData = new Intent(Intent.ACTION_GET_CONTENT);
        resultData.setData(Uri.fromFile(fileToObj));
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        Intents.init();
        Intents.intending(IntentMatchers.anyIntent())
            .respondWith(result);

        assertRenderScene(numCores, scene, false);
        Intents.intended(IntentMatchers.anyIntent());

        Intents.release();
    }

    /**
     * Tests rendering an OBJ scene from an OBJ file which the path was loaded with an external file
     * manager application. The OBJ is in an external SD card.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     *
     * @implNote E.g. of URLs to file:<br>
     * content://com.asus.filemanager.OpenFileProvider/file/storage/1CE6-261B/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj
     * content://com.asus.filemanager.OpenFileProvider/file/mnt/sdcard/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneFromSDCardOBJ() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);
        final int scene = Scene.OBJ.ordinal();

        // Mock the reply for the external file manager application.
        final String sdCardPath = UtilsContext.getSdCardPath(this.activity);
        final File fileToObj = new File("/file" + sdCardPath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj");
        final Intent resultData = new Intent(Intent.ACTION_GET_CONTENT);
        resultData.setData(Uri.fromFile(fileToObj));
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        Intents.init();
        Intents.intending(IntentMatchers.anyIntent())
            .respondWith(result);

        assertRenderScene(numCores, scene, false);
        Intents.intended(IntentMatchers.anyIntent());

        Intents.release();
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
    private void assertRenderScene(final int numCores,
                                   final int scene,
                                   final boolean expectedSameValues) throws TimeoutException {

        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SCENE, R.id.pickerScene, scene);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_THREADS, R.id.pickerThreads, numCores);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SIZE, R.id.pickerSize, 1);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_PIXEL, R.id.pickerSamplesPixel, 1);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_LIGHT, R.id.pickerSamplesLight, 1);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_ACCELERATOR, R.id.pickerAccelerator, 3);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SHADER, R.id.pickerShader, 1);

        UtilsT.startRendering();
        UtilsContextT.waitUntilRenderingDone(this.activity);

        UtilsT.assertRenderButtonText(Constants.RENDER);
        UtilsT.testStateAndBitmap(expectedSameValues);
        UtilsT.executeWithCatching(Espresso::onIdle);
    }

}
