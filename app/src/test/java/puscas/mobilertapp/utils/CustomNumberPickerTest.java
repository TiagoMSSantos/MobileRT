package puscas.mobilertapp.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.Contract;
import org.junit.Test;

import puscas.mobilertapp.MainActivity;
import puscas.mobilertapp.constants.CustomNumberPicker;
import puscas.mobilertapp.exceptions.FailureException;

/**
 * The test suite for the {@link CustomNumberPicker} util class.
 */
public final class CustomNumberPickerTest {

    /**
     * Helper method that creates an {@link AttributeSet}.
     *
     * @return An {@link AttributeSet}.
     */
    @NonNull
    @Contract(pure = true)
    private static AttributeSet createAttributeSet() {
        return new AttributeSet() {
            @Contract(pure = true)
            @Override
            public int getAttributeCount() {
                return 0;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public String getAttributeName(final int index) {
                return null;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public String getAttributeValue(final int index) {
                return null;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public String getAttributeValue(final String namespace, final String name) {
                return null;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public String getPositionDescription() {
                return null;
            }

            @Contract(pure = true)
            @Override
            public int getAttributeNameResource(final int index) {
                return 0;
            }

            @Contract(pure = true)
            @Override
            public int getAttributeListValue(final String namespace, final String attribute,
                                             final String[] options, final int defaultValue) {
                return 0;
            }

            @Contract(pure = true)
            @Override
            public boolean getAttributeBooleanValue(final String namespace, final String attribute,
                                                    final boolean defaultValue) {
                return false;
            }

            @Contract(pure = true)
            @Override
            public int getAttributeResourceValue(final String namespace, final String attribute,
                                                 final int defaultValue) {
                return 0;
            }

            @Contract(pure = true)
            @Override
            public int getAttributeIntValue(final String namespace, final String attribute,
                                            final int defaultValue) {
                return 0;
            }

            @Contract(pure = true)
            @Override
            public int getAttributeUnsignedIntValue(final String namespace, final String attribute,
                                                    final int defaultValue) {
                return 0;
            }

            @Contract(pure = true)
            @Override
            public float getAttributeFloatValue(final String namespace, final String attribute,
                                                final float defaultValue) {
                return 0.0F;
            }

            @Contract(pure = true)
            @Override
            public int getAttributeListValue(final int index, final String[] options,
                                             final int defaultValue) {
                return 0;
            }

            @Contract(pure = true)
            @Override
            public boolean getAttributeBooleanValue(final int index, final boolean defaultValue) {
                return false;
            }

            @Contract(pure = true)
            @Override
            public int getAttributeResourceValue(final int index, final int defaultValue) {
                return 0;
            }

            @Contract(pure = true)
            @Override
            public int getAttributeIntValue(final int index, final int defaultValue) {
                return 0;
            }

            @Contract(pure = true)
            @Override
            public int getAttributeUnsignedIntValue(final int index, final int defaultValue) {
                return 0;
            }

            @Contract(pure = true)
            @Override
            public float getAttributeFloatValue(final int index, final float defaultValue) {
                return 0.0F;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public String getIdAttribute() {
                return null;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public String getClassAttribute() {
                return null;
            }

            @Contract(pure = true)
            @Override
            public int getIdAttributeResourceValue(final int defaultValue) {
                return 0;
            }

            @Contract(pure = true)
            @Override
            public int getStyleAttribute() {
                return 0;
            }
        };
    }

    /**
     * Tests that the {@link CustomNumberPicker#addView(View, ViewGroup.LayoutParams)}
     * method throws an exception when provided a {@link View}.
     */
    @Test
    public void testAddViewWithWrongView() {
        final Context context = new MainActivity();
        final AttributeSet attrs = createAttributeSet();
        final View view = new View(context);
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(context, attrs);

        final NumberPicker customNumberPicker = new CustomNumberPicker(context, attrs);

        Assertions.assertThat(customNumberPicker)
            .as("CustomNumberPicker shouldn't be null.")
            .isNotNull();

        Assertions.assertThatThrownBy(() -> customNumberPicker.addView(view, params))
            .as("Assertions#assertThatThrownBy should throw an exception.")
            .isInstanceOf(FailureException.class)
            .hasMessageContaining("View cannot be cast to TextView.");
    }

    /**
     * Tests that the {@link CustomNumberPicker#addView(View, ViewGroup.LayoutParams)}
     * method doesn't throw any exception when providing a valid {@link TextView}.
     */
    @Test
    public void testAddViewWithTextView() {
        final Context context = new MainActivity();
        final AttributeSet attrs = createAttributeSet();
        final TextView view = new TextView(context);
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(context, attrs);

        final ViewManager customNumberPicker = new CustomNumberPicker(context, attrs);

        Assertions.assertThat(customNumberPicker)
            .as("ViewManager shouldn't be null.")
            .isNotNull();

        Assertions.assertThatCode(() -> customNumberPicker.addView(view, params))
            .as("ViewManager#addView shouldn't throw any exception.")
            .doesNotThrowAnyException();
    }

}
