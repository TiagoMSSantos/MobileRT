package puscas.mobilertapp.configs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.ImageButton;
import android.widget.TextView;

import org.assertj.core.api.Assertions;
import org.easymock.EasyMock;
import org.junit.Test;

import puscas.mobilertapp.constants.CustomNumberPicker;
import puscas.mobilertapp.exceptions.FailureException;

/**
 * The test suite for the {@link CustomNumberPicker} util class.
 */
public final class CustomNumberPickerTest {

    /**
     * The partial mocked {@link CustomNumberPicker} to be used by the tests.
     */
    final private ViewManager targetCustomNumberPicker = new CustomNumberPicker(EasyMock.mock(Context.class), EasyMock.mock(AttributeSet.class));

    /**
     * Tests that the {@link CustomNumberPicker#addView(View, ViewGroup.LayoutParams)}
     * method throws an exception when provided a {@link View}.
     */
    @Test
    public void testAddViewWithWrongView() {
        final View viewMocked = EasyMock.createNiceMock(View.class);

        EasyMock.replay(viewMocked);
        Assertions.assertThatThrownBy(() -> this.targetCustomNumberPicker.addView(viewMocked, EasyMock.mock(ViewGroup.LayoutParams.class)))
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
        final View viewMocked = EasyMock.createNiceMock(TextView.class);

        EasyMock.replay(viewMocked);
        Assertions.assertThatCode(() -> this.targetCustomNumberPicker.addView(viewMocked, EasyMock.mock(ViewGroup.LayoutParams.class)))
            .as("ViewManager#addView shouldn't throw any exception.")
            .doesNotThrowAnyException();
    }

    /**
     * Tests that the {@link CustomNumberPicker#addView(View, ViewGroup.LayoutParams)}
     * method doesn't throw any exception when providing a valid {@link ImageButton}.
     * <p>
     * This test is just to validate that the {@link CustomNumberPicker} is compatible with
     * Android API 15 or older.
     */
    @Test
    public void testAddViewWithImageButton() {
        final View viewMocked = EasyMock.createNiceMock(ImageButton.class);

        EasyMock.replay(viewMocked);
        Assertions.assertThatCode(() -> this.targetCustomNumberPicker.addView(viewMocked, EasyMock.mock(ViewGroup.LayoutParams.class)))
                .as("ViewManager#addView shouldn't throw any exception.")
                .doesNotThrowAnyException();
    }

}
