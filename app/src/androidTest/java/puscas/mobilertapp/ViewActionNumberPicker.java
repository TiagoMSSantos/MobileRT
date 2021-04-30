package puscas.mobilertapp;

import android.view.View;
import android.widget.NumberPicker;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Assertions;
import puscas.mobilertapp.utils.Utils;

/**
 * Auxiliary class which represents a {@link NumberPicker}.
 */
@Log
@RequiredArgsConstructor
public final class ViewActionNumberPicker implements ViewAction {

    /**
     * The value to be set in the {@link NumberPicker}.
     */
    private final int newValue;

    @NonNull
    @Override
    public final Matcher<View> getConstraints() {
        log.info("ViewActionNumberPicker#getConstraints");

        return ViewMatchers.isAssignableFrom(NumberPicker.class);
    }

    @NonNull
    @Override
    public final String getDescription() {
        log.info("ViewActionNumberPicker#getDescription");

        return "Set the value of a NumberPicker: " + this.newValue;
    }

    @Override
    public final void perform(@NonNull final UiController uiController, @NonNull final View view) {
        final NumberPicker numberPicker = (NumberPicker) view;
        numberPicker.setValue(this.newValue);
        Utils.executeWithCatching(() -> uiController.loopMainThreadForAtLeast(100L));
        Assertions.assertEquals(this.newValue, numberPicker.getValue(),
            "The setted value should be '" + this.newValue + "'");
    }

}
