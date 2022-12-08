package puscas.mobilertapp;

import com.google.common.base.Preconditions;

import java.lang.reflect.Field;

import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;

/**
 * Helper class which contains helper methods for the tests.
 */
@UtilityClass
@Log
public final class UtilsT {

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
    public static <T> T getPrivateField(@NonNull final Object clazz,
                                        @NonNull final String fieldName) {
        Field field = null;
        try {
            // Use reflection to access the private field.
            field = clazz.getClass().getDeclaredField(fieldName);
        } catch (final NoSuchFieldException ex) {
            log.warning(ex.getMessage());
        }
        Preconditions.checkNotNull(field, "field shouldn't be null");
        field.setAccessible(true); // Make the field public.

        T privateField = null;
        try {
            privateField = (T) field.get(clazz);
        } catch (final IllegalAccessException ex) {
            log.warning(ex.getMessage());
        }
        Preconditions.checkNotNull(privateField, "privateField shouldn't be null");

        return privateField;
    }

}
