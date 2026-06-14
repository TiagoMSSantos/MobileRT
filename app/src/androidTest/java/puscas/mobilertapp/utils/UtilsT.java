package puscas.mobilertapp.utils;

import android.app.UiAutomation;
import android.graphics.Bitmap;
import android.os.Build;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.common.base.Preconditions;

import org.junit.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Logger;

import java8.util.J8Arrays;
import puscas.mobilertapp.AbstractTest;
import puscas.mobilertapp.ConstantsAndroid;
import puscas.mobilertapp.DirectInteraction;
import puscas.mobilertapp.MainRenderer;
import puscas.mobilertapp.R;
import puscas.mobilertapp.ViewActionWait;
import puscas.mobilertapp.constants.Constants;
import puscas.mobilertapp.constants.ConstantsMethods;
import puscas.mobilertapp.constants.ConstantsUI;
import puscas.mobilertapp.constants.State;
import puscas.mobilertapp.exceptions.FailureException;

/**
 * Helper class which contains helper methods for the tests.
 */
public final class UtilsT {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(UtilsT.class.getSimpleName());

    /**
     * Private constructor to avoid creating instances.
     */
    private UtilsT() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Helper method that gets a private field from an {@link Object}.
     *
     * @param clazz     The {@link Object} to get the private field.
     * @param fieldName The name of the field to get.
     * @return The private field.
     * @implNote This method uses reflection to be able to get the private
     *           field from the {@link Object}.
     */
    @NonNull
    @SuppressWarnings({"unchecked"})
    public static <T> T getPrivateField(@NonNull final Object clazz,
                                        @NonNull final String fieldName) {
        final Field field;
        try {
            // Use reflection to access the private field.
            field = clazz.getClass().getDeclaredField(fieldName);
        } catch (final NoSuchFieldException ex) {
            throw new FailureException(ex);
        }
        Preconditions.checkNotNull(field, "field shouldn't be null");
        field.setAccessible(true); // Make the field public.

        final T privateField;
        try {
            privateField = (T) field.get(clazz);
        } catch (final IllegalAccessException ex) {
            throw new FailureException(ex);
        }
        Preconditions.checkNotNull(privateField, "privateField shouldn't be null");

        return privateField;
    }

    /**
     * Helper method that checks if a {@link Bitmap} contains valid values from
     * a rendered image.
     *
     * @param bitmap             The {@link Bitmap}.
     * @param expectedSameValues Whether the {@link Bitmap} should have have only
     *                           one color.
     */
    public static void assertRayTracingResultInBitmap(@NonNull final Bitmap bitmap,
                                                      final boolean expectedSameValues) {
        final boolean bitmapSingleColor = isBitmapSingleColor(bitmap);
        logger.info("Checking bitmap values.");
        Assert.assertEquals("The rendered image should have different values.",
            expectedSameValues, bitmapSingleColor
        );
    }

    /**
     * Whether the {@link Bitmap} only rendered one single color or not.
     *
     * @param bitmap The {@link Bitmap} to validate.
     * @return {@code true} if the {@link Bitmap} only rendered a single color, or {@code false} otherwise.
     */
    static boolean isBitmapSingleColor(@NonNull final Bitmap bitmap) {
        final int firstPixel = bitmap.getPixel(0, 0);
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        return J8Arrays.stream(pixels)
            .allMatch(pixel -> pixel == firstPixel);
    }

    /**
     * Helper method that clicks the Render {@link Button} 1 time,
     * so it starts the Ray Tracing engine.
     *
     * @param expectedSameValues Whether the {@link Button} should not change at all.
     */
    public static void startRendering(final boolean expectedSameValues) {
        logger.info("startRendering");
        ViewActionWait.waitForButtonUpdate(0);
        assertRenderButtonText(Constants.RENDER);
        ViewActionWait.waitForButtonUpdate(0);
        final String expectedText = expectedSameValues ? Constants.RENDER : Constants.STOP;
        // Click focus-free (performClick(), see DirectInteraction).
        DirectInteraction.clickButton(R.id.renderButton, expectedText, false, 10_000L);
        logger.info("startRendering" + ConstantsMethods.FINISHED);
    }

