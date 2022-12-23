package puscas.mobilertapp;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.lang.reflect.Field;

import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;
import puscas.mobilertapp.exceptions.FailureException;

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
    @SuppressWarnings("unchecked")
    public static <T> T getPrivateField(@NonNull final Object clazz,
                                        @NonNull final String fieldName) {
        Field field = null;
        try {
            // Use reflection to access the private field.
            field = clazz.getClass().getDeclaredField(fieldName);
        } catch (final NoSuchFieldException ex) {
            throw new FailureException(ex);
        }
        Preconditions.checkNotNull(field, "field shouldn't be null");
        field.setAccessible(true); // Make the field public.

        T privateField = null;
        try {
            privateField = (T) field.get(clazz);
        } catch (final IllegalAccessException ex) {
            throw new FailureException(ex);
        }
        Preconditions.checkNotNull(privateField, "privateField shouldn't be null");

        return privateField;
    }

    /**
     * Helper method that sets a private field in an {@link Object}.
     *
     * @param clazz     The {@link Object} to set the private field.
     * @param fieldName The name of the field to get.
     * @param newValue  The new value to be set.
     * @implNote This method uses reflection to be able to get the private
     *           field from the {@link Object}.
     */
    public static void setPrivateField(@NonNull final Object clazz,
                                       @NonNull final String fieldName,
                                       @NonNull final Object newValue) {
        Field field = null;
        try {
            // Use reflection to access the private field.
            field = clazz.getClass().getDeclaredField(fieldName);
        } catch (final NoSuchFieldException ex) {
            throw new FailureException(ex);
        }
        Preconditions.checkNotNull(field, "field shouldn't be null");
        field.setAccessible(true); // Make the field public.

        try {
            field.set(clazz, newValue);
        } catch (final IllegalAccessException ex) {
            throw new FailureException(ex);
        }
    }

}
