package puscas.mobilertapp;

import android.view.View;
import android.widget.Button;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import com.google.common.util.concurrent.Uninterruptibles;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.junit.jupiter.api.Assertions;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import puscas.mobilertapp.utils.Utils;

import static puscas.mobilertapp.utils.ConstantsMethods.FINISHED;

/**
 * Auxiliary class which represents the render {@link Button}.
 */
final class ViewActionButton implements ViewAction {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER =
        Logger.getLogger(ViewActionButton.class.getName());

    /**
     * The global counter of how many times the button was clicked.
     */
    private static long clickCounter = 0L;

    /**
     * The expected text for the {@link Button}.
     */
    @NonNls private final String expectedText;

    /**
     * The constructor for this class.
     */
    @Contract(pure = true)
    ViewActionButton(final String expectedText) {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
        Utils.handleInterruption(methodName);

        this.expectedText = expectedText;
    }

    @Nonnull
    @Override
    public final Matcher<View> getConstraints() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
        Utils.handleInterruption(methodName);

        return ViewMatchers.isAssignableFrom(Button.class);
    }

    @Nonnull
    @Override
    public final String getDescription() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
        Utils.handleInterruption(methodName);

        return "Click button";
    }

    @Override
    public final void perform(@Nonnull final UiController uiController, @Nonnull final View view) {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName + "(" + this.expectedText + ")");

        try {
            final Button button = (Button) view;
            LOGGER.info(methodName + " waiting");

            boolean textBeforeNotExpected = button.getText().toString().equals(this.expectedText);
            while (textBeforeNotExpected) {
                uiController.loopMainThreadForAtLeast(5000L);
                textBeforeNotExpected = button.getText().toString().equals(this.expectedText);
                LOGGER.info(methodName +
                    " waiting button to NOT have '" + this.expectedText + "' written!!!");
            }

            uiController.loopMainThreadUntilIdle();
            LOGGER.info(methodName + " clicking button");
            boolean buttonNotClickedProperly = !button.performClick();
            ++clickCounter;
            LOGGER.info(methodName + " BUTTON CLICKED: " + clickCounter +
                " (" + this.expectedText + ")");
            while (buttonNotClickedProperly) {
                LOGGER.info(methodName + " waiting to click button!!!");
                uiController.loopMainThreadForAtLeast(5000L);
                buttonNotClickedProperly = !button.performClick();
                ++clickCounter;
                LOGGER.info(methodName + " BUTTON CLICKED: " + clickCounter +
                    " (" + this.expectedText + ")");
            }

            boolean textEqualsNotExpected = !button.getText().toString().equals(this.expectedText);
            final long advanceSecs = 5L;
            for (long currentTimeSecs = 0L; currentTimeSecs < 60L && textEqualsNotExpected;
                 currentTimeSecs += advanceSecs) {
                uiController.loopMainThreadForAtLeast(advanceSecs * 1000L);
                Uninterruptibles.sleepUninterruptibly(advanceSecs, TimeUnit.SECONDS);
                textEqualsNotExpected = !button.getText().toString().equals(this.expectedText);
                LOGGER.info(methodName + " waiting button to have '" +
                    this.expectedText + "' written!!!");
            }
            Assertions.assertEquals(this.expectedText, button.getText().toString(),
                "Button with wrong text!!!!!");
        } finally {
            Utils.handleInterruption(methodName);
        }

        LOGGER.info(methodName + FINISHED);
    }
}
