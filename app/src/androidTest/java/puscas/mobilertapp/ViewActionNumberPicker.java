package puscas.mobilertapp;

import android.view.View;
import android.widget.NumberPicker;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.Contract;

import java.util.logging.Logger;

import javax.annotation.Nonnull;

import puscas.mobilertapp.utils.Utils;

import static puscas.mobilertapp.utils.ConstantsMethods.FINISHED;

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
    @Contract(pure = true)
    ViewActionNumberPicker(final int newValue) {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        this.newValue = newValue;

        Utils.handleInterruption(methodName);
    }

    @Nonnull
    @Override
    public final Matcher<View> getConstraints() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        Utils.handleInterruption(methodName);
        return ViewMatchers.isAssignableFrom(NumberPicker.class);
    }

    @Nonnull
    @Override
    public final String getDescription() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        Utils.handleInterruption(methodName);
        return "Set the value of a NumberPicker: " + this.newValue;
    }

    @Override
    public final void perform(@Nonnull final UiController uiController, final View view) {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        try {
            final NumberPicker numberPicker = (NumberPicker) view;
            uiController.loopMainThreadUntilIdle();
            numberPicker.setValue(this.newValue);
            numberPicker.computeScroll();
            uiController.loopMainThreadUntilIdle();
        } finally {
            Utils.handleInterruption(methodName);
        }

        LOGGER.info(methodName + FINISHED);
    }
}
