package puscas.mobilertapp;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Uninterruptibles;

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

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import java8.util.J8Arrays;
import java8.util.stream.IntStreams;
import java8.util.stream.StreamSupport;
import puscas.mobilertapp.utils.Accelerator;
import puscas.mobilertapp.utils.Constants;
import puscas.mobilertapp.utils.ConstantsUI;
import puscas.mobilertapp.utils.Scene;
import puscas.mobilertapp.utils.Shader;
import puscas.mobilertapp.utils.State;
import puscas.mobilertapp.utils.UtilsContext;

import static puscas.mobilertapp.utils.ConstantsMethods.FINISHED;

/**
 * The test suite for {@link MainActivity}.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class MainActivityTest {

    /**
     * The {@link Logger} for this class.
     */
    @Nonnull
    private static final Logger LOGGER = Logger.getLogger(MainActivityTest.class.getName());

    /**
     * The current index of the scene in the {@link NumberPicker}.
     */
    private int counterScene = 0;

    /**
     * The current index of the accelerator in the {@link NumberPicker}.
     */
    private int counterAccelerator = 0;

    /**
     * The current index of the shader in the {@link NumberPicker}.
     */
    private int counterShader = 0;

    /**
     * The current index of the resolution in the {@link NumberPicker}.
     */
    private int counterResolution = 0;

    /**
     * The current index of the number of samples per light in the {@link NumberPicker}.
     */
    private int counterSPL = 0;

    /**
     * The current index of the number of samples per pixel in the {@link NumberPicker}.
     */
    private int counterSPP = 0;

    /**
     * The current index of the number of threads in the {@link NumberPicker}.
     */
    private int counterThreads = 0;

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
    public GrantPermissionRule grantPermissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);

    /**
     * The rule for the timeout for each test.
     */
    @Nonnull
    @Rule
    public final TestRule timeoutRule = new Timeout(30L, TimeUnit.MINUTES);

    /**
     * The rule for the timeout for all the tests.
     */
    @Nonnull
    @ClassRule
    public static final TestRule timeoutClassRule = new Timeout(40L, TimeUnit.MINUTES);

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
        LOGGER.info("Device: " + Build.DEVICE);
        LOGGER.info("User: " + Build.USER);
        LOGGER.info("Type: " + Build.TYPE);
        LOGGER.info("Unknown: " + Build.UNKNOWN);
        LOGGER.info("Time: " + Build.TIME);
        LOGGER.info("Tags: " + Build.TAGS);
        LOGGER.info("Id: " + Build.ID);
        LOGGER.info("Host: " + Build.HOST);
        LOGGER.info("Fingerprint: " + Build.FINGERPRINT);
        LOGGER.info("Display: " + Build.DISPLAY);
        LOGGER.info("Brand: " + Build.BRAND);
        LOGGER.info("Bootloader: " + Build.BOOTLOADER);
        LOGGER.info("Board: " + Build.BOARD);
        LOGGER.info("Hardware: " + Build.HARDWARE);
        LOGGER.info("Manufacturer: " + Build.MANUFACTURER);
        LOGGER.info("Model: " + Build.MODEL);
        LOGGER.info("Product: " + Build.PRODUCT);
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
     * Tests that a file in the Android device exists and is readable.
     */
    @Test(timeout = 5L * 1000L)
    public void testFilesExistAndReadable() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final List<String> paths = ImmutableList.<String>builder().add(
            Constants.OBJ_FILE_TEAPOT
        ).build();
        StreamSupport.stream(paths)
            .forEach(path -> {
                final File file = new File(path);
                final String filePath = file.getAbsolutePath();
                Assertions.assertTrue(file.exists(), Constants.FILE_SHOULD_EXIST + ": " + filePath);
                Assertions.assertTrue(file.canRead(), "File should be readable: " + filePath);
            });
    }

    /**
     * Tests that a file does not exist in the Android device.
     */
    @Test(timeout = 5L * 1000L)
    public void testFilesNotExist() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final String sdCardPath = UtilsContext.getSDCardPath(this.activity);

        final List<String> paths = ImmutableList.<String>builder().add(
            Constants.EMPTY_FILE,
            sdCardPath + Constants.OBJ_FILE_NOT_EXISTS
        ).build();
        StreamSupport.stream(paths)
            .forEach(path -> {
                final File file = new File(path);
                Assertions.assertFalse(file.exists(), "File should not exist!");
                Assertions.assertFalse(file.canRead(), "File should not be readable!");
            });
    }

    /**
     * Tests changing all the {@link NumberPicker} and clicking the render
     * {@link Button} few times.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testUI() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        Espresso.onView(ViewMatchers.withId(R.id.renderButton))
            .check((view, exception) -> {
                final Button button = view.findViewById(R.id.renderButton);
                Assertions.assertEquals(Constants.RENDER, button.getText().toString(), "Button message");
            });

        final int numCores = UtilsContext.getNumOfCores(this.activity);
        assertClickRenderButton(1, numCores);
        assertPickerNumbers(numCores);
        clickPreviewCheckBox(false);
    }

    /**
     * Tests clicking the render {@link Button} many times without preview.
     */
    @Test(timeout = 20L * 60L * 1000L)
    public void testClickRenderButtonManyTimesWithoutPreview() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        clickPreviewCheckBox(false);

        final int numCores = UtilsContext.getNumOfCores(this.activity);
        assertClickRenderButton(5, numCores);
    }

    /**
     * Tests clicking the render {@link Button} many times with preview.
     */
    @Test(timeout = 30L * 60L * 1000L)
    public void testClickRenderButtonManyTimesWithPreview() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        clickPreviewCheckBox(false);
        clickPreviewCheckBox(true);

        final int numCores = UtilsContext.getNumOfCores(this.activity);
        assertClickRenderButton(5, numCores);
    }

    /**
     * Tests the preview feature in a scene.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testPreviewScene() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final int numCores = UtilsContext.getNumOfCores(this.activity);

        changePickerValue("pickerScene", R.id.pickerScene, 2);
        changePickerValue("pickerThreads", R.id.pickerThreads, numCores);
        changePickerValue("pickerSize", R.id.pickerSize, 8);
        changePickerValue("pickerSamplesPixel", R.id.pickerSamplesPixel, 1);
        changePickerValue("pickerSamplesLight", R.id.pickerSamplesLight, 1);
        changePickerValue("pickerAccelerator", R.id.pickerAccelerator, 3);
        changePickerValue("pickerShader", R.id.pickerShader, 2);

        LOGGER.info("GOING TO CLICK THE BUTTON.");
        final ViewInteraction viewInteraction = Espresso.onView(ViewMatchers.withId(R.id.renderButton))
            .check((view, exception) -> {
                LOGGER.info("GOING TO CLICK THE BUTTON 1.");
                final Button renderButton = view.findViewById(R.id.renderButton);
                LOGGER.info("GOING TO CLICK THE BUTTON 2.");
                Assertions.assertEquals(
                    Constants.RENDER,
                    renderButton.getText().toString(),
                    "Button message"
                );
                LOGGER.info("GOING TO CLICK THE BUTTON 3.");
            })
            .perform(new ViewActionButton(Constants.STOP))
            .check((view, exception) -> {
                LOGGER.info("GOING TO CLICK THE BUTTON 4.");
                final Button renderButton = view.findViewById(R.id.renderButton);
                LOGGER.info("GOING TO CLICK THE BUTTON 5.");
                Assertions.assertEquals(
                    Constants.STOP,
                    renderButton.getText().toString(),
                    "Button message"
                );
                LOGGER.info("GOING TO CLICK THE BUTTON 6.");
            })
            .perform(new ViewActionButton(Constants.RENDER));
        LOGGER.info("RENDERING STARTED AND STOPPED 1.");
        Espresso.onIdle();
        LOGGER.info("RENDERING STARTED AND STOPPED 2.");

        final long advanceSecs = 3L;
        final AtomicBoolean done = new AtomicBoolean(false);
        final DrawView drawView = Utils.getPrivateField(this.activity, "drawView");
        final MainRenderer renderer = drawView.getRenderer();
        LOGGER.info("RENDERING STARTED AND STOPPED 3.");
        for (long currentTimeSecs = 0L; currentTimeSecs < 120L && !done.get(); currentTimeSecs += advanceSecs) {
            LOGGER.info("WAITING FOR RENDERING TO FINISH.");
            Uninterruptibles.sleepUninterruptibly(advanceSecs, TimeUnit.SECONDS);
            LOGGER.info("WAITING FOR RENDERING TO FINISH 2.");

            viewInteraction.check((view, exception) -> {
                final Button renderButton = view.findViewById(R.id.renderButton);
                LOGGER.info("CHECKING IF RENDERING DONE.");
                LOGGER.info("Render button: " + renderButton.getText().toString());
                LOGGER.info("State: " + renderer.getState().name());
                if (renderButton.getText().toString().equals(Constants.RENDER)
                    && renderer.getState() == State.IDLE) {
                    done.set(true);
                    LOGGER.info("RENDERING DONE.");
                }
            });
        }

        viewInteraction.check((view, exception) -> {
            final Button renderButton = view.findViewById(R.id.renderButton);
            LOGGER.info("CHECKING RENDERING BUTTON.");
            Assertions.assertEquals(
                Constants.RENDER,
                renderButton.getText().toString(),
                "Button message"
            );
        });

        LOGGER.info("CHECKING RAY TRACING STATE.");
        Espresso.onView(ViewMatchers.withId(R.id.drawLayout))
            .check((view, exception) -> {
                final Bitmap bitmap = Utils.getPrivateField(renderer, "bitmap");
                assertRayTracingResultInBitmap(bitmap, false);

                Assertions.assertEquals(
                    State.IDLE,
                    renderer.getState(),
                    "State is not the expected"
                );
            });

        LOGGER.info(methodName + FINISHED);
    }

    /**
     * Tests render a scene from an invalid OBJ file.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderInvalidScene() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final int numCores = UtilsContext.getNumOfCores(this.activity);

        changePickerValue("pickerScene", R.id.pickerScene, 6);
        changePickerValue("pickerThreads", R.id.pickerThreads, numCores);
        changePickerValue("pickerSize", R.id.pickerSize, 8);
        changePickerValue("pickerSamplesPixel", R.id.pickerSamplesPixel, 1);
        changePickerValue("pickerSamplesLight", R.id.pickerSamplesLight, 1);
        changePickerValue("pickerAccelerator", R.id.pickerAccelerator, 3);
        changePickerValue("pickerShader", R.id.pickerShader, 2);

        LOGGER.info("GOING TO CLICK THE BUTTON.");
        final ViewInteraction viewInteraction = Espresso.onView(ViewMatchers.withId(R.id.renderButton))
            .check((view, exception) -> {
                LOGGER.info("GOING TO CLICK THE BUTTON 1.");
                final Button renderButton = view.findViewById(R.id.renderButton);
                LOGGER.info("GOING TO CLICK THE BUTTON 2.");
                Assertions.assertEquals(
                    Constants.RENDER,
                    renderButton.getText().toString(),
                    "Button message"
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
        for (long currentTimeSecs = 0L; currentTimeSecs < 120L && !done.get(); currentTimeSecs += advanceSecs) {
            LOGGER.info("WAITING FOR RENDERING TO FINISH.");
            Uninterruptibles.sleepUninterruptibly(advanceSecs, TimeUnit.SECONDS);
            LOGGER.info("WAITING FOR RENDERING TO FINISH 2.");

            viewInteraction.check((view, exception) -> {
                final Button renderButton = view.findViewById(R.id.renderButton);
                LOGGER.info("CHECKING IF RENDERING DONE.");
                LOGGER.info("Render button: " + renderButton.getText().toString());
                LOGGER.info("State: " + renderer.getState().name());
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
                assertRayTracingResultInBitmap(bitmap, true);

                Assertions.assertEquals(
                    State.IDLE,
                    renderer.getState(),
                    "State is not the expected"
                );
            });

        LOGGER.info(methodName + FINISHED);
    }

    /**
     * Tests rendering a scene.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderScene() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final int numCores = UtilsContext.getNumOfCores(this.activity);

        changePickerValue("pickerScene", R.id.pickerScene, 2);
        changePickerValue("pickerThreads", R.id.pickerThreads, numCores);
        changePickerValue("pickerSize", R.id.pickerSize, 1);
        changePickerValue("pickerSamplesPixel", R.id.pickerSamplesPixel, 1);
        changePickerValue("pickerSamplesLight", R.id.pickerSamplesLight, 1);
        changePickerValue("pickerAccelerator", R.id.pickerAccelerator, 3);
        changePickerValue("pickerShader", R.id.pickerShader, 1);

        final ViewInteraction viewInteraction = Espresso.onView(ViewMatchers.withId(R.id.renderButton))
        .check((view, exception) -> {
            final Button renderButton = view.findViewById(R.id.renderButton);
            Assertions.assertEquals(
                Constants.RENDER,
                renderButton.getText().toString(),
                "Button message"
            );
        })
        .perform(new ViewActionButton(Constants.STOP))
        .check((view, exception) -> {
            final Button renderButton = view.findViewById(R.id.renderButton);
            Assertions.assertEquals(
                Constants.STOP,
                renderButton.getText().toString(),
                "Button message"
            );
        });
        Espresso.onIdle();

        final long advanceSecs = 3L;
        final AtomicBoolean done = new AtomicBoolean(false);
        for (long currentTimeSecs = 0L; currentTimeSecs < 600L && !done.get(); currentTimeSecs += advanceSecs) {
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
                "Button message"
            );
        });

        Espresso.onView(ViewMatchers.withId(R.id.drawLayout))
            .check((view, exception) -> {
                final DrawView drawView = (DrawView) view;
                final MainRenderer renderer = drawView.getRenderer();
                final Bitmap bitmap = Utils.getPrivateField(renderer, "bitmap");
                assertRayTracingResultInBitmap(bitmap, false);

                Assertions.assertEquals(
                    State.IDLE,
                    renderer.getState(),
                    "State is not the expected"
                );
            });
    }

    /**
     * Helper method which tests clicking the render {@link Button}.
     *
     * @param repetitions The number of repetitions.
     * @param numCores    The number of CPU cores in the system.
     */
    private void assertClickRenderButton(final int repetitions, final int numCores) {
        changePickerValue("pickerSamplesPixel", R.id.pickerSamplesPixel, 99);
        changePickerValue("pickerScene", R.id.pickerScene, 5);
        changePickerValue("pickerThreads", R.id.pickerThreads, 1);
        changePickerValue("pickerSize", R.id.pickerSize, 8);
        changePickerValue("pickerSamplesLight", R.id.pickerSamplesLight, 1);
        changePickerValue("pickerAccelerator", R.id.pickerAccelerator, 2);
        changePickerValue("pickerShader", R.id.pickerShader, 2);

        final int numScenes = Scene.values().length;
        final int numAccelerators = Accelerator.values().length;
        final int numShaders = Shader.values().length;
        final int numSPP = 99;
        final int numSPL = 100;
        final int numRes = 9;

        final List<String> buttonTextList = ImmutableList.<String>builder().add(Constants.STOP, Constants.RENDER).build();
        IntStreams.range(0, buttonTextList.size() * repetitions).forEach(currentIndex -> {
            LOGGER.info("currentIndex = " + currentIndex);
            final int finalCounterScene =  Math.min(this.counterScene % numScenes, 3);
            this.counterScene++;
            final int finalCounterAccelerator =  Math.max(this.counterAccelerator % numAccelerators, 1);
            this.counterAccelerator++;
            final int finalCounterShader = Math.max(this.counterShader % numShaders, 0);
            this.counterShader++;
            final int finalCounterSPP = Math.max(this.counterSPP % numSPP, 90);
            this.counterSPP++;
            final int finalCounterSPL = Math.max(this.counterSPL % numSPL, 1);
            this.counterSPL++;
            final int finalCounterResolution = Math.max(this.counterResolution % numRes, 7);
            this.counterResolution++;
            final int finalCounterThreads = Math.max(this.counterThreads % numCores, 1);
            this.counterThreads++;
            changePickerValue("pickerScene", R.id.pickerScene, finalCounterScene);
            changePickerValue("pickerAccelerator", R.id.pickerAccelerator, finalCounterAccelerator);
            changePickerValue("pickerShader", R.id.pickerShader, finalCounterShader);
            changePickerValue("pickerSamplesPixel", R.id.pickerSamplesPixel, finalCounterSPP);
            changePickerValue("pickerSamplesLight", R.id.pickerSamplesLight, finalCounterSPL);
            changePickerValue("pickerSize", R.id.pickerSize, finalCounterResolution);
            changePickerValue("pickerThreads", R.id.pickerThreads, finalCounterThreads);


            final int expectedIndexOld = currentIndex > 0?
                (currentIndex - 1) % buttonTextList.size() : 1;
            final String expectedButtonTextOld = buttonTextList.get(expectedIndexOld);
            final ViewInteraction viewInteraction = Espresso.onView(ViewMatchers.withId(R.id.renderButton));
            final int expectedIndex = currentIndex % buttonTextList.size();
            final String expectedButtonText = buttonTextList.get(expectedIndex);
            viewInteraction.check((view, exception) -> {
                final Button renderButton = view.findViewById(R.id.renderButton);
                Assertions.assertEquals(
                    expectedButtonTextOld,
                    renderButton.getText().toString(),
                    "Button message at currentIndex: " + currentIndex
                );
            })
                .perform(new ViewActionButton(expectedButtonText))
                .check((view, exception) -> {
                    final Button renderButton = view.findViewById(R.id.renderButton);
                    Assertions.assertEquals(
                        expectedButtonText,
                        renderButton.getText().toString(),
                        "Button message at currentIndex: " + currentIndex + "(" + expectedIndex + ")"
                            + ConstantsUI.LINE_SEPARATOR
                            + "finalCounterScene: " + finalCounterScene
                            + ConstantsUI.LINE_SEPARATOR
                            + "finalCounterAccelerator: " + finalCounterAccelerator
                            + ConstantsUI.LINE_SEPARATOR
                            + "finalCounterShader: " + finalCounterShader
                            + ConstantsUI.LINE_SEPARATOR
                            + "finalCounterSPP: " + finalCounterSPP
                            + ConstantsUI.LINE_SEPARATOR
                            + "finalCounterSPL: " + finalCounterSPL
                            + ConstantsUI.LINE_SEPARATOR
                            + "finalCounterResolution: " + finalCounterResolution
                            + ConstantsUI.LINE_SEPARATOR
                            + "finalCounterThreads: " + finalCounterThreads
                            + ConstantsUI.LINE_SEPARATOR
                    );
                });
        });
    }

    /**
     * Helper method which tests the range of the {@link NumberPicker} in the UI.
     *
     * @param numCores The number of CPU cores in the system.
     */
    private static void assertPickerNumbers(final int numCores) {
        IntStreams.rangeClosed(0, 2).forEach(value ->
            changePickerValue("pickerAccelerator", R.id.pickerAccelerator, value)
        );
        IntStreams.rangeClosed(1, 100).forEach(value ->
            changePickerValue("pickerSamplesLight", R.id.pickerSamplesLight, value)
        );
        IntStreams.rangeClosed(0, 6).forEach(value ->
            changePickerValue("pickerScene", R.id.pickerScene, value)
        );
        IntStreams.rangeClosed(0, 4).forEach(value ->
            changePickerValue("pickerShader", R.id.pickerShader, value)
        );
        IntStreams.rangeClosed(1, numCores).forEach(value ->
            changePickerValue("pickerThreads", R.id.pickerThreads, value)
        );
        IntStreams.rangeClosed(1, 99).forEach(value ->
            changePickerValue("pickerSamplesPixel", R.id.pickerSamplesPixel, value)
        );
        IntStreams.rangeClosed(1, 8).forEach(value ->
            changePickerValue("pickerSize", R.id.pickerSize, value)
        );
    }

    /**
     * Helper method which tests clicking the preview {@link CheckBox}.
     *
     * @param expectedValue The expected value for the {@link CheckBox}.
     */
    private static void clickPreviewCheckBox(final boolean expectedValue) {
        Espresso.onView(ViewMatchers.withId(R.id.preview))
            .check((view, exception) ->
                assertCheckBox(view, R.id.preview, Constants.PREVIEW, Constants.CHECK_BOX_MESSAGE, !expectedValue)
            )
            .perform(ViewActions.click())
            .check((view, exception) ->
                assertCheckBox(view, R.id.preview, Constants.PREVIEW, Constants.CHECK_BOX_MESSAGE, expectedValue)
            );
    }

    /**
     * Asserts the {@link CheckBox} expected value.
     *
     * @param view                The {@link View}.
     * @param id                  The id of the {@link CheckBox}.
     * @param expectedDescription The expected description of the {@link CheckBox}.
     * @param checkBoxMessage     The message.
     * @param expectedValue       The expected value for the {@link CheckBox}.
     */
    private static void assertCheckBox(@Nonnull final View view,
                                       final int id,
                                       final String expectedDescription,
                                       final String checkBoxMessage,
                                       final boolean expectedValue) {
        final CheckBox checkbox = view.findViewById(id);
        Assertions.assertEquals(expectedDescription, checkbox.getText().toString(), checkBoxMessage);
        Assertions.assertEquals(expectedValue, checkbox.isChecked(), "Check box has not the expected value");
    }

    /**
     * Helper method which changes the {@code value} of a {@link NumberPicker}.
     *
     * @param pickerName    The name of the {@link NumberPicker}.
     * @param pickerId      The identifier of the {@link NumberPicker}.
     * @param expectedValue The new expectedValue for the {@link NumberPicker}.
     */
    private static void changePickerValue(final String pickerName,
                                          final int pickerId,
                                          final int expectedValue) {
        Espresso.onView(ViewMatchers.withId(pickerId))
            .perform(new ViewActionNumberPicker(expectedValue))
            .check((view, exception) -> {
                final NumberPicker numberPicker = view.findViewById(pickerId);
                Assertions.assertEquals(expectedValue, numberPicker.getValue(),
                    "Number picker '" + pickerName + "' with wrong value"
                );
            });
    }

    /**
     * Helper method that checks if a {@link Bitmap} contains valid values from
     * a rendered image.
     *
     * @param bitmap The {@link Bitmap}.
     * @param expectedSameValues Whether the {@link Bitmap} should have have only
     *                           one color.
     */
    private static void assertRayTracingResultInBitmap(@Nonnull final Bitmap bitmap, final boolean expectedSameValues) {
        final int firstPixel = bitmap.getPixel(0, 0);
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        final boolean bitmapSameColor = J8Arrays.stream(pixels)
            .allMatch(pixel -> pixel == firstPixel);

        LOGGER.info("CHECKING BITMAP VALUES.");
        Assertions.assertEquals(expectedSameValues, bitmapSameColor,
            "The rendered image should have different values."
        );
    }
}
