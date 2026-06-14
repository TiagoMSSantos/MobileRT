package puscas.mobilertapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.common.util.concurrent.Uninterruptibles;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import puscas.mobilertapp.DirectInteraction;
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

        // Max wait for a render (shared by both poll loops). Slow on swiftshader/no-KVM macOS runners (NAIVE teapot ~384 rays/s); 180s covers it, loops exit as soon as done.
        final int timeToWaitForRenderCompletionInMillis = 180 * 1000;
        // 20 seconds might not be enough for UiTest#testUI when Android emulator doesn´t have hardware acceleration enabled.
        final int timeToWaitInMillis = Objects.equals(expectedButtonText, Constants.STOP) ? 30 * 1000 : timeToWaitForRenderCompletionInMillis;

        // The test 'PreviewTest#testPreviewSceneOrthographicCamera' starts to fail when using 1ms and 4ms.
        // The test 'UiTest' starts to fail when using 0ms.
        // The test 'PreviewTest#testPreviewScenePerspectiveCamera' can fail when using 2ms with MacOS.
        // The test 'RayTracingTest#testRenderSceneFromSDCardOBJ' starts to fail when using 5ms.
        final int waitInMillisForBitmapUpdate = 2;
        // Self-healing re-click: on slow software-GL macOS API 21 the executor render-start can throw
        // (GL surface not ready after an activity relaunch) and reset the engine to IDLE, so BUSY is
        // never reached. When the start is expected (BUSY) and the engine sits back at IDLE past a grace
        // window, re-issue the click. Guarded on IDLE so MainActivity#startRender always takes the START
        // branch (never toggles off a live render); bounded so a genuinely stuck leg still fails fast.
        final boolean expectingBusy = Arrays.asList(expectedStates).contains(State.BUSY);
        final long reclickGraceMs = 5_000L;
        final int maxReclicks = 3;
        int reclicks = 0;
        long lastClickInstantMs = System.currentTimeMillis();
        // Wall-clock deadline, not an iteration count: each Espresso call takes ~190ms on API 26.
        final long forLoopEndInstantMs = System.currentTimeMillis() + timeToWaitInMillis;
        while (System.currentTimeMillis() < forLoopEndInstantMs && !done.get()) {
            // Let the activity settle before reading the button below.
            ViewActionWait.waitForBitmapUpdate(waitInMillisForBitmapUpdate);
            // Read button text focus-free (see DirectInteraction).
            final String renderButtonText = DirectInteraction.readText(R.id.renderButton);
            evaluateButtonState(renderButtonText, renderer.getState(), expectedButtonText,
                expectedStates, renderer, done, updatedBitmap);
            if (!done.get()) {
                if (expectingBusy && reclicks < maxReclicks && renderer.getState() == State.IDLE
                        && System.currentTimeMillis() - lastClickInstantMs > reclickGraceMs) {
                    reclicks++;
                    logger.warning("waitUntil: render-start did not stick (state IDLE); re-clicking render button (attempt " + reclicks + '/' + maxReclicks + ").");
                    DirectInteraction.clickButton(R.id.renderButton, null, false, 0L);
                    lastClickInstantMs = System.currentTimeMillis();
                }
                Uninterruptibles.sleepUninterruptibly(waitInMillis, TimeUnit.MILLISECONDS);
            }
        }

        if (!done.get()) {
            final State rendererState = renderer.getState();
            final String errorMessage = "Test: " + testName + ", State: '" + rendererState.name() + "' (expecting " + Arrays.toString(expectedStates) + "), Expected button: '" + expectedButtonText + "'.";
            throw new TimeoutException("The Ray Tracing engine didn't reach the expected state in " + (float) (timeToWaitInMillis) / 1000 + " secs. " + errorMessage);
        }

        if (Objects.equals(expectedButtonText, Constants.STOP)) {
            logger.info("Waiting '" + timeToWaitForRenderCompletionInMillis + "'ms for Bitmap to contain some rendered pixels.");
            final long endTimeInstantMs = System.currentTimeMillis() + timeToWaitForRenderCompletionInMillis;
            // Check the deadline at the top of every iteration so a slow getState() can't overshoot.
            while (System.currentTimeMillis() < endTimeInstantMs && !updatedBitmap.get()) {
                final Bitmap bitmap = UtilsT.getPrivateField(renderer, "bitmap");
                final boolean bitmapSingleColor = UtilsT.isBitmapSingleColor(bitmap);
                if (!bitmapSingleColor) {
                    updatedBitmap.set(true);
                    logger.info("waitUntil for bitmap update success");
                } else if (renderer.getState() == State.FINISHED) {
                    // GL preview renders to the framebuffer, not the Java Bitmap; FINISHED = done.
                    updatedBitmap.set(true);
                    logger.info("waitUntil for bitmap update success (state FINISHED)");
                } else {
                    Uninterruptibles.sleepUninterruptibly(waitInMillisForBitmapUpdate, TimeUnit.MILLISECONDS);
                }
            }

            if (!updatedBitmap.get()) {
                final Bitmap bitmap = UtilsT.getPrivateField(renderer, "bitmap");
                final boolean bitmapSingleColor = UtilsT.isBitmapSingleColor(bitmap);
                final String errorMessage = "Test: " + testName + ", State: '" + renderer.getState().name() + "', Bitmap had no pixel rendered: " + bitmapSingleColor + ".";
                throw new TimeoutException("The Ray Tracing engine didn't reach the expected state in " + (float) (timeToWaitForRenderCompletionInMillis) / 1000 + " secs. " + errorMessage);
            }
        }

        logger.info("waitUntil finished");
    }

    /** Evaluates whether the render {@link Button} text and renderer {@link State} reached the expected combination, updating {@code done} / {@code updatedBitmap}. */
    private static void evaluateButtonState(final String renderButtonText,
                                            final State rendererState,
                                            final String expectedButtonText,
                                            final State[] expectedStates,
                                            @NonNull final MainRenderer renderer,
                                            final AtomicBoolean done,
                                            final AtomicBoolean updatedBitmap) {
        // STOP+BUSY: the button showing "Stop" is enough proof the render started (end-state validated later by testStateAndBitmap). Also accept RENDER+FINISHED for fast GL renders.
        final boolean waitingForStop = Constants.STOP.equals(expectedButtonText);
        final boolean exactMatch = Objects.equals(renderButtonText, expectedButtonText)
            && (waitingForStop || Arrays.asList(expectedStates).contains(rendererState));
        final boolean fastRenderCompleted = Constants.STOP.equals(expectedButtonText)
            && Constants.RENDER.equals(renderButtonText)
            && rendererState == State.FINISHED;
        if (exactMatch || fastRenderCompleted) {
            done.set(true);
            logger.info("waitUntil for button update success");
            if (Objects.equals(expectedButtonText, Constants.STOP)) {
                if (fastRenderCompleted) {
                    // GL stopped: no happens-before for GL-thread bitmap writes; testStateAndBitmap() validates.
                    updatedBitmap.set(true);
                    logger.info("waitUntil for bitmap update success (fast render)");
                } else {
                    final Bitmap bitmap = UtilsT.getPrivateField(renderer, "bitmap");
                    final boolean bitmapSingleColor = UtilsT.isBitmapSingleColor(bitmap);
                    if (!bitmapSingleColor) {
                        updatedBitmap.set(true);
                        logger.info("waitUntil for bitmap update success");
                    }
                }
            }
        }
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
