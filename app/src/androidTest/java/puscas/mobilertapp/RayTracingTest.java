package puscas.mobilertapp;

import android.os.Build;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import puscas.mobilertapp.utils.Constants;
import puscas.mobilertapp.utils.ConstantsMethods;
import puscas.mobilertapp.utils.ConstantsUI;
import puscas.mobilertapp.utils.UtilsContext;
import puscas.mobilertapp.utils.UtilsContextTest;
import puscas.mobilertapp.utils.UtilsPickerTest;
import puscas.mobilertapp.utils.UtilsTest;

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
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderInvalidScene() throws TimeoutException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        UtilsContextTest.resetPickerValues(this.activity, 6);

        UtilsTest.startRendering();
        Espresso.onIdle();

        UtilsContextTest.waitUntilRenderingDone(this.activity);

        UtilsTest.testStateAndBitmap(true);

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

        UtilsPickerTest.changePickerValue(ConstantsUI.PICKER_SCENE, R.id.pickerScene, 2);
        UtilsPickerTest.changePickerValue(ConstantsUI.PICKER_THREADS, R.id.pickerThreads, numCores);
        UtilsPickerTest.changePickerValue(ConstantsUI.PICKER_SIZE, R.id.pickerSize, 1);
        UtilsPickerTest
            .changePickerValue(ConstantsUI.PICKER_SAMPLES_PIXEL, R.id.pickerSamplesPixel, 1);
        UtilsPickerTest
            .changePickerValue(ConstantsUI.PICKER_SAMPLES_LIGHT, R.id.pickerSamplesLight, 1);
        UtilsPickerTest
            .changePickerValue(ConstantsUI.PICKER_ACCELERATOR, R.id.pickerAccelerator, 3);
        UtilsPickerTest.changePickerValue(ConstantsUI.PICKER_SHADER, R.id.pickerShader, 1);

        UtilsTest.startRendering();
        UtilsTest.assertRenderButtonText(Constants.STOP);
        Espresso.onIdle();

        UtilsContextTest.waitUntilRenderingDone(this.activity);
        UtilsTest.assertRenderButtonText(Constants.RENDER);

        UtilsTest.testStateAndBitmap(false);
    }

}
