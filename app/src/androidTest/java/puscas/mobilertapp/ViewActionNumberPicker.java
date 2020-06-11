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
        this.newValue = newValue;
        Thread.interrupted();
    }

    @Override
    @Nonnull
    public final Matcher<View> getConstraints() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
        Thread.interrupted();

        return ViewMatchers.isAssignableFrom(NumberPicker.class);
    }

    @Override
    @Nonnull
    public final String getDescription() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
        Thread.interrupted();

        return "Set the value of a NumberPicker: " + this.newValue;
    }

    @Override
    public final void perform(@Nonnull final UiController uiController, final View view) {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        try {
            final NumberPicker numberPicker = (NumberPicker) view;
            LOGGER.info(methodName + " 1");
            uiController.loopMainThreadUntilIdle();
            LOGGER.info(methodName + " 2");
            numberPicker.setValue(this.newValue);
            LOGGER.info(methodName + " 3");
            numberPicker.computeScroll();
            LOGGER.info(methodName + " 4");
            uiController.loopMainThreadUntilIdle();
        } finally {
            LOGGER.info("Reset interrupted.");
            Thread.interrupted();
        }

        LOGGER.info(methodName + " finished");
    }
}
