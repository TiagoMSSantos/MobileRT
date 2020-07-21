package puscas.mobilertapp;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.multidex.BuildConfig;

import org.junit.Assume;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Helper class which contains helper methods for the tests.
 */
final class Utils {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());

    /**
     * Private method to avoid instantiating this helper class.
     */
    private Utils () {
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

        LOGGER.info("BuildConfig.DEBUG: " + BuildConfig.DEBUG);
        LOGGER.info("Build.TAGS: " + Build.TAGS);
        LOGGER.info("numCores: " + numCores);
        Assume.assumeFalse(
            "This test fails in Debug with only 1 core.",
            BuildConfig.DEBUG // Debug mode
                && Build.TAGS.equals("test-keys") // In third party systems (CI)
                && numCores == 1 // Android system with only 1 CPU core
        );
        LOGGER.info(methodName + " finish");
    }

    /**
     * Helper method that gets a private field from an {@link Object}.
     *
     * @param clazz The {@link Object} to get the private field.
     * @return The {@link Bitmap} from the {@link MainRenderer}.
     *
     * @implNote This method uses reflection to be able to get the private
     * field from the {@link Object}.
     */
    @NonNull
    static <T> T getPrivateField(@Nonnull final Object clazz, @NonNull final String fieldName) {
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
     * @param clazz The {@link Object} to invoke the private method.
     * @return The return value from the private method.
     *
     * @implNote This method uses reflection to be able to invoke the private
     * method from the {@link Object}.
     */
    @NonNull
    static <T> T invokePrivateMethod(
        @Nonnull final Object clazz, @NonNull final String methodName,
        @NonNull final List<Class<?>> parameterTypes, @NonNull final Collection<Object> args
    ) {
        Method method = null;
        try {
            // Use reflection to access the private method.
            method = clazz.getClass().getDeclaredMethod(methodName, parameterTypes.toArray(new Class<?>[0]));
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
}
