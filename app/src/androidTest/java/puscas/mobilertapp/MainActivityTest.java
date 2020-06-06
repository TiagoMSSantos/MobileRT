package puscas.mobilertapp;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.core.internal.deps.guava.collect.ImmutableList;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.FlakyTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.google.common.util.concurrent.Uninterruptibles;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.Contract;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import java8.util.stream.IntStreams;
import java8.util.stream.StreamSupport;
import puscas.mobilertapp.utils.Accelerator;
import puscas.mobilertapp.utils.Constants;
import puscas.mobilertapp.utils.ConstantsUI;
import puscas.mobilertapp.utils.Scene;
import puscas.mobilertapp.utils.Shader;
import puscas.mobilertapp.utils.State;

/**
 * The test suite for {@link MainActivity}.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class MainActivityTest {

    /**
     * The {@link Logger} for this class.
     */
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
     * A setup method which is called first.
     */
    @BeforeClass
    public static void setUpAll() {
        LOGGER.info(Constants.SET_UP_ALL);

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
        LOGGER.info(Constants.TEAR_DOWN_ALL);
    }

    /**
     * Helper method which tests the range of the {@link NumberPicker} in the UI.
     *
     * @param numCores The number of CPU cores in the system.
     */
    private static void testPickerNumbers(final int numCores) {
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
        IntStreams.rangeClosed(1, 10).forEach(value ->
            changePickerValue("pickerSamplesPixel", R.id.pickerSamplesPixel, value)
        );
        IntStreams.rangeClosed(1, 8).forEach(value ->
            changePickerValue("pickerSize", R.id.pickerSize, value)
        );
    }

    /**
     * Helper method which tests clicking the render {@link Button}.
     *
     * @param repetitions The number of repetitions.
     * @param numCores    The number of CPU cores in the system.
     */
    private void testRenderButton(final int repetitions, final int numCores) {
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
            .perform(new MainActivityTest.ViewActionButton(expectedButtonText))
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
//            Uninterruptibles.sleepUninterruptibly(3000L, TimeUnit.MILLISECONDS);
        });
    }

    /**
     * Helper method which tests clicking the preview {@link CheckBox}.
     */
    private static void testPreviewCheckBox() {
        final ViewInteraction viewInteraction = Espresso.onView(ViewMatchers.withId(R.id.preview));
        viewInteraction.check((view, exception) ->
            assertCheckBox(view, R.id.preview, Constants.PREVIEW, Constants.CHECK_BOX_MESSAGE, true)
        );
        viewInteraction.perform(new MainActivityTest.ViewActionCheckBox());
        viewInteraction.check((view, exception) ->
            assertCheckBox(view, R.id.preview, Constants.PREVIEW, Constants.CHECK_BOX_MESSAGE, false)
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
    private static void assertCheckBox(
            @Nonnull final View view,
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
            .perform(new MainActivityTest.ViewActionNumberPicker(expectedValue))
            .check((view, exception) -> {
                final NumberPicker numberPicker = view.findViewById(pickerId);
                Assertions.assertEquals(expectedValue, numberPicker.getValue(),
                    "Number picker '" + pickerName + "' with wrong value"
                );
            });
    }

    /**
     * Setup method called before each test.
     */
    @Before
    public void setUp() {
        LOGGER.info(Constants.SET_UP);
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        LOGGER.info(Constants.TEAR_DOWN);
    }

    /**
     * Tests that a file in the Android device exists and is readable.
     */
    @Test(timeout = 5L * 1000L)
    public void testFilesExistAndReadable() {
        LOGGER.info("testFilesExistAndReadable");

        final MainActivity activity = this.mainActivityActivityTestRule.getActivity();

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

        this.mainActivityActivityTestRule.finishActivity();
    }

    /**
     * Tests that a file does not exist in the Android device.
     */
    @Test(timeout = 5L * 1000L)
    public void testFilesNotExist() {
        LOGGER.info("testFilesNotExist");

        final MainActivity activity = this.mainActivityActivityTestRule.getActivity();

        final List<String> paths = ImmutableList.<String>builder().add(
            Constants.EMPTY_FILE,
            activity.getSDCardPath() + Constants.OBJ_FILE_NOT_EXISTS
        ).build();
        StreamSupport.stream(paths)
            .forEach(path -> {
                final File file = new File(path);
                Assertions.assertFalse(file.exists(), "File should not exist!");
                Assertions.assertFalse(file.canRead(), "File should not be readable!");
            });

        this.mainActivityActivityTestRule.finishActivity();
    }

    /**
     * Tests changing all the {@link NumberPicker} and clicking the render
     * {@link Button} few times.
     */
    @Test(timeout = 60L * 1000L)
    public void testUI() {
        LOGGER.info("testUI");

        final MainActivity activity = this.mainActivityActivityTestRule.getActivity();

        Espresso.onView(ViewMatchers.withId(R.id.renderButton))
            .check((view, exception) -> {
                final Button button = view.findViewById(R.id.renderButton);
                Assertions.assertEquals(Constants.RENDER, button.getText().toString(), "Button message");
            });

        final int numCores = activity.getNumOfCores();
        testRenderButton(1, numCores);
        testPickerNumbers(numCores);
        testPreviewCheckBox();

        this.mainActivityActivityTestRule.finishActivity();
    }

    /**
     * Tests clicking the render {@link Button} many times without preview.
     */
    @FlakyTest(detail = "Race condition in the system.")
    @Test(timeout = 20L * 60L * 1000L)
    public void testRenderManyTimesWithoutPreview() {
        LOGGER.info("testRenderManyTimesWithoutPreview");

        final MainActivity activity = this.mainActivityActivityTestRule.getActivity();
        final int numCores = activity.getNumOfCores();

        Espresso.onView(ViewMatchers.withId(R.id.preview))
            .check((view, exception) ->
                assertCheckBox(view, R.id.preview, Constants.PREVIEW, Constants.CHECK_BOX_MESSAGE, true)
            )
            .perform(new MainActivityTest.ViewActionCheckBox())
            .check((view, exception) ->
                assertCheckBox(view, R.id.preview, Constants.PREVIEW, Constants.CHECK_BOX_MESSAGE, false)
            );

        checksIfSystemShouldContinue(numCores);
        testRenderButton(40, numCores);

        this.mainActivityActivityTestRule.finishActivity();
    }

    /**
     * Tests clicking the render {@link Button} many times with preview.
     */
    @FlakyTest(detail = "Race condition in the system.")
    @Test(timeout = 20L * 60L * 1000L)
    public void testRenderManyTimesWithPreview() {
        LOGGER.info("testRenderManyTimesWithPreview");

        final MainActivity activity = this.mainActivityActivityTestRule.getActivity();
        final int numCores = activity.getNumOfCores();

        Espresso.onView(ViewMatchers.withId(R.id.preview))
            .check((view, exception) ->
                assertCheckBox(view, R.id.preview, Constants.PREVIEW, Constants.CHECK_BOX_MESSAGE, true)
            );

        checksIfSystemShouldContinue(numCores);
        testRenderButton(40, numCores);

        this.mainActivityActivityTestRule.finishActivity();
    }

    /**
     * Tests rendering a scene.
     */
    @Test(timeout = 10L * 60L * 1000L)
    public void testRenderScene() {
        LOGGER.info("testRenderScene");

        final MainActivity activity = this.mainActivityActivityTestRule.getActivity();
        final int numCores = activity.getNumOfCores();

        changePickerValue("pickerScene", R.id.pickerScene, 2);
        changePickerValue("pickerThreads", R.id.pickerThreads, numCores);
        changePickerValue("pickerSize", R.id.pickerSize, 8);
        changePickerValue("pickerSamplesPixel", R.id.pickerSamplesPixel, 1);
        changePickerValue("pickerSamplesLight", R.id.pickerSamplesLight, 1);
        changePickerValue("pickerAccelerator", R.id.pickerAccelerator, 3);
        changePickerValue("pickerShader", R.id.pickerShader, 2);

        final ViewInteraction viewInteraction = Espresso.onView(ViewMatchers.withId(R.id.renderButton))
        .check((view, exception) -> {
            final Button renderButton = view.findViewById(R.id.renderButton);
            Assertions.assertEquals(
                Constants.RENDER,
                renderButton.getText().toString(),
                "Button message"
            );
        })
        .perform(new MainActivityTest.ViewActionButton(Constants.STOP))
        .check((view, exception) -> {
            final Button renderButton = view.findViewById(R.id.renderButton);
            Assertions.assertEquals(
                Constants.STOP,
                renderButton.getText().toString(),
                "Button message"
            );
        });

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

                // Get bitmap
                Field field = null;
                try {
                    field = renderer.getClass().getDeclaredField("bitmap");
                } catch (final NoSuchFieldException ex) {
                    LOGGER.warning(ex.getMessage());
                }
                if (field != null) {
                    field.setAccessible(true);

                    try {
                        final Bitmap bitmap = (Bitmap) field.get(renderer);
                        final int pixel = bitmap.getPixel(0, 0);

                        // Check value of 1st pixel
                        Assertions.assertNotSame(Color.BLACK, pixel, "Pixel color shouldn't be black.");
                    } catch (final IllegalAccessException ex) {
                        LOGGER.warning(ex.getMessage());
                    }
                }

                Assertions.assertEquals(
                    State.IDLE,
                    renderer.getState(),
                    "State is not the expected"
                );
            });

        this.mainActivityActivityTestRule.finishActivity();
    }

    /**
     * Helper method that checks if the current system should or not execute the
     * flaky tests.
     *
     * @param numCores The number of CPU cores available.
     */
    private static void checksIfSystemShouldContinue(final int numCores) {
        LOGGER.info("checksIfSystemShouldContinue");
        LOGGER.info("BuildConfig.DEBUG: " + BuildConfig.DEBUG);
        LOGGER.info("Build.TAGS: " + Build.TAGS);
        LOGGER.info("numCores: " + numCores);
        Assume.assumeFalse(
            "This test fails in Debug with only 1 core.",
            BuildConfig.DEBUG // Debug mode
                && Build.TAGS.equals("test-keys") // In third party systems (CI)
                && numCores == 1 // Android system with only 1 CPU core
        );
        LOGGER.info("checksIfSystemShouldContinue finish");
    }

    /**
     * Auxiliary class which represents the render {@link Button}.
     */
    private static class ViewActionButton implements ViewAction {

        /**
         * The {@link Logger} for this class.
         */
        private static final Logger LOGGER_BUTTON =
            Logger.getLogger(MainActivityTest.ViewActionButton.class.getName());

        /**
         * The expected text for the {@link Button}.
         */
        private final String expectedText;

        /**
         * The constructor for this class.
         */
        @Contract(pure = true)
        ViewActionButton(final String expectedText) {
            this.expectedText = expectedText;
        }

        @Nonnull
        @Override
        public final Matcher<View> getConstraints() {
            LOGGER_BUTTON.info("ViewActionButton#getConstraints");

            return ViewMatchers.isAssignableFrom(Button.class);
        }

        @Nonnull
        @Override
        public final String getDescription() {
            LOGGER_BUTTON.info("ViewActionButton#getDescription");

            return "Click button";
        }

        @Override
        public final void perform(@Nonnull final UiController uiController, @Nonnull final View view) {
            LOGGER_BUTTON.info("ViewActionButton#perform (" + this.expectedText + ")");

            final Button button = (Button) view;
            LOGGER_BUTTON.info("ViewActionButton#perform waiting");

            boolean textEquals = button.getText().toString().equals(this.expectedText);
            while (textEquals) {
                uiController.loopMainThreadForAtLeast(3000L);
                textEquals = button.getText().toString().equals(this.expectedText);
                LOGGER_BUTTON.info("ViewActionButton# waiting button to NOT have '" + this.expectedText + "' written!!!");
            }

            uiController.loopMainThreadUntilIdle();
            LOGGER_BUTTON.info("ViewActionButton#perform clicking button");
            boolean result = button.performClick();
            while (!result) {
                uiController.loopMainThreadForAtLeast(3000L);
                result = button.performClick();
                LOGGER_BUTTON.info("ViewActionButton# waiting to click button!!!");
            }
            LOGGER_BUTTON.info("ViewActionButton#perform button clicked");

            textEquals = button.getText().toString().equals(this.expectedText);
            while (!textEquals) {
                uiController.loopMainThreadForAtLeast(3000L);
                textEquals = button.getText().toString().equals(this.expectedText);
                LOGGER_BUTTON.info("ViewActionButton# waiting button to have '" + this.expectedText + "' written!!!");
            }

//            uiController.loopMainThreadForAtLeast(3000L);

            LOGGER_BUTTON.info("ViewActionButton#perform finished");
        }
    }

    /**
     * Auxiliary class which represents a {@link NumberPicker}.
     */
    private static class ViewActionNumberPicker implements ViewAction {

        /**
         * The {@link Logger} for this class.
         */
        private static final Logger LOGGER_PICKER =
            Logger.getLogger(MainActivityTest.ViewActionNumberPicker.class.getName());

        /**
         * The value to be set in the {@link NumberPicker}.
         */
        private final int value;

        /**
         * The constructor for this class.
         *
         * @param value The value for the {@link NumberPicker}.
         */
        @Contract(pure = true)
        ViewActionNumberPicker(final int value) {
            this.value = value;
        }

        @Override
        @Nonnull
        public final Matcher<View> getConstraints() {
            LOGGER_PICKER.info("ViewActionNumberPicker#getConstraints");

            return ViewMatchers.isAssignableFrom(NumberPicker.class);
        }

        @Override
        @Nonnull
        public final String getDescription() {
            LOGGER_PICKER.info("ViewActionNumberPicker#getDescription");

            return "Set the value of a NumberPicker";
        }

        @Override
        public final void perform(@Nonnull final UiController uiController, final View view) {
            LOGGER_PICKER.info("ViewActionNumberPicker#perform");

            final NumberPicker numberPicker = (NumberPicker) view;

            uiController.loopMainThreadUntilIdle();
            numberPicker.setValue(this.value);
            uiController.loopMainThreadUntilIdle();

            LOGGER_PICKER.info("ViewActionNumberPicker#perform finished");
        }
    }

    /**
     * Auxiliary class which represents a {@link CheckBox}.
     */
    private static class ViewActionCheckBox implements ViewAction {

        /**
         * The {@link Logger} for this class.
         */
        private static final Logger LOGGER_CHECKBOX =
            Logger.getLogger(MainActivityTest.ViewActionCheckBox.class.getName());

        /**
         * The constructor for this class.
         */
        @Contract(pure = true)
        ViewActionCheckBox() {
        }

        @Override
        @Nonnull
        public final Matcher<View> getConstraints() {
            LOGGER_CHECKBOX.info("ViewActionCheckBox#getConstraints");

            return ViewMatchers.isAssignableFrom(CheckBox.class);
        }

        @Override
        @Nonnull
        public final String getDescription() {
            LOGGER_CHECKBOX.info("ViewActionCheckBox#getDescription");

            return "Click checkbox";
        }

        @Override
        public final void perform(@Nonnull final UiController uiController, final View view) {
            LOGGER_CHECKBOX.info("ViewActionCheckBox#perform");

            final CheckBox checkBox = (CheckBox) view;

            uiController.loopMainThreadUntilIdle();
            checkBox.performClick();
            uiController.loopMainThreadUntilIdle();

            LOGGER_CHECKBOX.info("ViewActionCheckBox#perform finished");
        }
    }
}
