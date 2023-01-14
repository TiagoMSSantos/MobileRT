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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.ConstantsUI;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.constants.Shader;
import puscas.mobilertapp.utils.UtilsContext;

/**
 * The test suite for the Ray Tracing engine used in {@link MainActivity}.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class RayTracingTest extends AbstractTest {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(RayTracingTest.class.getSimpleName());

    /**
     * A setup method which is called first.
     */
    @BeforeClass
    public static void setUpAll() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info(methodName);

        logger.info("---------------------------------------------------");
        final String messageDevice = "Device: " + Build.DEVICE;
        logger.info(messageDevice);
        final String messageUser = "User: " + Build.USER;
        logger.info(messageUser);
        final String messageType = "Type: " + Build.TYPE;
        logger.info(messageType);
        final String messageTags = "Tags: " + Build.TAGS;
        logger.info(messageTags);
        final String messageHost = "Host: " + Build.HOST;
        logger.info(messageHost);
        final String messageFingerPrint = "Fingerprint: " + Build.FINGERPRINT;
        logger.info(messageFingerPrint);
        final String messageDisplay = "Display: " + Build.DISPLAY;
        logger.info(messageDisplay);
        final String messageBrand = "Brand: " + Build.BRAND;
        logger.info(messageBrand);
        final String messageModel = "Model: " + Build.MODEL;
        logger.info(messageModel);
        final String messageProduct = "Product: " + Build.PRODUCT;
        logger.info(messageProduct);
        logger.info("---------------------------------------------------");
    }

    /**
     * A tear down method which is called last.
     */
    @AfterClass
    public static void tearDownAll() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info(methodName);
    }

    /**
     * Tests render a scene from an OBJ file that doesn't exist.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderInvalidScene() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        // Mock the reply as the external file manager application, to select an OBJ file that doesn't exist.
        final File fileToObj = new File("/path/to/OBJ/file/that/doesn't/exist.obj");
        final Intent resultData = new Intent(Intent.ACTION_GET_CONTENT);
        resultData.setData(Uri.fromFile(fileToObj));
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result);

        assertRenderScene(numCores, Scene.OBJ, Shader.WHITTED, Accelerator.BVH, 1, 1, true);
        Intents.intended(IntentMatchers.anyIntent());
    }

    /**
     * Tests not selecting a file when choosing an OBJ scene in the file manager.
     *
     * @throws TimeoutException If there is a timeout while waiting for the engine to become idle.
     */
    @Test(timeout = 60L * 1000L)
    public void testNotSelectingScene() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        // Mock the reply as the external file manager application, to not select anything.
        final Intent resultData = new Intent(Intent.ACTION_GET_CONTENT);
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, resultData);
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result);

        assertRenderScene(numCores, Scene.OBJ, Shader.WHITTED, Accelerator.BVH, 1, 1, true);
        Intents.intended(IntentMatchers.anyIntent());
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
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneFromInternalStorageOBJ() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        // Mock the reply as the external file manager application, to select an OBJ file.
        final Intent resultData;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            resultData = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            resultData = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        }
        resultData.addCategory(Intent.CATEGORY_OPENABLE);
        resultData.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            resultData.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        final String internalStoragePath = UtilsContext.getInternalStoragePath(this.activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final Uri objFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/teapot/teapot.obj"));
            final Uri mtlFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/teapot/teapot.mtl"));
            final Uri camFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/teapot/teapot.cam"));
            final Uri textureFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/teapot/default.png"));
            final ClipData clipData = new ClipData(new ClipDescription("Scene", new String[]{"*" + ConstantsUI.FILE_SEPARATOR + "*"}), new ClipData.Item(objFile));
            clipData.addItem(new ClipData.Item(mtlFile));
            clipData.addItem(new ClipData.Item(camFile));
            clipData.addItem(new ClipData.Item(textureFile));
            resultData.setClipData(clipData);
        } else {
            resultData.setData(Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/teapot/teapot.obj")));
        }
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result);

        assertRenderScene(numCores, Scene.OBJ, Shader.WHITTED, Accelerator.BVH, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

    /**
     * Tests rendering an OBJ scene from an OBJ file which the path was loaded with an external file
     * manager application. The OBJ is in an external SD card.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     *
     * @implNote E.g. of URLs to file:<br>
     * content://com.asus.filemanager.OpenFileProvider/file/storage/1CE6-261B/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj<br>
     * content://com.asus.filemanager.OpenFileProvider/file/mnt/sdcard/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj<br>
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneFromSDCardOBJ() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        // Mock the reply as the external file manager application, to select an OBJ file.
        final Intent resultData;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            resultData = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            resultData = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        }
        resultData.addCategory(Intent.CATEGORY_OPENABLE);
        resultData.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            resultData.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        final String sdCardPath = UtilsContext.getSdCardPath(this.activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final Uri objFile = Uri.fromFile(new File(sdCardPath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj"));
            final Uri mtlFile = Uri.fromFile(new File(sdCardPath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.mtl"));
            final Uri camFile = Uri.fromFile(new File(sdCardPath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.cam"));
            final ClipData clipData = new ClipData(new ClipDescription("Scene", new String[]{"*" + ConstantsUI.FILE_SEPARATOR + "*"}), new ClipData.Item(objFile));
            clipData.addItem(new ClipData.Item(mtlFile));
            clipData.addItem(new ClipData.Item(camFile));
            resultData.setClipData(clipData);
        } else {
            resultData.setData(Uri.fromFile(new File(sdCardPath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj")));
        }
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result);

        assertRenderScene(numCores, Scene.OBJ, Shader.WHITTED, Accelerator.BVH, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

    /**
     * Tests rendering a scene without any {@link Accelerator}.
     * It shouldn't render anything and be just a black image.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Ignore("It's failing in CI for Linux machines")
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithoutAccelerator() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        assertRenderScene(numCores, Scene.CORNELL, Shader.WHITTED, Accelerator.NONE, 1, 1, true);
    }

}
