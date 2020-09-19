package puscas.mobilertapp.utils;

import android.graphics.Bitmap;
import android.os.Build;
import android.widget.Button;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java8.util.J8Arrays;
import javax.annotation.Nonnull;
import org.junit.Assume;
import org.junit.jupiter.api.Assertions;
import puscas.mobilertapp.BuildConfig;
import puscas.mobilertapp.DrawView;
import puscas.mobilertapp.MainRenderer;
import puscas.mobilertapp.R;
import puscas.mobilertapp.ViewActionButton;

/**
 * Helper class which contains helper methods for the tests.
 */
public final class UtilsTest {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(UtilsTest.class.getName());

    /**
     * Private method to avoid instantiating this helper class.
     */
    private UtilsTest() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Helper method that checks if the current system should or not execute the
     * flaky tests.
     *
     * @param numCores The number of CPU cores available.
     */
    static void checksIfSystemShouldContinue(final int numCores) {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final String messageDebug = "BuildConfig.DEBUG: " + BuildConfig.DEBUG;
        LOGGER.info(messageDebug);
        final String messageTags = "Build.TAGS: " + Build.TAGS;
        LOGGER.info(messageTags);
        final String messageNumCores = "numCores: " + numCores;
        LOGGER.info(messageNumCores);
        Assume.assumeFalse(
            "This test fails in Debug with only 1 core.",
            BuildConfig.DEBUG // Debug mode
                && Build.TAGS.equals("test-keys") // In third party systems (CI)
                && numCores == 1 // Android system with only 1 CPU core
        );

        final String message = methodName + ConstantsMethods.FINISHED;
        LOGGER.info(message);
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
    @Nonnull
    public static <T> T getPrivateField(@Nonnull final Object clazz,
                                        @Nonnull final String fieldName) {
        Field field = null;
        try {
            // Use reflection to access the private field.
            field = clazz.getClass().getDeclaredField(fieldName);
        } catch (final NoSuchFieldException ex) {
            LOGGER.warning(ex.getMessage());
        }
        assert field != null;
        field.setAccessible(true); // Make the field public.

        T privateField = null;
        try {
            privateField = (T) field.get(clazz);
        } catch (final IllegalAccessException ex) {
            LOGGER.warning(ex.getMessage());
        }
        assert privateField != null;

        return privateField;
    }

    /**
     * Helper method that invokes a private method from an {@link Object}.
     *
     * @param clazz          The {@link Object} to invoke the private method.
     * @param methodName     The name of the method to invoke.
     * @param parameterTypes The types of the parameters of the method.
     * @param args           The arguments to pass to the method.
     * @return The return value from the private method.
     * @implNote This method uses reflection to be able to invoke the private
     *           method from the {@link Object}.
     */
    @Nonnull
    static <T> T invokePrivateMethod(@Nonnull final Object clazz,
                                     @Nonnull final String methodName,
                                     @Nonnull final List<Class<?>> parameterTypes,
                                     @Nonnull final Collection<Object> args) {
        Method method = null;
        try {
            // Use reflection to access the private method.
            method = clazz.getClass()
                .getDeclaredMethod(methodName, parameterTypes.toArray(new Class<?>[0]));
        } catch (final NoSuchMethodException ex) {
            LOGGER.warning(ex.getMessage());
        }
        assert method != null;
        method.setAccessible(true); // Make the method public.

        T privateMethodReturnValue = null;
        try {
            privateMethodReturnValue = (T) method.invoke(clazz, args.toArray(new Object[0]));
        } catch (final IllegalAccessException ex) {
            LOGGER.warning(ex.getMessage());
        } catch (final InvocationTargetException ex) {
            LOGGER.warning(Objects.requireNonNull(ex.getCause()).getMessage());
        }
        assert privateMethodReturnValue != null;

        return privateMethodReturnValue;
    }

    /**
     * Helper method that checks if a {@link Bitmap} contains valid values from
     * a rendered image.
     *
     * @param bitmap             The {@link Bitmap}.
     * @param expectedSameValues Whether the {@link Bitmap} should have have only
     *                           one color.
     */
    private static void assertRayTracingResultInBitmap(@Nonnull final Bitmap bitmap,
                                                       final boolean expectedSameValues) {
        final int firstPixel = bitmap.getPixel(0, 0);
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        final boolean bitmapSameColor = J8Arrays.stream(pixels)
            .allMatch(pixel -> pixel == firstPixel);

        LOGGER.info("Checking bitmap values.");
        Assertions.assertEquals(expectedSameValues, bitmapSameColor,
            "The rendered image should have different values."
        );
    }

    /**
     * Helper method that clicks the Render {@link Button} 1 time,
     * so it starts the Ray Tracing engine.
     *
     * @return The {@link ViewInteraction} for the Render {@link Button}.
     */
    @Nonnull
    public static ViewInteraction startRendering() {
        LOGGER.info("startRendering");
        UtilsTest.assertRenderButtonText(Constants.RENDER);
        return Espresso.onView(ViewMatchers.withId(R.id.renderButton))
            .perform(new ViewActionButton(Constants.STOP));
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
        LOGGER.info("testBitmap");
        Espresso.onView(ViewMatchers.withId(R.id.drawLayout))
            .check((view, exception) -> {
                final DrawView drawView = (DrawView) view;
                final MainRenderer renderer = drawView.getRenderer();
                final Bitmap bitmap = UtilsTest.getPrivateField(renderer, "bitmap");
                UtilsTest.assertRayTracingResultInBitmap(bitmap, expectedSameValues);

                Assertions.assertTrue(
                    renderer.getState() == State.IDLE || renderer.getState() == State.FINISHED,
                    "State is not the expected"
                );
            });
    }

    /**
     * Helper method that checks the text from the Render {@link Button}.
     *
     * @param expectedText The expected text shown in the {@link Button}.
     */
    public static void assertRenderButtonText(@Nonnull final String expectedText) {
        LOGGER.info("assertRenderButtonText");
        Espresso.onView(ViewMatchers.withId(R.id.renderButton))
            .check((view, exception) -> {
                final Button renderButton = view.findViewById(R.id.renderButton);
                Assertions.assertEquals(
                    expectedText,
                    renderButton.getText().toString(),
                    puscas.mobilertapp.Constants.BUTTON_MESSAGE
                );
            });
    }

}