    /**
     * Helper method that clicks the Render {@link Button} 1 time,
     * so it stops the Ray Tracing engine.
     */
    public static void stopRendering() {
        logger.info("stopRendering");
        // Click focus-free (performClick(), see DirectInteraction).
        DirectInteraction.clickButton(R.id.renderButton, Constants.RENDER, false, 10_000L);

        // Wait for the app to stop completely.
        ViewActionWait.waitForButtonUpdate(0);

        assertRenderButtonText(Constants.RENDER);
        logger.info("stopRendering" + ConstantsMethods.FINISHED);
    }

    /**
     * Helper method that checks if the {@link State} of the Ray Tracing engine
     * is {@link State#IDLE} and if the {@link Bitmap} in the {@link MainRenderer}
     * is a valid one as expected.
     *
     * @param expectedSameValues Whether the {@link Bitmap} should have have only
     *                           one color.
     */
    public static void testStateAndBitmap(final boolean expectedSameValues) {
        logger.info("testStateAndBitmap: " + expectedSameValues);
        ViewActionWait.waitForBitmapUpdate(0);
        // Read the DrawView's renderer focus-free (see DirectInteraction).
        final MainRenderer renderer = DirectInteraction.readRenderer(R.id.drawLayout);
        Assert.assertNotNull("DrawView/renderer could not be resolved", renderer);
        Assert.assertTrue(
            "State is not the expected",
            renderer.getState() == State.IDLE || renderer.getState() == State.FINISHED
        );
        final Bitmap bitmap = getPrivateField(renderer, "bitmap");
        assertRayTracingResultInBitmap(bitmap, expectedSameValues);
        ViewActionWait.waitForButtonUpdate(0);
    }

    /**
     * Helper method that checks the text from the Render {@link Button}.
     *
     * @param expectedText The expected text shown in the {@link Button}.
     */
    public static void assertRenderButtonText(@NonNull final String expectedText) {
        logger.info("assertRenderButtonText: " + expectedText);
        // Read the button text focus-free (see DirectInteraction).
        final String actual = DirectInteraction.readText(R.id.renderButton);
        Assert.assertEquals(ConstantsAndroid.BUTTON_MESSAGE, expectedText, actual);
    }

    /**
     * Helper method that will execute a {@link Runnable} and will ignore any
     * {@link Exception} that might be thrown.
     * @implNote If an {@link Exception} is thrown, then it will just log
     * the message.
     *
     * @param method The {@link Runnable} to call.
     */
    public static void executeWithCatching(@NonNull final Runnable method) {
        try {
            method.run();
        } catch (final Exception ex) {
            logger.severe("Error: " + ex.getMessage() + "\nCause: " + ex.getCause());
        }
    }

