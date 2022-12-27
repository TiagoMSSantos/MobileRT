package puscas.mobilertapp;

import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;
import org.junit.Assert;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import puscas.mobilertapp.utils.UtilsT;

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
    public Matcher<View> getConstraints() {
        log.info("ViewActionNumberPicker#getConstraints");

        return ViewMatchers.isAssignableFrom(NumberPicker.class);
    }

    @NonNull
    @Override
    public String getDescription() {
        log.info("ViewActionNumberPicker#getDescription");

        return "Set the value of a NumberPicker: " + this.newValue;
    }

    @Override
    public void perform(@NonNull final UiController uiController, @NonNull final View view) {
        final NumberPicker numberPicker = (NumberPicker) view;
        numberPicker.setValue(this.newValue);
        UtilsT.executeWithCatching(() -> uiController.loopMainThreadForAtLeast(100L));
        Assert.assertEquals("The set value should be '" + this.newValue + "'", this.newValue, numberPicker.getValue());
    }

}
