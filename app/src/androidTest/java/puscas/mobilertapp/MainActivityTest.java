package puscas.mobilertapp;

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
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import java8.util.stream.IntStreams;
import java8.util.stream.StreamSupport;

import static puscas.mobilertapp.utils.Constants.CHECK_BOX_MESSAGE;
import static puscas.mobilertapp.utils.Constants.EMPTY_FILE;
import static puscas.mobilertapp.utils.Constants.FILE_SHOULD_EXIST;
import static puscas.mobilertapp.utils.Constants.OBJ_FILE_CONFERENCE;
import static puscas.mobilertapp.utils.Constants.OBJ_FILE_NOT_EXISTS;
import static puscas.mobilertapp.utils.Constants.OBJ_FILE_TEAPOT;
import static puscas.mobilertapp.utils.Constants.PREVIEW;
import static puscas.mobilertapp.utils.Constants.RENDER;
import static puscas.mobilertapp.utils.Constants.SET_UP;
import static puscas.mobilertapp.utils.Constants.SET_UP_ALL;
import static puscas.mobilertapp.utils.Constants.STOP;
import static puscas.mobilertapp.utils.Constants.TEAR_DOWN;
import static puscas.mobilertapp.utils.Constants.TEAR_DOWN_ALL;

/**
 * The test suite for {@link MainActivity}.
 */
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
    @Rule
    public final ActivityTestRule<MainActivity> mainActivityActivityTestRule =
            new ActivityTestRule<>(MainActivity.class, true, true);

    /**
     * A setup method which is called first.
     */
    @BeforeClass
    public static void setUpAll() {
        LOGGER.info(SET_UP_ALL);
    }

    /**
     * A tear down method which is called last.
     */
    @AfterClass
    public static void tearDownAll() {
        LOGGER.info(TEAR_DOWN_ALL);
    }

    /**
     * Helper method which tests the range of the {@link NumberPicker} in the UI.
     */
    private static void testPickerNumbers(final int numOfCores) {
        IntStreams.rangeClosed(0, 2).forEach(value ->
            assertPickerValue("pickerAccelerator", R.id.pickerAccelerator, value)
        );
        IntStreams.rangeClosed(1, 100).forEach(value ->
            assertPickerValue("pickerSamplesLight", R.id.pickerSamplesLight, value)
        );
        IntStreams.rangeClosed(0, 6).forEach(value ->
            assertPickerValue("pickerScene", R.id.pickerScene, value)
        );
        IntStreams.rangeClosed(0, 4).forEach(value ->
            assertPickerValue("pickerShader", R.id.pickerShader, value)
        );
        IntStreams.rangeClosed(1, numOfCores).forEach(value ->
            assertPickerValue("pickerThreads", R.id.pickerThreads, value)
        );
        IntStreams.rangeClosed(1, 10).forEach(value ->
            assertPickerValue("pickerSamplesPixel", R.id.pickerSamplesPixel, value)
        );
        IntStreams.rangeClosed(1, 8).forEach(value ->
            assertPickerValue("pickerSize", R.id.pickerSize, value)
        );
    }

    /**
     * Helper method which tests clicking the render {@link Button}.
     *
     * @param repetitions The number of repetitions.
     */
    private void testRenderButton(final int repetitions) {
        if (repetitions <= 4) {
            assertPickerValue("pickerSamplesPixel", R.id.pickerSamplesPixel, 1);
            assertPickerValue("pickerScene", R.id.pickerScene, 5);
        } else {
            assertPickerValue("pickerSamplesPixel", R.id.pickerSamplesPixel, 3);
            assertPickerValue("pickerScene", R.id.pickerScene, 2);
        }
        assertPickerValue("pickerThreads", R.id.pickerThreads, 1);
        assertPickerValue("pickerSize", R.id.pickerSize, 8);
        assertPickerValue("pickerSamplesLight", R.id.pickerSamplesLight, 1);
        assertPickerValue("pickerAccelerator", R.id.pickerAccelerator, 2);
        assertPickerValue("pickerShader", R.id.pickerShader, 2);

        final int numOfCores = this.mainActivityActivityTestRule.getActivity().getNumOfCores();
        final List<String> buttonTextList = ImmutableList.<String>builder().add(STOP, RENDER).build();
        IntStreams.range(0, buttonTextList.size() * repetitions).forEach(currentIndex -> {
            LOGGER.info("currentIndex = " + currentIndex);
            final int finalCounterScene = this.counterScene % 6;
            this.counterScene++;
            final int finalCounterAccelerator = this.counterAccelerator % 2;
            this.counterAccelerator++;
            final int finalCounterShader = this.counterShader % 4;
            this.counterShader++;
            final int finalCounterSPP = this.counterSPP % 10;
            this.counterSPP++;
            final int finalCounterSPL = this.counterSPL % 100;
            this.counterSPL++;
            final int finalCounterResolution = this.counterResolution % 9;
            this.counterResolution++;
            final int finalCounterThreads = this.counterThreads % numOfCores;
            this.counterThreads++;
            assertPickerValue("pickerScene", R.id.pickerScene, Math.min(finalCounterScene, 3));
            assertPickerValue("pickerAccelerator", R.id.pickerAccelerator, Math.max(finalCounterAccelerator, 0));
            assertPickerValue("pickerShader", R.id.pickerShader, Math.max(finalCounterShader, 0));
            assertPickerValue("pickerSize", R.id.pickerSize, Math.max(finalCounterResolution, 6));
            assertPickerValue("pickerSamplesPixel", R.id.pickerSamplesPixel, Math.max(finalCounterSPP, 1));
            assertPickerValue("pickerSamplesLight", R.id.pickerSamplesLight, Math.max(finalCounterSPL, 1));
            assertPickerValue("pickerThreads", R.id.pickerThreads, Math.max(finalCounterThreads, 1));

            final int expectedIndex = currentIndex % buttonTextList.size();
            final String expectedButtonText = buttonTextList.get(expectedIndex);
            final ViewInteraction viewInteraction = Espresso.onView(ViewMatchers.withId(R.id.renderButton));
            viewInteraction.perform(new ViewActionButton());
            viewInteraction.check((view, exception) -> {
                final Button renderButton = view.findViewById(R.id.renderButton);
                Assertions.assertEquals(
                    expectedButtonText,
                    renderButton.getText().toString(),
                    "Button message at currentIndex " + currentIndex
                );
            });
        });
    }

    /**
     * Helper method which tests clicking the preview {@link CheckBox}.
     */
    private static void testPreviewCheckBox() {
        final ViewInteraction viewInteraction = Espresso.onView(ViewMatchers.withId(R.id.preview));
        viewInteraction.check((view, exception) ->
            assertCheckBox(view, R.id.preview, PREVIEW, CHECK_BOX_MESSAGE, true)
        );
        viewInteraction.perform(new ViewActionButton());
        viewInteraction.check((view, exception) ->
            assertCheckBox(view, R.id.preview, PREVIEW, CHECK_BOX_MESSAGE, false)
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
    private static void assertPickerValue(final String pickerName,
                                          final int pickerId,
                                          final int expectedValue) {
        Espresso.onView(ViewMatchers.withId(pickerId))
            .perform(new MainActivityTest.ViewActionNumberPicker(expectedValue))
            .check((view, exception) -> {
                final NumberPicker numberPicker = view.findViewById(pickerId);
                Assertions.assertEquals(expectedValue, numberPicker.getValue(),
                        "Number picker '" + pickerName + "' with wrong value");
            });
    }

    /**
     * Setup method called before each test.
     */
    @Before
    public void setUp() {
        LOGGER.info(SET_UP);
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        LOGGER.info(TEAR_DOWN);
    }

    /**
     * Tests that a file in the Android device exists and is readable.
     */
    @Ignore("In CI, these files don't exist.")
    @Test
    public void testFilesExistAndReadable() {
        LOGGER.info("testFilesExistAndReadable");
        final MainActivity activity = this.mainActivityActivityTestRule.getActivity();
        final List<String> paths = ImmutableList.<String>builder().add(
            activity.getSDCardPath() + OBJ_FILE_CONFERENCE,
            activity.getSDCardPath() + OBJ_FILE_TEAPOT
        ).build();
        StreamSupport.stream(paths)
            .forEach(path -> {
                final File file = new File(path);
                Assertions.assertTrue(file.exists(), FILE_SHOULD_EXIST);
                Assertions.assertTrue(file.canRead(), "File should be readable!");
            });
    }

    /**
     * Tests that a file does not exist in the Android device.
     */
    @Test
    public void testFilesNotExist() {
        LOGGER.info("testFilesNotExist");
        final MainActivity activity = this.mainActivityActivityTestRule.getActivity();
        final List<String> paths = ImmutableList.<String>builder().add(
            EMPTY_FILE,
            activity.getSDCardPath() + OBJ_FILE_NOT_EXISTS
        ).build();
        StreamSupport.stream(paths)
            .forEach(path -> {
                final File file = new File(path);
                Assertions.assertFalse(file.exists(), "File should not exist!");
                Assertions.assertFalse(file.canRead(), "File should not be readable!");
            });
    }

    /**
     * Tests changing all the {@link NumberPicker} and clicking the render {@link Button} many times.
     */
    @Test
    public void testUI() {
        LOGGER.info("testUI");
        final MainActivity activity = this.mainActivityActivityTestRule.getActivity();

        Espresso.onView(ViewMatchers.withId(R.id.renderButton))
            .check((view, exception) -> {
                final Button button = view.findViewById(R.id.renderButton);
                Assertions.assertEquals(RENDER, button.getText().toString(), "Button message");
            });

        testRenderButton(4);
        testRenderButton(8);
//        testRenderButton(200);

        final int numOfCores = activity.getNumOfCores();
        testPickerNumbers(numOfCores);
        testPreviewCheckBox();

        this.mainActivityActivityTestRule.finishActivity();
    }

    /**
     * Auxiliary class which represents the render {@link Button}.
     */
    private static class ViewActionButton implements ViewAction {

        /**
         * The {@link Logger} for this class.
         */
        private static final Logger LOGGER_BUTTON =
                Logger.getLogger(ViewActionButton.class.getName());

        /**
         * The constructor for this class.
         */
        ViewActionButton() {
        }

        @Override
        public final Matcher<View> getConstraints() {
            LOGGER_BUTTON.info("testRenderButton#getConstraints");

            return ViewMatchers.isAssignableFrom(Button.class);
        }

        @Override
        public final String getDescription() {
            LOGGER_BUTTON.info("testRenderButton#getDescription");

            return "Click render button";
        }

        @Override
        public final void perform(final UiController uiController, final View view) {
            LOGGER_BUTTON.info("testRenderButton#perform");

            view.performClick();
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
        ViewActionNumberPicker(final int value) {
            this.value = value;
        }

        @Override
        @Nonnull
        public final Matcher<View> getConstraints() {
            LOGGER_PICKER.info("assertPickerValue#getConstraints");

            return ViewMatchers.isAssignableFrom(NumberPicker.class);
        }

        @Override
        @Nonnull public final String getDescription() {
            LOGGER_PICKER.info("assertPickerValue#getDescription");

            return "Set the value of a NumberPicker";
        }

        @Override
        public final void perform(final UiController uiController, final View view) {
            LOGGER_PICKER.info("assertPickerValue#perform");

            ((NumberPicker) view).setValue(this.value);
        }
    }
}
