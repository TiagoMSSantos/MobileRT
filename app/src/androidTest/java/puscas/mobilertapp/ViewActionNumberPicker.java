package puscas.mobilertapp;

import android.view.View;
import android.widget.NumberPicker;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.logging.Logger;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Assertions;
import puscas.mobilertapp.utils.ConstantsMethods;
import puscas.mobilertapp.utils.Utils;

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

    @NonNull
    @Override
    public final Matcher<View> getConstraints() {
        LOGGER.info("ViewActionNumberPicker#getConstraints");

        return ViewMatchers.isAssignableFrom(NumberPicker.class);
    }

    @NonNull
    @Override
    public final String getDescription() {
        LOGGER.info("ViewActionNumberPicker#getDescription");

        return "Set the value of a NumberPicker: " + this.newValue;
    }

    @Override
    public final void perform(@NonNull final UiController uiController, @NonNull final View view) {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final NumberPicker numberPicker = (NumberPicker) view;
        numberPicker.setValue(this.newValue);
        Utils.executeWithCatching(() -> uiController.loopMainThreadForAtLeast(100L));
        Assertions.assertEquals(this.newValue, numberPicker.getValue(),
            "The setted value should be '" + this.newValue + "'");

        final String message = methodName + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }

}
