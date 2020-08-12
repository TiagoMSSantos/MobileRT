package puscas.mobilertapp;

import android.view.View;
import android.widget.Button;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.junit.jupiter.api.Assertions;
import puscas.mobilertapp.utils.ConstantsMethods;
import puscas.mobilertapp.utils.Utils;

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
     * The expected text for the {@link Button}.
     */
    private final @NonNls String expectedText;

    /**
     * The constructor for this class.
     */
    @Contract(pure = true) ViewActionButton(final String expectedText) {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        this.expectedText = expectedText;
    }

    @Nonnull
    @Override
    public final Matcher<View> getConstraints() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        return ViewMatchers.isAssignableFrom(Button.class);
    }

    @Nonnull
    @Override
    public final String getDescription() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        return "Click button";
    }

    @Override
    public final void perform(@Nonnull final UiController uiController, @Nonnull final View view) {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        final String message = methodName + "(" + this.expectedText + ")";
        LOGGER.info(message);

        try {
            final Button button = (Button) view;
            final String messageWaiting = methodName + " waiting";
            LOGGER.info(messageWaiting);

            boolean textBeforeNotExpected = button.getText().toString().equals(this.expectedText);
            while (textBeforeNotExpected) {
                uiController.loopMainThreadForAtLeast(5000L);
                textBeforeNotExpected = button.getText().toString().equals(this.expectedText);
                final String messageButtonWaiting = methodName +
                    " waiting button to NOT have '" + this.expectedText + "' written!!!";
                LOGGER.info(messageButtonWaiting);
            }

            uiController.loopMainThreadUntilIdle();
            final String messageButton = methodName + " clicking button";
            LOGGER.info(messageButton);
            boolean buttonNotClickedProperly = !button.performClick();
            final String messageButtonClicked =
                methodName + " BUTTON CLICKED: (" + this.expectedText + ")";
            LOGGER.info(messageButtonClicked);
            while (buttonNotClickedProperly) {
                final String messageButtonWaiting = methodName + " waiting to click button!!!";
                LOGGER.info(messageButtonWaiting);
                uiController.loopMainThreadForAtLeast(5000L);
                buttonNotClickedProperly = !button.performClick();
                LOGGER.info(messageButtonClicked);
            }

            boolean textEqualsNotExpected = !button.getText().toString().equals(this.expectedText);
            final long advanceSecs = 5L;
            for (long currentTimeSecs = 0L; currentTimeSecs < 60L && textEqualsNotExpected;
                 currentTimeSecs += advanceSecs) {
                uiController.loopMainThreadForAtLeast(advanceSecs * 1000L);
                Uninterruptibles.sleepUninterruptibly(advanceSecs, TimeUnit.SECONDS);
                textEqualsNotExpected = !button.getText().toString().equals(this.expectedText);
                final String messageButtonWaiting = methodName + " waiting button to have '" +
                    this.expectedText + "' written!!!";
                LOGGER.info(messageButtonWaiting);
            }
            Assertions.assertEquals(this.expectedText, button.getText().toString(),
                "Button with wrong text!!!!!");
        } finally {
            Utils.handleInterruption(methodName);
        }

        final String messageFinished = methodName + ConstantsMethods.FINISHED;
        LOGGER.info(messageFinished);
    }
}
