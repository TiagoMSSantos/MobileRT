package puscas.mobilertapp.utils;

import android.content.Context;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;

import com.google.common.util.concurrent.Uninterruptibles;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import puscas.mobilertapp.DrawView;
import puscas.mobilertapp.MainActivity;
import puscas.mobilertapp.MainRenderer;
import puscas.mobilertapp.R;
import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.ConstantsUI;
import puscas.mobilertapp.constants.Shader;
import puscas.mobilertapp.constants.State;

/**
 * Helper class which contains helper methods that need the {@link Context} for the tests.
 */
public final class UtilsContextT {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(UtilsContextT.class.getSimpleName());

    /**
     * Private constructor to avoid creating instances.
     */
    private UtilsContextT() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Helper method that waits until the Ray Tracing engine reaches the expected {@link State}.
     *
     * @param activity           The {@link MainActivity} of MobileRT.
     * @param expectedButtonText The expected {@link Button} text.
     * @param expectedStates     The expected {@link State}s.
     * @throws TimeoutException If the Ray Tracing engine didn't reach the expected {@link State}.
     */
    public static void waitUntil(@NonNull final MainActivity activity, final String expectedButtonText, final State... expectedStates) throws TimeoutException {
        logger.info("waitUntil start, expected button: " + expectedButtonText + ", expected state: " + expectedStates[0].name());
        final AtomicBoolean done = new AtomicBoolean(false);
        final long advanceSecs = 1L;

        final DrawView drawView = UtilsT.getPrivateField(activity, "drawView");
        final MainRenderer renderer = drawView.getRenderer();

        for (long currentTimeSecs = 0L; currentTimeSecs < 60L && !done.get(); currentTimeSecs += advanceSecs) {
            Uninterruptibles.sleepUninterruptibly(advanceSecs, TimeUnit.SECONDS);

            Espresso.onView(ViewMatchers.withId(R.id.renderButton))
                .check((view, exception) -> {
                    final Button renderButton = view.findViewById(R.id.renderButton);
                    final String renderButtonText = renderButton.getText().toString();
                    final State rendererState = renderer.getState();
                    logger.info("State: '" + rendererState.name() + "', Button: '" + renderButtonText + "'. Expecting: " + expectedButtonText + ", " + Arrays.toString(expectedStates));
                    if (Objects.equals(renderButtonText, expectedButtonText) && Arrays.asList(expectedStates).contains(rendererState)) {
                        done.set(true);
                        logger.info("waitUntil success");
                    }
            });
        }

        logger.info("waitUntil finished");
        if (!done.get()) {
            throw new TimeoutException("The Ray Tracing engine didn't reach the expected state.");
        }
    }

    /**
     * Helper method that resets the {@link android.widget.NumberPicker}s values
     * in the UI to some predefined values.
     *
     * @param context     The {@link Context} of the application.
     * @param scene       The id of the scene to set.
     * @param accelerator The {@link Accelerator} to use.
     * @param spp         The number of samples per pixel. Acceptable range is: [1-99].
     * @param spl         The number of samples per light. Acceptable range is: [1-100].
     */
    public static void resetPickerValues(@NonNull final Context context, final int scene, final Accelerator accelerator, final int spp, final int spl) {
        logger.info("resetPickerValues");

        final int numCores = UtilsContext.getNumOfCores(context);

        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SCENE, R.id.pickerScene, scene);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_THREADS, R.id.pickerThreads, numCores);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SIZE, R.id.pickerSize, 8);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_PIXEL, R.id.pickerSamplesPixel, spp);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_LIGHT, R.id.pickerSamplesLight, spl);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_ACCELERATOR, R.id.pickerAccelerator, accelerator.ordinal());
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SHADER, R.id.pickerShader, Shader.PATH_TRACING.ordinal());
    }

}
