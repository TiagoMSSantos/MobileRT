package puscas.mobilertapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import puscas.mobilertapp.DrawView;
import puscas.mobilertapp.MainActivity;
import puscas.mobilertapp.MainRenderer;
import puscas.mobilertapp.R;
import puscas.mobilertapp.ViewActionWait;
import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.Constants;
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
     * @param testName           The name of the current test.
     * @param activity           The {@link MainActivity} of MobileRT.
     * @param expectedButtonText The expected {@link Button} text.
     * @param expectedStates     The expected {@link State}s.
     * @throws TimeoutException If the Ray Tracing engine didn't reach the expected {@link State}.
     */
    public static void waitUntil(final String testName,
                                 @NonNull final MainActivity activity,
                                 final String expectedButtonText,
                                 final State... expectedStates) throws TimeoutException {
        logger.info("waitUntil start, expected button: " + expectedButtonText + ", expected state(s): " + Arrays.toString(expectedStates));
        final AtomicBoolean done = new AtomicBoolean(false);
        // The test 'PreviewTest#testPreviewSceneOrthographicCamera' starts to fail when using 20ms.
        final int waitInMillis = 15;

        final DrawView drawView = UtilsT.getPrivateField(activity, "drawView");
        final MainRenderer renderer = drawView.getRenderer();

        final int timeToWaitInMillis = Objects.equals(expectedButtonText, Constants.STOP) ? 20 * 1000 : 90 * 1000;
        for (int currentTimeMs = 0; currentTimeMs < timeToWaitInMillis && !done.get(); currentTimeMs += waitInMillis) {
            ViewActionWait.waitFor(waitInMillis);
            Espresso.onView(ViewMatchers.withId(R.id.renderButton))
                .inRoot(RootMatchers.isTouchable())
                .check((view, exception) -> {
                    final Button renderButton = view.findViewById(R.id.renderButton);
                    final String renderButtonText = renderButton.getText().toString();
                    final State rendererState = renderer.getState();
                    if (Objects.equals(renderButtonText, expectedButtonText) && Arrays.asList(expectedStates).contains(rendererState)) {
                        done.set(true);
                        logger.info("waitUntil for button update success");
                    }
            });
        }

        if (!done.get()) {
            final State rendererState = renderer.getState();
            final String errorMessage = "Test: " + testName + ", State: '" + rendererState.name() + "' (expecting " + Arrays.toString(expectedStates) + "), Expected button: '" + expectedButtonText + "'.";
            throw new TimeoutException("The Ray Tracing engine didn't reach the expected state in " + (float) (timeToWaitInMillis) / 1000 + " secs. " + errorMessage);
        }

        if (Objects.equals(expectedButtonText, Constants.STOP)) {
            done.set(false);
            final int timeToWaitForUpdatedImageInMillis = 10 * 1000;
            logger.info("Waiting '" + timeToWaitForUpdatedImageInMillis + "'ms for Bitmap to contain some rendered pixels.");
            for (int currentTimeMs = 0; currentTimeMs < timeToWaitForUpdatedImageInMillis && !done.get(); currentTimeMs += waitInMillis) {
                ViewActionWait.waitFor(waitInMillis);
                final Bitmap bitmap = UtilsT.getPrivateField(renderer, "bitmap");
                final boolean bitmapSingleColor = UtilsT.isBitmapSingleColor(bitmap);
                if (!bitmapSingleColor) {
                    done.set(true);
                    logger.info("waitUntil for bitmap update success");
                }
            }

            if (!done.get()) {
                final Bitmap bitmap = UtilsT.getPrivateField(renderer, "bitmap");
                final boolean bitmapSingleColor = UtilsT.isBitmapSingleColor(bitmap);
                final String errorMessage = "Test: " + testName + ", Bitmap had no pixel rendered: " + bitmapSingleColor + ".";
                throw new TimeoutException("The Ray Tracing engine didn't reach the expected state in " + (float) (timeToWaitForUpdatedImageInMillis) / 1000 + " secs. " + errorMessage);
            }
        }

        logger.info("waitUntil finished");
    }

    /**
     * Helper method that resets the {@link android.widget.NumberPicker}s values
     * in the UI to some predefined values.
     *
     * @param scene       The id of the scene to set.
     * @param accelerator The {@link Accelerator} to use.
     * @param spp         The number of samples per pixel. Acceptable range is: [1-99].
     * @param spl         The number of samples per light. Acceptable range is: [1-100].
     */
    public static void resetPickerValues(final int scene, final Accelerator accelerator, final int spp, final int spl) {
        logger.info("resetPickerValues");

        final int numCores = UtilsContext.getNumOfCores(InstrumentationRegistry.getInstrumentation().getTargetContext());

        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SCENE, R.id.pickerScene, scene);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_THREADS, R.id.pickerThreads, numCores);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SIZE, R.id.pickerSize, 8);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_PIXEL, R.id.pickerSamplesPixel, spp);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_LIGHT, R.id.pickerSamplesLight, spl);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_ACCELERATOR, R.id.pickerAccelerator, accelerator.ordinal());
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SHADER, R.id.pickerShader, Shader.PATH_TRACING.ordinal());
    }

}
