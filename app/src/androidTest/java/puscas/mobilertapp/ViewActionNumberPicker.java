package puscas.mobilertapp;

import android.view.View;
import android.widget.NumberPicker;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.Contract;
import org.junit.jupiter.api.Assertions;
import puscas.mobilertapp.utils.ConstantsMethods;

/**
 * Auxiliary class which represents a {@link NumberPicker}.
 */
final class ViewActionNumberPicker implements ViewAction {

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
    @Contract(pure = true) ViewActionNumberPicker(final int newValue) {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        this.newValue = newValue;
    }

    @Nonnull
    @Override
    public final Matcher<View> getConstraints() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        return ViewMatchers.isAssignableFrom(NumberPicker.class);
    }

    @Nonnull
    @Override
    public final String getDescription() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        return "Set the value of a NumberPicker: " + this.newValue;
    }

    @Override
    public final void perform(@Nonnull final UiController uiController, final View view) {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        uiController.loopMainThreadUntilIdle();
        final NumberPicker numberPicker = (NumberPicker) view;
        numberPicker.setValue(this.newValue);
        uiController.loopMainThreadUntilIdle();
        Assertions.assertEquals(this.newValue, numberPicker.getValue(),
            "The setted value should be '" + this.newValue + "'");

        final String message = methodName + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }
}
