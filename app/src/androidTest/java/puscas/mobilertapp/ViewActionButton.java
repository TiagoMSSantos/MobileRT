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

        this.expectedText = expectedText;

        final boolean interrupted = Thread.interrupted();
        LOGGER.warning("Reset interrupted: " + interrupted);
    }

    @Nonnull
    @Override
    public final Matcher<View> getConstraints() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final boolean interrupted = Thread.interrupted();
        LOGGER.warning("Reset interrupted: " + interrupted);

        return ViewMatchers.isAssignableFrom(Button.class);
    }

    @Nonnull
    @Override
    public final String getDescription() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final boolean interrupted = Thread.interrupted();
        LOGGER.warning("Reset interrupted: " + interrupted);

        return "Click button";
    }

    @Override
    public final void perform(@Nonnull final UiController uiController, @Nonnull final View view) {
        LOGGER.info("ViewActionButton#perform (" + this.expectedText + ")");

        try {
            final Button button = (Button) view;
            LOGGER.info("ViewActionButton#perform waiting");

            boolean textEqualsWrongExpected = button.getText().toString().equals(this.expectedText);
            while (textEqualsWrongExpected) {
                uiController.loopMainThreadForAtLeast(5000L);
                textEqualsWrongExpected = button.getText().toString().equals(this.expectedText);
                LOGGER.info("ViewActionButton# waiting button to NOT have '" + this.expectedText + "' written!!!");
            }

            uiController.loopMainThreadUntilIdle();
            LOGGER.info("ViewActionButton#perform clicking button");
            boolean buttonNotClickedProperly = !button.performClick();
            ++clickCounter;
            LOGGER.info("ViewActionButton#perform BUTTON CLICKED: " + clickCounter + " (" + this.expectedText + ")");
            while (buttonNotClickedProperly) {
                uiController.loopMainThreadForAtLeast(5000L);
                buttonNotClickedProperly = !button.performClick();
                ++clickCounter;
                LOGGER.info("ViewActionButton#perform BUTTON CLICKED: " + clickCounter + " (" + this.expectedText + ")");
                LOGGER.info("ViewActionButton# waiting to click button!!!");
            }

            boolean textEqualsNotExpected = !button.getText().toString().equals(this.expectedText);
            final long advanceSecs = 5L;
            for (long currentTimeSecs = 0L; currentTimeSecs < 60L && textEqualsNotExpected; currentTimeSecs += advanceSecs) {
                uiController.loopMainThreadForAtLeast(advanceSecs * 1000L);
                Uninterruptibles.sleepUninterruptibly(advanceSecs, TimeUnit.SECONDS);
                textEqualsNotExpected = !button.getText().toString().equals(this.expectedText);
                LOGGER.info("ViewActionButton# waiting button to have '" + this.expectedText + "' written!!!");
            }
            Assertions.assertEquals(this.expectedText, button.getText().toString(), "Button with wrong text!!!!!");
        } finally {
            final boolean interrupted = Thread.interrupted();
            LOGGER.info("Reset interrupted: " + interrupted);
        }

        LOGGER.info("ViewActionButton#perform" + FINISHED);
    }
}
