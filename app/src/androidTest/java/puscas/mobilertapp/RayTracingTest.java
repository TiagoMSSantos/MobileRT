package puscas.mobilertapp;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Build;
import android.widget.Button;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runners.MethodSorters;
import puscas.mobilertapp.utils.Constants;
import puscas.mobilertapp.utils.ConstantsMethods;
import puscas.mobilertapp.utils.ConstantsUI;
import puscas.mobilertapp.utils.State;
import puscas.mobilertapp.utils.UtilsContext;

/**
 * The test suite for the Ray Tracing engine used in {@link MainActivity}.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class RayTracingTest {

    /**
     * The rule for the timeout for all the tests.
     */
    @Nonnull
    @ClassRule
    public static final TestRule timeoutClassRule = new Timeout(40L, TimeUnit.MINUTES);
    /**
     * The {@link Logger} for this class.
     */
    @Nonnull
    private static final Logger LOGGER = Logger.getLogger(RayTracingTest.class.getName());
    /**
     * The rule for the timeout for each test.
     */
    @Nonnull
    @Rule
    public final TestRule timeoutRule = new Timeout(30L, TimeUnit.MINUTES);
    /**
     * The rule to create the MainActivity.
     */
    @Nonnull
    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule =
        new ActivityTestRule<>(MainActivity.class, true, true);
    /**
     * The rule to access external SD card.
     */
    @Nonnull
    @Rule
    public GrantPermissionRule grantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);

    /**
     * The MainActivity to test.
     */
    private MainActivity activity = null;

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
        final String messageUnknown = "Unknown: " + Build.UNKNOWN;
        LOGGER.info(messageUnknown);
        final String messageTime = "Time: " + Build.TIME;
        LOGGER.info(messageTime);
        final String messageTags = "Tags: " + Build.TAGS;
        LOGGER.info(messageTags);
        final String messageId = "Id: " + Build.ID;
        LOGGER.info(messageId);
        final String messageHost = "Host: " + Build.HOST;
        LOGGER.info(messageHost);
        final String messageFingerPrint = "Fingerprint: " + Build.FINGERPRINT;
        LOGGER.info(messageFingerPrint);
        final String messageDisplay = "Display: " + Build.DISPLAY;
        LOGGER.info(messageDisplay);
        final String messageBrand = "Brand: " + Build.BRAND;
        LOGGER.info(messageBrand);
        final String messageBootloader = "Bootloader: " + Build.BOOTLOADER;
        LOGGER.info(messageBootloader);
        final String messageBoard = "Board: " + Build.BOARD;
        LOGGER.info(messageBoard);
        final String messageHardware = "Hardware: " + Build.HARDWARE;
        LOGGER.info(messageHardware);
        final String messageManufacturer = "Manufacturer: " + Build.MANUFACTURER;
        LOGGER.info(messageManufacturer);
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
    public void setUp() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        this.activity = this.mainActivityActivityTestRule.getActivity();
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        this.activity.finish();
        this.mainActivityActivityTestRule.finishActivity();
        this.activity = null;
    }

    /**
     * Tests render a scene from an invalid OBJ file.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderInvalidScene() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final int numCores = UtilsContext.getNumOfCores(this.activity);

        Utils.changePickerValue(ConstantsUI.PICKER_SCENE, R.id.pickerScene, 6);
        Utils.changePickerValue(ConstantsUI.PICKER_THREADS, R.id.pickerThreads, numCores);
        Utils.changePickerValue(ConstantsUI.PICKER_SIZE, R.id.pickerSize, 8);
        Utils.changePickerValue(ConstantsUI.PICKER_SAMPLES_PIXEL, R.id.pickerSamplesPixel, 1);
        Utils.changePickerValue(ConstantsUI.PICKER_SAMPLES_LIGHT, R.id.pickerSamplesLight, 1);
        Utils.changePickerValue(ConstantsUI.PICKER_ACCELERATOR, R.id.pickerAccelerator, 3);
        Utils.changePickerValue(ConstantsUI.PICKER_SHADER, R.id.pickerShader, 2);

        LOGGER.info("GOING TO CLICK THE BUTTON.");
        final ViewInteraction viewInteraction =
            Espresso.onView(ViewMatchers.withId(R.id.renderButton))
                .check((view, exception) -> {
                    LOGGER.info("GOING TO CLICK THE BUTTON 1.");
                    final Button renderButton = view.findViewById(R.id.renderButton);
                    LOGGER.info("GOING TO CLICK THE BUTTON 2.");
                    Assertions.assertEquals(
                        Constants.RENDER,
                        renderButton.getText().toString(),
                        puscas.mobilertapp.Constants.BUTTON_MESSAGE
                    );
                    LOGGER.info("GOING TO CLICK THE BUTTON 3.");
                })
                .perform(new ViewActionButton(Constants.STOP));
        LOGGER.info("RENDERING STARTED AND STOPPED 1.");
        Espresso.onIdle();
        LOGGER.info("RENDERING STARTED AND STOPPED 2.");

        final long advanceSecs = 3L;
        final AtomicBoolean done = new AtomicBoolean(false);
        final DrawView drawView = Utils.getPrivateField(this.activity, "drawView");
        final MainRenderer renderer = drawView.getRenderer();
        LOGGER.info("RENDERING STARTED AND STOPPED 3.");
        for (long currentTimeSecs = 0L; currentTimeSecs < 120L && !done.get();
             currentTimeSecs += advanceSecs) {
            LOGGER.info("WAITING FOR RENDERING TO FINISH.");
            Uninterruptibles.sleepUninterruptibly(advanceSecs, TimeUnit.SECONDS);
            LOGGER.info("WAITING FOR RENDERING TO FINISH 2.");

            viewInteraction.check((view, exception) -> {
                final Button renderButton = view.findViewById(R.id.renderButton);
                LOGGER.info("CHECKING IF RENDERING DONE.");
                final String message = "Render button: " + renderButton.getText().toString();
                LOGGER.info(message);
                final String messageState = "State: " + renderer.getState().name();
                LOGGER.info(messageState);
                if (renderButton.getText().toString().equals(Constants.RENDER)
                    && renderer.getState() == State.IDLE) {
                    done.set(true);
                    LOGGER.info("RENDERING DONE.");
                }
            });
        }

        LOGGER.info("CHECKING RAY TRACING STATE.");
        Espresso.onView(ViewMatchers.withId(R.id.drawLayout))
            .check((view, exception) -> {
                final Bitmap bitmap = Utils.getPrivateField(renderer, "bitmap");
                Utils.assertRayTracingResultInBitmap(bitmap, true);

                Assertions.assertEquals(
                    State.IDLE,
                    renderer.getState(),
                    "State is not the expected"
                );
            });

        final String message = methodName + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }

    /**
     * Tests rendering a scene.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderScene() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final int numCores = UtilsContext.getNumOfCores(this.activity);

        Utils.changePickerValue(ConstantsUI.PICKER_SCENE, R.id.pickerScene, 2);
        Utils.changePickerValue(ConstantsUI.PICKER_THREADS, R.id.pickerThreads, numCores);
        Utils.changePickerValue(ConstantsUI.PICKER_SIZE, R.id.pickerSize, 1);
        Utils.changePickerValue(ConstantsUI.PICKER_SAMPLES_PIXEL, R.id.pickerSamplesPixel, 1);
        Utils.changePickerValue(ConstantsUI.PICKER_SAMPLES_LIGHT, R.id.pickerSamplesLight, 1);
        Utils.changePickerValue(ConstantsUI.PICKER_ACCELERATOR, R.id.pickerAccelerator, 3);
        Utils.changePickerValue(ConstantsUI.PICKER_SHADER, R.id.pickerShader, 1);

        final ViewInteraction viewInteraction =
            Espresso.onView(ViewMatchers.withId(R.id.renderButton))
                .check((view, exception) -> {
                    final Button renderButton = view.findViewById(R.id.renderButton);
                    Assertions.assertEquals(
                        Constants.RENDER,
                        renderButton.getText().toString(),
                        puscas.mobilertapp.Constants.BUTTON_MESSAGE
                    );
                })
                .perform(new ViewActionButton(Constants.STOP))
                .check((view, exception) -> {
                    final Button renderButton = view.findViewById(R.id.renderButton);
                    Assertions.assertEquals(
                        Constants.STOP,
                        renderButton.getText().toString(),
                        puscas.mobilertapp.Constants.BUTTON_MESSAGE
                    );
                });
        Espresso.onIdle();

        final long advanceSecs = 3L;
        final AtomicBoolean done = new AtomicBoolean(false);
        for (long currentTimeSecs = 0L; currentTimeSecs < 600L && !done.get();
             currentTimeSecs += advanceSecs) {
            Uninterruptibles.sleepUninterruptibly(advanceSecs, TimeUnit.SECONDS);

            viewInteraction.check((view, exception) -> {
                final Button renderButton = view.findViewById(R.id.renderButton);
                if (renderButton.getText().toString().equals(Constants.RENDER)) {
                    done.set(true);
                }
            });
        }

        viewInteraction.check((view, exception) -> {
            final Button renderButton = view.findViewById(R.id.renderButton);
            Assertions.assertEquals(
                Constants.RENDER,
                renderButton.getText().toString(),
                puscas.mobilertapp.Constants.BUTTON_MESSAGE
            );
        });

        Espresso.onView(ViewMatchers.withId(R.id.drawLayout))
            .check((view, exception) -> {
                final DrawView drawView = (DrawView) view;
                final MainRenderer renderer = drawView.getRenderer();
                final Bitmap bitmap = Utils.getPrivateField(renderer, "bitmap");
                Utils.assertRayTracingResultInBitmap(bitmap, false);

                Assertions.assertEquals(
                    State.IDLE,
                    renderer.getState(),
                    "State is not the expected"
                );
            });
    }
}
