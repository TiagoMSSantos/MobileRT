package puscas.mobilertapp;

import static puscas.mobilertapp.ConstantsAndroidTests.BUTTON_MESSAGE;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;

import com.google.common.collect.ImmutableList;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import java8.util.stream.IntStreams;
import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.Constants;
import puscas.mobilertapp.constants.ConstantsUI;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.constants.Shader;
import puscas.mobilertapp.constants.State;
import puscas.mobilertapp.utils.UtilsContext;
import puscas.mobilertapp.utils.UtilsContextT;
import puscas.mobilertapp.utils.UtilsPickerT;
import puscas.mobilertapp.utils.UtilsT;

/**
 * The test suite for the User Interface.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class UiTest extends AbstractTest {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(UiTest.class.getSimpleName());

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
    private int counterSpl = 0;

    /**
     * The current index of the number of samples per pixel in the {@link NumberPicker}.
     */
    private int counterSpp = 0;

    /**
     * The current index of the number of threads in the {@link NumberPicker}.
     */
    private int counterThreads = 0;

    /**
     * Helper method which tests clicking the preview {@link CheckBox}.
     *
     * @param expectedValue The expected value for the {@link CheckBox}.
     */
    public static void clickPreviewCheckBox(final boolean expectedValue) {
        UtilsT.waitForAppToIdle();
        Espresso.onView(ViewMatchers.withId(R.id.preview))
            .check((view, exception) ->
                assertPreviewCheckBox(view, !expectedValue)
            )
            .perform(ViewActions.click())
            .check((view, exception) ->
                assertPreviewCheckBox(view, expectedValue)
            );
    }

    /**
     * Asserts the {@link CheckBox} expected value.
     *
     * @param view          The {@link View}.
     * @param expectedValue The expected value for the {@link CheckBox}.
     */
    private static void assertPreviewCheckBox(@NonNull final View view,
                                              final boolean expectedValue) {
        final CheckBox checkbox = view.findViewById(R.id.preview);
        Assert.assertEquals(Constants.CHECK_BOX_MESSAGE, Constants.PREVIEW, checkbox.getText().toString());
        Assert.assertEquals( "Check box has not the expected value", expectedValue, checkbox.isChecked());
    }

    /**
     * Helper method which tests the range of the {@link NumberPicker} in the UI.
     *
     * @param numCores The number of CPU cores in the system.
     */
    private static void assertPickerNumbers(final int numCores) {
        IntStreams.rangeClosed(0, 2).forEach(value ->
            UtilsPickerT.changePickerValue(ConstantsUI.PICKER_ACCELERATOR, R.id.pickerAccelerator, value)
        );
        IntStreams.rangeClosed(1, 100).forEach(value ->
            UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_LIGHT, R.id.pickerSamplesLight, value)
        );
        IntStreams.rangeClosed(0, 4).forEach(value ->
            UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SCENE, R.id.pickerScene, value)
        );
        IntStreams.rangeClosed(0, 4).forEach(value ->
            UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SHADER, R.id.pickerShader, value)
        );
        IntStreams.rangeClosed(1, numCores).forEach(value ->
            UtilsPickerT.changePickerValue(ConstantsUI.PICKER_THREADS, R.id.pickerThreads, value)
        );
        IntStreams.rangeClosed(1, 99).forEach(value ->
            UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_PIXEL, R.id.pickerSamplesPixel, value)
        );
        IntStreams.rangeClosed(1, 8).forEach(value ->
            UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SIZE, R.id.pickerSize, value)
        );
    }

    /**
     * Tests changing all the {@link NumberPicker} and clicking the render
     * {@link Button} few times.
     *
     * @throws TimeoutException If the Ray Tracing engine didn't reach the expected {@link State}.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testUI() throws TimeoutException {
        UtilsT.assertRenderButtonText(Constants.RENDER);

        final int numCores = UtilsContext.getNumOfCores(this.activity);
        assertClickRenderButton(1, numCores);
        assertPickerNumbers(numCores);
        clickPreviewCheckBox(false);
    }

    /**
     * Tests clicking the render {@link Button} many times without preview.
     *
     * @throws TimeoutException If the Ray Tracing engine didn't reach the expected {@link State}.
     * @implNote This test can take more than 2 min in CI.
     */
    @Test(timeout = 3L * 60L * 1000L)
    public void testClickRenderButtonManyTimesWithoutPreview() throws TimeoutException {
        clickPreviewCheckBox(false);

        final int numCores = UtilsContext.getNumOfCores(this.activity);
        assertClickRenderButton(5, numCores);
    }

    /**
     * Tests clicking the render {@link Button} many times with preview.
     *
     * @throws TimeoutException If the Ray Tracing engine didn't reach the expected {@link State}.
     * @implNote This test can take more than 2 min in CI.
     */
    @Test(timeout = 3L * 60L * 1000L)
    public void testClickRenderButtonManyTimesWithPreview() throws TimeoutException {
        clickPreviewCheckBox(false);
        clickPreviewCheckBox(true);

        final int numCores = UtilsContext.getNumOfCores(this.activity);
        assertClickRenderButton(5, numCores);
    }

    /**
     * Tests clicking the render {@link Button} with a long press.
     * It is expected for the {@link android.app.Activity} to restart.
     */
    @Test(timeout = 60L * 1000L)
    public void testClickRenderButtonLongPress() {
        Espresso.onView(ViewMatchers.withId(R.id.renderButton))
            .perform(new ViewActionButton(Constants.RENDER, true))
            .check((view, exception) -> {
                final Button button = (Button) view;
                Assert.assertEquals(BUTTON_MESSAGE, Constants.RENDER, button.getText().toString());
            });
        UtilsT.testStateAndBitmap(true);
    }

    /**
     * Helper method which tests clicking the render {@link Button}.
     *
     * @param repetitions The number of repetitions.
     * @param numCores    The number of CPU cores in the system.
     */
    private void assertClickRenderButton(final int repetitions, final int numCores) throws TimeoutException {
        UtilsContextT.resetPickerValues(this.activity, Scene.CORNELL2.ordinal(), Accelerator.BVH, 99, 99);

        final List<String> buttonTextList = ImmutableList.of(Constants.STOP, Constants.RENDER);
        for (int currentIndex = 0; currentIndex < buttonTextList.size() * repetitions; currentIndex++) {
            final String message = "currentIndex = " + currentIndex;
            logger.info(message);

            incrementCountersAndUpdatePickers(numCores);

            final int expectedIndexOld = currentIndex > 0 ? (currentIndex - 1) % buttonTextList.size() : 1;
            final String expectedButtonTextOld = buttonTextList.get(expectedIndexOld);
            final ViewInteraction viewInteraction = Espresso.onView(ViewMatchers.withId(R.id.renderButton));
            final int expectedIndex = currentIndex % buttonTextList.size();
            final String expectedButtonText = buttonTextList.get(expectedIndex);

            UtilsT.assertRenderButtonText(expectedButtonTextOld);

            viewInteraction.perform(new ViewActionButton(expectedButtonText, false));

            if (expectedIndex % 2 == 0) {
                UtilsContextT.waitUntil(this.activity, expectedButtonText, State.BUSY);
            } else {
                UtilsContextT.waitUntil(this.activity, expectedButtonText, State.IDLE, State.FINISHED);
            }
            UtilsT.assertRenderButtonText(expectedButtonText);
        }
    }

    /**
     * Helper method that increments all the fields' counters and updates all
     * the {@link NumberPicker}s in the UI with the current values.
     *
     * @param numCores The number of CPU cores in the system.
     */
    private void incrementCountersAndUpdatePickers(final int numCores) {
        final int finalCounterScene = Math.min(this.counterScene % Scene.values().length, 3);
        final int finalCounterAccelerator = Math.max(this.counterAccelerator % Accelerator.values().length, 1);
        final int finalCounterShader = Math.max(this.counterShader % Shader.values().length, 0);
        final int finalCounterSpp = Math.max(this.counterSpp % 99, 90);
        final int finalCounterSpl = Math.max(this.counterSpl % 100, 1);
        final int finalCounterThreads = Math.max(this.counterThreads % numCores, 1);
        final int finalCounterSize = Math.max(this.counterResolution % 9, 1);

        incrementCounters();

        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SCENE, R.id.pickerScene, finalCounterScene);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_ACCELERATOR, R.id.pickerAccelerator, finalCounterAccelerator);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SHADER, R.id.pickerShader, finalCounterShader);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_PIXEL, R.id.pickerSamplesPixel, finalCounterSpp);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_LIGHT, R.id.pickerSamplesLight, finalCounterSpl);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_THREADS, R.id.pickerThreads, finalCounterThreads);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SIZE, R.id.pickerSize, finalCounterSize);
    }

    /**
     * Helper method that increments all the fields' counters.
     */
    private void incrementCounters() {
        this.counterScene++;
        this.counterAccelerator++;
        this.counterShader++;
        this.counterSpp++;
        this.counterSpl++;
        this.counterResolution++;
        this.counterThreads++;
    }

}
