package puscas.mobilertapp;

import android.graphics.Bitmap;
import android.os.Build;
import androidx.test.espresso.Espresso;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import puscas.mobilertapp.utils.Constants;
import puscas.mobilertapp.utils.ConstantsMethods;
import puscas.mobilertapp.utils.ConstantsUI;
import puscas.mobilertapp.utils.Scene;
import puscas.mobilertapp.utils.Utils;
import puscas.mobilertapp.utils.UtilsContext;
import puscas.mobilertapp.utils.UtilsContextT;
import puscas.mobilertapp.utils.UtilsPickerT;
import puscas.mobilertapp.utils.UtilsT;

/**
 * The test suite for the Ray Tracing engine used in {@link MainActivity}.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class RayTracingTest extends AbstractTest {

    /**
     * The {@link Logger} for this class.
     */
    @Nonnull
    private static final Logger LOGGER = Logger.getLogger(RayTracingTest.class.getName());

    /**
     * A setup method which is called first.
     */
    @BeforeClass
    public static void setUpAll() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        LOGGER.info("---------------------------------------------------");
        final String messageDevice = "Device: " + Build.DEVICE;
        LOGGER.info(messageDevice);
        final String messageUser = "User: " + Build.USER;
        LOGGER.info(messageUser);
        final String messageType = "Type: " + Build.TYPE;
        LOGGER.info(messageType);
        final String messageTags = "Tags: " + Build.TAGS;
        LOGGER.info(messageTags);
        final String messageHost = "Host: " + Build.HOST;
        LOGGER.info(messageHost);
        final String messageFingerPrint = "Fingerprint: " + Build.FINGERPRINT;
        LOGGER.info(messageFingerPrint);
        final String messageDisplay = "Display: " + Build.DISPLAY;
        LOGGER.info(messageDisplay);
        final String messageBrand = "Brand: " + Build.BRAND;
        LOGGER.info(messageBrand);
        final String messageModel = "Model: " + Build.MODEL;
        LOGGER.info(messageModel);
        final String messageProduct = "Product: " + Build.PRODUCT;
        LOGGER.info(messageProduct);
        LOGGER.info("---------------------------------------------------");
    }

    /**
     * A tear down method which is called last.
     */
    @AfterClass
    public static void tearDownAll() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }


    /**
     * Setup method called before each test.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();

        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Tear down method called after each test.
     */
    @After
    @Override
    public void tearDown() {
        super.tearDown();

        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Tests render a scene from an invalid OBJ file.
     *
     * @throws TimeoutException If the Ray Tracing engine didn't stop rendering the scene.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderInvalidScene() throws TimeoutException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        UtilsContextT.resetPickerValues(this.activity, Scene.WRONG_FILE.ordinal());

        UtilsT.startRendering();
        Utils.executeWithCatching(Espresso::onIdle);

        UtilsContextT.waitUntilRenderingDone(this.activity);

        UtilsT.testStateAndBitmap(true);

        final String message = methodName + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }

    /**
     * Tests rendering a scene.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderScene() throws TimeoutException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final int numCores = UtilsContext.getNumOfCores(this.activity);
        final int scene = Scene.CORNELL2.ordinal();

        assertRenderScene(numCores, scene);
    }

    /**
     * Tests rendering an OBJ scene in the internal storage.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneFromInternalStorageOBJ() throws TimeoutException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final int numCores = UtilsContext.getNumOfCores(this.activity);
        final int scene = Scene.TEST_INTERNAL_STORAGE.ordinal();

        assertRenderScene(numCores, scene);
    }

    /**
     * Tests rendering an OBJ scene in the SD card.
     */
    @Test(timeout = 2L * 60L * 1000L)
    @Ignore("In CI, the emulator can't access the SD card, even though it works via adb shell.")
    public void testRenderSceneFromSDCardOBJ() throws TimeoutException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final int numCores = UtilsContext.getNumOfCores(this.activity);
        final int scene = Scene.TEST_SD_CARD.ordinal();

        assertRenderScene(numCores, scene);
    }

    /**
     * Helper method that clicks the Render {@link android.widget.Button} and waits for the
     * Ray Tracing engine to render the whole scene and then checks if the resulted image in the
     * {@link Bitmap} has different values.
     *
     * @param numCores The number of CPU cores to use in the Ray Tracing process.
     * @param scene    The desired scene to render.
     * @throws TimeoutException If it couldn't render the whole scene in less than 2 minutes.
     */
    private void assertRenderScene(final int numCores, final int scene) throws TimeoutException {
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SCENE, R.id.pickerScene, scene);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_THREADS, R.id.pickerThreads, numCores);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SIZE, R.id.pickerSize, 1);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_PIXEL, R.id.pickerSamplesPixel, 1);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_LIGHT, R.id.pickerSamplesLight, 1);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_ACCELERATOR, R.id.pickerAccelerator, 3);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SHADER, R.id.pickerShader, 1);

        UtilsT.startRendering();
        Utils.executeWithCatching(Espresso::onIdle);
        UtilsT.assertRenderButtonText(Constants.STOP);
        Utils.executeWithCatching(Espresso::onIdle);
        UtilsContextT.waitUntilRenderingDone(this.activity);

        UtilsT.assertRenderButtonText(Constants.RENDER);
        UtilsT.testStateAndBitmap(false);
    }

}
