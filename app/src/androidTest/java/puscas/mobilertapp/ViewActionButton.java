package puscas.mobilertapp;

import android.view.View;
import android.widget.Button;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.Contract;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

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
    private final String expectedText;

    /**
     * The constructor for this class.
     */
    @Contract(pure = true)
    ViewActionButton(final String expectedText) {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
        Thread.interrupted();

        this.expectedText = expectedText;
    }

    @Nonnull
    @Override
    public final Matcher<View> getConstraints() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
        Thread.interrupted();

        return ViewMatchers.isAssignableFrom(Button.class);
    }

    @Nonnull
    @Override
    public final String getDescription() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
        Thread.interrupted();

        return "Click button";
    }

    @Override
    public final void perform(@Nonnull final UiController uiController, @Nonnull final View view) {
        LOGGER.info("ViewActionButton#perform (" + this.expectedText + ")");

        try {
            final Button button = (Button) view;
            LOGGER.info("ViewActionButton#perform waiting");

            boolean textEquals = button.getText().toString().equals(this.expectedText);
            while (textEquals) {
                uiController.loopMainThreadForAtLeast(3000L);
                textEquals = button.getText().toString().equals(this.expectedText);
                LOGGER.info("ViewActionButton# waiting button to NOT have '" + this.expectedText + "' written!!!");
            }

            uiController.loopMainThreadUntilIdle();
            LOGGER.info("ViewActionButton#perform clicking button");
            boolean result = button.performClick();
            LOGGER.info("ViewActionButton#perform button clicked 1");
            while (!result) {
                uiController.loopMainThreadForAtLeast(3000L);
                result = button.performClick();
                LOGGER.info("ViewActionButton# waiting to click button!!!");
            }
            LOGGER.info("ViewActionButton#perform button clicked 2");

            textEquals = button.getText().toString().equals(this.expectedText);
            final long advanceSecs = 3L;
            for (long currentTimeSecs = 0L; currentTimeSecs < 60L && !textEquals; currentTimeSecs += advanceSecs) {
                result = button.performClick();
                uiController.loopMainThreadForAtLeast(advanceSecs * 1000L);
                textEquals = button.getText().toString().equals(this.expectedText);
                LOGGER.info("ViewActionButton# waiting button to have '" + this.expectedText + "' written!!!");
            }
        } finally {
            LOGGER.info("Reset interrupted.");
            Thread.interrupted();
        }

        LOGGER.info("ViewActionButton#perform finished");
    }
}
