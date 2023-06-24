package puscas.mobilertapp;

import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;
import org.junit.Assert;

import java.util.logging.Logger;

import puscas.mobilertapp.utils.UtilsT;

/**
 * Auxiliary class which represents a {@link NumberPicker}.
 */
public final class ViewActionNumberPicker implements ViewAction {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(ViewActionNumberPicker.class.getSimpleName());

    /**
     * The value to be set in the {@link NumberPicker}.
     */
    private final int newValue;

    /**
     * The constructor.
     *
     * @param newValue The value to be set in the {@link NumberPicker}.
     */
    public ViewActionNumberPicker(final int newValue) {
        this.newValue = newValue;
    }

    @NonNull
    @Override
    public Matcher<View> getConstraints() {
        logger.info("ViewActionNumberPicker#getConstraints");

        return ViewMatchers.isAssignableFrom(NumberPicker.class);
    }

    @NonNull
    @Override
    public String getDescription() {
        logger.info("ViewActionNumberPicker#getDescription");

        return "Set the value of a NumberPicker: " + this.newValue;
    }

    @Override
    public void perform(@NonNull final UiController uiController, @NonNull final View view) {
        final NumberPicker numberPicker = (NumberPicker) view;
        numberPicker.setValue(this.newValue);
        UtilsT.executeWithCatching(uiController::loopMainThreadUntilIdle);
        Assert.assertEquals("The set value should be '" + this.newValue + "'", this.newValue, numberPicker.getValue());
    }

}
