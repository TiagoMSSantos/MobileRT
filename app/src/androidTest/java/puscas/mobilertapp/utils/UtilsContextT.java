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
     * @throws TimeoutException If the Ray Tracing engine didn't reach the expected {@link State} before a timeout occurs.
     */
    public static void waitUntil(final String testName,
                                 @NonNull final MainActivity activity,
                                 final String expectedButtonText,
                                 final State... expectedStates) throws TimeoutException {
        logger.info("waitUntil start, expected button: " + expectedButtonText + ", expected state(s): " + Arrays.toString(expectedStates));
        final AtomicBoolean done = new AtomicBoolean(false);
        final AtomicBoolean updatedBitmap = new AtomicBoolean(false);
        // The test 'RayTracingTest#testRenderSceneFromSDCardOB' starts to fail when using 15ms.
        final int waitInMillis = 10;

        final DrawView drawView = UtilsT.getPrivateField(activity, "drawView");
        final MainRenderer renderer = drawView.getRenderer();

        final int timeToWaitForUpdatedImageInMillis = 60 * 1000;
        // 20 seconds might not be enough for UiTest#testUI when Android emulator doesn´t have hardware acceleration enabled.
        final int timeToWaitInMillis = Objects.equals(expectedButtonText, Constants.STOP) ? 30 * 1000 : timeToWaitForUpdatedImageInMillis;

        // The test 'PreviewTest#testPreviewSceneOrthographicCamera' starts to fail when using 1ms and 4ms.
        // The test 'UiTest' starts to fail when using 0ms.
        // The test 'PreviewTest#testPreviewScenePerspectiveCamera' can fail when using 2ms with MacOS.
        // The test 'RayTracingTest#testRenderSceneFromSDCardOBJ' starts to fail when using 5ms.
        final int waitInMillisForBitmapUpdate = 2;
        for (int currentTimeMs = 0; currentTimeMs < timeToWaitInMillis && !done.get(); currentTimeMs += waitInMillis) {
            ViewActionWait.waitForBitmapUpdate(waitInMillisForBitmapUpdate);
            Espresso.onView(ViewMatchers.withId(R.id.renderButton))
                .inRoot(RootMatchers.isTouchable())
                .perform(new ViewActionWait<>(waitInMillis, R.id.renderButton))
                .check((view, exception) -> {
                    UtilsT.rethrowException(exception);
                    final Button renderButton = view.findViewById(R.id.renderButton);
                    final String renderButtonText = renderButton.getText().toString();
                    final State rendererState = renderer.getState();
                    if (Objects.equals(renderButtonText, expectedButtonText) && Arrays.asList(expectedStates).contains(rendererState)) {
                        done.set(true);
                        logger.info("waitUntil for button update success");
                        if (Objects.equals(expectedButtonText, Constants.STOP)) {
                            final Bitmap bitmap = UtilsT.getPrivateField(renderer, "bitmap");
                            final boolean bitmapSingleColor = UtilsT.isBitmapSingleColor(bitmap);
                            if (!bitmapSingleColor) {
                                updatedBitmap.set(true);
                                logger.info("waitUntil for bitmap update success");
                            }
                        }
                    }
            });
        }

        if (!done.get()) {
            final State rendererState = renderer.getState();
            final String errorMessage = "Test: " + testName + ", State: '" + rendererState.name() + "' (expecting " + Arrays.toString(expectedStates) + "), Expected button: '" + expectedButtonText + "'.";
            throw new TimeoutException("The Ray Tracing engine didn't reach the expected state in " + (float) (timeToWaitInMillis) / 1000 + " secs. " + errorMessage);
        }

        if (Objects.equals(expectedButtonText, Constants.STOP)) {
            logger.info("Waiting '" + timeToWaitForUpdatedImageInMillis + "'ms for Bitmap to contain some rendered pixels.");
            final long endTimeInstantMs = System.currentTimeMillis() + timeToWaitForUpdatedImageInMillis;
            long currentInstantMs;
            do {
                final Bitmap bitmap = UtilsT.getPrivateField(renderer, "bitmap");
                final boolean bitmapSingleColor = UtilsT.isBitmapSingleColor(bitmap);
                if (!bitmapSingleColor) {
                    updatedBitmap.set(true);
                    logger.info("waitUntil for bitmap update success");
                } else {
                    ViewActionWait.waitForBitmapUpdate(waitInMillisForBitmapUpdate);
                }
                currentInstantMs = System.currentTimeMillis();
            } while (currentInstantMs < endTimeInstantMs && !updatedBitmap.get());

            if (!updatedBitmap.get()) {
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
