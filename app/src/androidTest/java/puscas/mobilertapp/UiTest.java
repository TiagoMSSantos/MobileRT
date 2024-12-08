package puscas.mobilertapp;

import static puscas.mobilertapp.ConstantsAndroidTests.BUTTON_MESSAGE;

import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;

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
        ViewActionWait.waitForButtonUpdate(0);
        final int viewId = R.id.preview;
        Espresso.onView(ViewMatchers.withId(viewId))
            .inRoot(RootMatchers.isTouchable())
            .perform(new ViewActionWait<>(0, viewId, !expectedValue))
            .check((view, exception) -> {
                UtilsT.rethrowException(exception);
                assertPreviewCheckBox(view, !expectedValue);
            })
            .perform(ViewActions.click(InputDevice.SOURCE_TOUCHSCREEN, MotionEvent.BUTTON_PRIMARY))
            .perform(new ViewActionWait<>(0, viewId, expectedValue))
            .check((view, exception) -> {
                UtilsT.rethrowException(exception);
                assertPreviewCheckBox(view, expectedValue);
            });
        ViewActionWait.waitForButtonUpdate(0);
    }

    /**
     * Asserts the {@link CheckBox} expected value.
     *
     * @param view          The {@link View}.
     * @param expectedValue The expected value for the {@link CheckBox}.
     */
    static void assertPreviewCheckBox(@NonNull final View view,
                                      final boolean expectedValue) {
        final CheckBox checkbox = view.findViewById(R.id.preview);
        Assert.assertEquals(Constants.CHECK_BOX_MESSAGE, Constants.PREVIEW, checkbox.getText().toString());
        Assert.assertEquals( "Check box has not the expected value", expectedValue, checkbox.isChecked());
    }

    /**
     * Helper method which tests the range of the {@link NumberPicker} in the UI.
     */
    private static void assertPickerNumbers() {
        final int numCores = UtilsContext.getNumOfCores(InstrumentationRegistry.getInstrumentation().getTargetContext());
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
    @Test
    public void testUI() throws TimeoutException {
        UtilsT.assertRenderButtonText(Constants.RENDER);

        assertClickRenderButton(1);
        assertPickerNumbers();
        clickPreviewCheckBox(false);
    }

    /**
     * Tests clicking the render {@link Button} many times without preview.
     *
     * @throws TimeoutException If the Ray Tracing engine didn't reach the expected {@link State}.
     * @implNote This test can take more than 2 minutes in CI.
     */
    @Test
    public void testClickRenderButtonManyTimesWithoutPreview() throws TimeoutException {
        clickPreviewCheckBox(false);

        assertClickRenderButton(5);
    }

    /**
     * Tests clicking the render {@link Button} many times with preview.
     *
     * @throws TimeoutException If the Ray Tracing engine didn't reach the expected {@link State}.
     * @implNote This test can take more than 2 minutes in CI.
     */
    @Test
    public void testClickRenderButtonManyTimesWithPreview() throws TimeoutException {
        clickPreviewCheckBox(false);
        clickPreviewCheckBox(true);

        assertClickRenderButton(5);
    }

    /**
     * Tests clicking the render {@link Button} with a long press.
     * It is expected for the {@link android.app.Activity} to restart.
     */
    @Test
    public void testClickRenderButtonLongPress() {
        Espresso.onView(ViewMatchers.withId(R.id.renderButton))
            .inRoot(RootMatchers.isTouchable())
            .perform(new ViewActionButton(Constants.RENDER, true))
            .perform(new ViewActionWait<>(0, R.id.renderButton))
            .check((view, exception) -> {
                UtilsT.rethrowException(exception);
                final Button button = (Button) view;
                Assert.assertEquals(BUTTON_MESSAGE, Constants.RENDER, button.getText().toString());
            });
        UtilsT.testStateAndBitmap(true);
    }

    /**
     * Helper method which tests clicking the render {@link Button}.
     *
     * @param repetitions The number of repetitions.
     */
    private void assertClickRenderButton(final int repetitions) throws TimeoutException {
        UtilsContextT.resetPickerValues(Scene.CORNELL2.ordinal(), Accelerator.NAIVE, 99, 1);

        final List<String> buttonTextList = ImmutableList.of(Constants.STOP, Constants.RENDER);
        for (int currentIndex = 0; currentIndex < buttonTextList.size() * repetitions; currentIndex++) {
            final String message = "currentIndex = " + currentIndex;
            logger.info(message);

            final int expectedIndex = currentIndex % buttonTextList.size();
            final String expectedButtonText = buttonTextList.get(expectedIndex);

            Espresso.onView(ViewMatchers.withId(R.id.renderButton))
                .inRoot(RootMatchers.isTouchable())
                .perform(new ViewActionButton(expectedButtonText, false));

            if (expectedIndex % 2 == 0) {
                UtilsContextT.waitUntil(this.testName.getMethodName(), this.activity, expectedButtonText, State.BUSY);
            } else {
                UtilsContextT.waitUntil(this.testName.getMethodName(), this.activity, expectedButtonText, State.IDLE, State.FINISHED);
                ViewActionWait.waitForButtonUpdate(0);
                // Only update pickers when app is idle.
                incrementCountersAndUpdatePickers();
            }
        }
    }

    /**
     * Helper method that increments all the fields' counters and updates all
     * the {@link NumberPicker}s in the UI with the current values.
     */
    private void incrementCountersAndUpdatePickers() {
        final int numCores = UtilsContext.getNumOfCores(InstrumentationRegistry.getInstrumentation().getTargetContext());
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
