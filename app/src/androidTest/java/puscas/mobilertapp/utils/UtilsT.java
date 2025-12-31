package puscas.mobilertapp.utils;

import android.graphics.Bitmap;
import android.widget.Button;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.core.app.DeviceCapture;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.common.base.Preconditions;

import org.junit.Assert;

import java.io.File;
import java.io.FileOutputStream;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import java8.util.J8Arrays;
import puscas.mobilertapp.ConstantsAndroid;
import puscas.mobilertapp.DrawView;
import puscas.mobilertapp.MainRenderer;
import puscas.mobilertapp.R;
import puscas.mobilertapp.ViewActionButton;
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
        Espresso.onView(ViewMatchers.withId(R.id.renderButton))
            .inRoot(RootMatchers.isTouchable())
            .perform(new ViewActionButton(expectedSameValues ? Constants.RENDER : Constants.STOP, false));
        logger.info("startRendering" + ConstantsMethods.FINISHED);
    }

    /**
     * Helper method that clicks the Render {@link Button} 1 time,
     * so it stops the Ray Tracing engine.
     */
    public static void stopRendering() {
        logger.info("stopRendering");
        Espresso.onView(ViewMatchers.withId(R.id.renderButton))
            .inRoot(RootMatchers.isTouchable())
            .perform(new ViewActionButton(Constants.RENDER, false));

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
        Espresso.onView(ViewMatchers.withId(R.id.drawLayout))
            .inRoot(RootMatchers.isTouchable())
            .perform(new ViewActionWait<>(0, R.id.drawLayout))
            .check((view, exception) -> {
                rethrowException(exception);
                final DrawView drawView = (DrawView) view;
                final MainRenderer renderer = drawView.getRenderer();
                Assert.assertTrue(
                    "State is not the expected",
                    renderer.getState() == State.IDLE || renderer.getState() == State.FINISHED
                );
                final Bitmap bitmap = getPrivateField(renderer, "bitmap");
                assertRayTracingResultInBitmap(bitmap, expectedSameValues);
            });
        ViewActionWait.waitForButtonUpdate(0);
    }

    /**
     * Helper method that checks the text from the Render {@link Button}.
     *
     * @param expectedText The expected text shown in the {@link Button}.
     */
    public static void assertRenderButtonText(@NonNull final String expectedText) {
        logger.info("assertRenderButtonText: " + expectedText);
        Espresso.onView(ViewMatchers.withId(R.id.renderButton))
            .inRoot(RootMatchers.isTouchable())
            .check((view, exception) -> {
                rethrowException(exception);
                final Button renderButton = view.findViewById(R.id.renderButton);
                Assert.assertEquals(
                    ConstantsAndroid.BUTTON_MESSAGE,
                    expectedText,
                    renderButton.getText().toString()
                );
            });
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
        } catch (final RuntimeException ex) {
            logger.severe("Error: " + ex.getMessage() + "\nCause: " + ex.getCause());
        }
    }

    /**
     * Helper method to rethrow any possible {@link Exception}.
     *
     * @param exception The exception to throw.
     */
    public static void rethrowException(@Nullable final Exception exception) {
        if (exception != null) {
            throw new FailureException(exception);
        }
    }

    /**
    * Take a screenshot of MobileRT rendered scene.
    * <p>
    * It stores the screenshot in the path: {@code /data/local/tmp/MobileRT/screenshots}.
    *
    * @param name The name of the screenshot.
    */
    public static void captureScreenshot(final String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            final Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
            } else {
                bitmap = DeviceCapture.takeScreenshot();
            }
            final File path = new File(UtilsContext.getInternalStorageFilePath(InstrumentationRegistry.getInstrumentation().getTargetContext()), "MobileRT" + ConstantsUI.FILE_SEPARATOR + "screenshots");
            path.mkdirs();
            final File imageFile = new File(path, name);
            try (final FileOutputStream out = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (final Exception ex) {
                UtilsLogging.logException(ex, "UtilsT#captureScreenshot");
                throw new FailureException(ex);
            }
        } else {
            logger.info("Didn't capture screenshot because Android API " + Build.VERSION.SDK_INT + " is not supported yet.");
        }
    }

}
