package puscas.mobilertapp;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.logging.Logger;
import java8.util.stream.IntStreams;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runners.MethodSorters;
import puscas.mobilertapp.utils.Accelerator;
import puscas.mobilertapp.utils.Constants;
import puscas.mobilertapp.utils.ConstantsUI;
import puscas.mobilertapp.utils.Scene;
import puscas.mobilertapp.utils.Shader;
import puscas.mobilertapp.utils.UtilsContext;

@FixMethodOrder(MethodSorters.DEFAULT)
public final class UITest {

    /**
     * The {@link Logger} for this class.
     */
    @Nonnull
    private static final Logger LOGGER = Logger.getLogger(UITest.class.getName());

    /**
     * The rule to create the MainActivity.
     */
    @Nonnull
    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule =
        new ActivityTestRule<>(MainActivity.class, true, true);

    /**
     * The MainActivity to test.
     */
    private MainActivity activity = null;


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
     * Helper method which tests clicking the preview {@link CheckBox}.
     *
     * @param expectedValue The expected value for the {@link CheckBox}.
     */
    private static void clickPreviewCheckBox(final boolean expectedValue) {
        Espresso.onView(ViewMatchers.withId(R.id.preview))
            .check((view, exception) ->
                assertCheckBox(view, R.id.preview, Constants.PREVIEW, Constants.CHECK_BOX_MESSAGE,
                    !expectedValue)
            )
            .perform(ViewActions.click())
            .check((view, exception) ->
                assertCheckBox(view, R.id.preview, Constants.PREVIEW, Constants.CHECK_BOX_MESSAGE,
                    expectedValue)
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
        Assertions
            .assertEquals(expectedDescription, checkbox.getText().toString(), checkBoxMessage);
        Assertions.assertEquals(expectedValue, checkbox.isChecked(),
            "Check box has not the expected value");
    }

    /**
     * Helper method which tests the range of the {@link NumberPicker} in the UI.
     *
     * @param numCores The number of CPU cores in the system.
     */
    private static void assertPickerNumbers(final int numCores) {
        IntStreams.rangeClosed(0, 2).forEach(value ->
            Utils.changePickerValue("pickerAccelerator", R.id.pickerAccelerator, value)
        );
        IntStreams.rangeClosed(1, 100).forEach(value ->
            Utils.changePickerValue("pickerSamplesLight", R.id.pickerSamplesLight, value)
        );
        IntStreams.rangeClosed(0, 6).forEach(value ->
            Utils.changePickerValue("pickerScene", R.id.pickerScene, value)
        );
        IntStreams.rangeClosed(0, 4).forEach(value ->
            Utils.changePickerValue("pickerShader", R.id.pickerShader, value)
        );
        IntStreams.rangeClosed(1, numCores).forEach(value ->
            Utils.changePickerValue("pickerThreads", R.id.pickerThreads, value)
        );
        IntStreams.rangeClosed(1, 99).forEach(value ->
            Utils.changePickerValue("pickerSamplesPixel", R.id.pickerSamplesPixel, value)
        );
        IntStreams.rangeClosed(1, 8).forEach(value ->
            Utils.changePickerValue("pickerSize", R.id.pickerSize, value)
        );
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
                Assertions
                    .assertEquals(Constants.RENDER, button.getText().toString(), "Button message");
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
     * Helper method which tests clicking the render {@link Button}.
     *
     * @param repetitions The number of repetitions.
     * @param numCores    The number of CPU cores in the system.
     */
    private void assertClickRenderButton(final int repetitions, final int numCores) {
        Utils.changePickerValue("pickerSamplesPixel", R.id.pickerSamplesPixel, 99);
        Utils.changePickerValue("pickerScene", R.id.pickerScene, 5);
        Utils.changePickerValue("pickerThreads", R.id.pickerThreads, 1);
        Utils.changePickerValue("pickerSize", R.id.pickerSize, 8);
        Utils.changePickerValue("pickerSamplesLight", R.id.pickerSamplesLight, 1);
        Utils.changePickerValue("pickerAccelerator", R.id.pickerAccelerator, 2);
        Utils.changePickerValue("pickerShader", R.id.pickerShader, 2);

        final int numScenes = Scene.values().length;
        final int numAccelerators = Accelerator.values().length;
        final int numShaders = Shader.values().length;
        final int numSPP = 99;
        final int numSPL = 100;
        final int numRes = 9;

        final List<String> buttonTextList =
            ImmutableList.<String>builder().add(Constants.STOP, Constants.RENDER).build();
        IntStreams.range(0, buttonTextList.size() * repetitions).forEach(currentIndex -> {
            final String message = "currentIndex = " + currentIndex;
            LOGGER.info(message);
            final int finalCounterScene = Math.min(this.counterScene % numScenes, 3);
            this.counterScene++;
            final int finalCounterAccelerator =
                Math.max(this.counterAccelerator % numAccelerators, 1);
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
            Utils.changePickerValue("pickerScene", R.id.pickerScene, finalCounterScene);
            Utils.changePickerValue("pickerAccelerator", R.id.pickerAccelerator,
                finalCounterAccelerator);
            Utils.changePickerValue("pickerShader", R.id.pickerShader, finalCounterShader);
            Utils.changePickerValue("pickerSamplesPixel", R.id.pickerSamplesPixel, finalCounterSPP);
            Utils.changePickerValue("pickerSamplesLight", R.id.pickerSamplesLight, finalCounterSPL);
            Utils.changePickerValue("pickerThreads", R.id.pickerThreads, finalCounterThreads);
            // TODO: the picker for the resolution always resets its value to the default one
            Utils.changePickerValue("pickerSize", R.id.pickerSize, 4);


            final int expectedIndexOld = currentIndex > 0 ?
                (currentIndex - 1) % buttonTextList.size() : 1;
            final String expectedButtonTextOld = buttonTextList.get(expectedIndexOld);
            final ViewInteraction viewInteraction =
                Espresso.onView(ViewMatchers.withId(R.id.renderButton));
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
                        "Button message at currentIndex: " + currentIndex
                            + "(" + expectedIndex + ")"
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

}
