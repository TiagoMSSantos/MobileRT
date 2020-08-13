package puscas.mobilertapp;

import android.view.View;
import android.widget.NumberPicker;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Assertions;
import puscas.mobilertapp.utils.ConstantsMethods;

/**
 * Auxiliary class which represents a {@link NumberPicker}.
 */
public final class ViewActionNumberPicker implements ViewAction {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER =
        Logger.getLogger(ViewActionNumberPicker.class.getName());

    /**
     * The value to be set in the {@link NumberPicker}.
     */
    private final int newValue;

    /**
     * The constructor for this class.
     *
     * @param newValue The value for the {@link NumberPicker}.
     */
    public ViewActionNumberPicker(final int newValue) {
        LOGGER.info("ViewActionNumberPicker");

        this.newValue = newValue;
    }

    @Nonnull
    @Override
    public final Matcher<View> getConstraints() {
        LOGGER.info("ViewActionNumberPicker#getConstraints");

        return ViewMatchers.isAssignableFrom(NumberPicker.class);
    }

    @Nonnull
    @Override
    public final String getDescription() {
        LOGGER.info("ViewActionNumberPicker#getDescription");

        return "Set the value of a NumberPicker: " + this.newValue;
    }

    @Override
    public final void perform(@Nonnull final UiController uiController, final View view) {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final NumberPicker numberPicker = (NumberPicker) view;
        numberPicker.setValue(this.newValue);
        uiController.loopMainThreadUntilIdle();
        Assertions.assertEquals(this.newValue, numberPicker.getValue(),
            "The setted value should be '" + this.newValue + "'");

        final String message = methodName + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }

}