    /**
    * Take a screenshot of the Android emulator showing the MobileRT rendered scene.
    * <p>
    * It stores the screenshot in the path: {@code /data/local/tmp/MobileRT/screenshots}.
    * <p>
    * The capture method is chosen per API because the {@link puscas.mobilertapp.DrawView}
    * {@code GLSurfaceView} layer is only reachable by a privileged reader:
    * <ul>
    *   <li><b>API &gt;= 21</b>: {@code screencap}, run through
    *       {@link puscas.mobilertapp.AbstractTest#runShellCommand(String)} which on these APIs uses
    *       {@code UiAutomation.executeShellCommand} (the shell uid), so it can read the
    *       SurfaceFlinger framebuffer and capture the full emulator screen including the
    *       {@code GLSurfaceView}. The app uid cannot ({@code SurfaceFlinger: Permission Denial: can't
    *       read framebuffer}), so {@code screencap} is not usable below API 21 where no shell-uid exec
    *       route exists.</li>
    *   <li><b>API 18-20</b>: {@link UiAutomation#takeScreenshot()} (added in API 18). It captures the
    *       window but misses the {@code GLSurfaceView} before API 26 (black {@code DrawView}); it is a
    *       best-effort fallback so the leg still produces an image.</li>
    *   <li><b>API &lt; 18</b>: no {@link UiAutomation}, so fall back to the rendered {@link Bitmap}
    *       held by the {@code DrawView}'s {@link MainRenderer} (the ray-traced output itself).</li>
    * </ul>
    * None of these fork a Dalvik {@code app_process} ({@code screencap} is a native binary, the others
    * are in-process), so the API &lt;= 18 'deadd00d' crash is not triggered.
    *
    * @param name The name of the screenshot.
    */
    public static void captureScreenshot(final String name) {
        final File path = new File(UtilsContext.getInternalStorageFilePath(), "MobileRT" + ConstantsUI.FILE_SEPARATOR + "screenshots");
        AbstractTest.runShellCommand("mkdir -p " + path.getAbsolutePath());
        final File imageFile = new File(path, name);
        final String imagePath = imageFile.getAbsolutePath();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // API >= 21: screencap runs as the shell uid (executeShellCommand) which can read the framebuffer.
            AbstractTest.runShellCommand("screencap -p " + imagePath);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // API 18-20: no shell-uid exec, screencap as app uid is denied; fall back to UiAutomation.
            captureViaUiAutomation(imageFile);
        } else {
            // API < 18: no UiAutomation; fall back to the rendered Bitmap held by the DrawView.
            captureRendererBitmap(imageFile);
        }

        if (imageFile.exists() && imageFile.length() > 1020L) {
            logger.info("Captured screenshot: " + imagePath);
        } else {
            final String errorMessage = "Failed to capture screenshot: '" + imagePath + "' imageFile.exists()=" + imageFile.exists() + " imageFile.length()=" + imageFile.length();
            logger.severe(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    /**
     * Captures the screen via {@link UiAutomation#takeScreenshot()} and writes it as a PNG.
     * Used on API 18-20 (the method exists since API 18) where {@code screencap} is not reachable.
     *
     * @param imageFile The destination PNG file.
     */
    private static void captureViaUiAutomation(@NonNull final File imageFile) {
        final UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        final Bitmap bitmap = uiAutomation.takeScreenshot();
        if (bitmap == null) {
            logger.severe("UiAutomation.takeScreenshot returned null");
            return;
        }
        writeBitmapAsPng(bitmap, imageFile);
    }

    /**
     * Captures the {@link MainRenderer}'s rendered {@link Bitmap} and writes it as a PNG.
     * Used on API &lt; 18 where neither {@code screencap} (app uid denied) nor {@link UiAutomation}
     * (added in API 18) is available; the ray-traced output is the best obtainable image there.
     *
     * @param imageFile The destination PNG file.
     */
    private static void captureRendererBitmap(@NonNull final File imageFile) {
        final MainRenderer renderer = DirectInteraction.readRenderer(R.id.drawLayout);
        if (renderer == null) {
            logger.severe("captureRendererBitmap: DrawView/renderer could not be resolved");
            return;
        }
        final Bitmap bitmap = getPrivateField(renderer, "bitmap");
        writeBitmapAsPng(bitmap, imageFile);
    }

    /**
     * Writes a {@link Bitmap} to a PNG file as the app uid (no {@code app_process} fork).
     *
     * @param bitmap    The {@link Bitmap} to write.
     * @param imageFile The destination PNG file.
     */
    private static void writeBitmapAsPng(@NonNull final Bitmap bitmap, @NonNull final File imageFile) {
        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        } catch (final IOException ex) {
            logger.severe("Failed to write screenshot PNG '" + imageFile.getAbsolutePath() + "': " + ex);
        }
    }

}
