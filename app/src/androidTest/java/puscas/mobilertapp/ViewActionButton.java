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
import org.jetbrains.annotations.NonNls;
import org.junit.jupiter.api.Assertions;
import puscas.mobilertapp.utils.Utils;

/**
 * Auxiliary class which represents the render {@link Button}.
 */
public final class ViewActionButton implements ViewAction {

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
     * Whether to do a long click or not.
     */
    private final boolean pressLongClick;

    /**
     * The constructor for this class.
     *
     * @param expectedText The expected text to show on the {@link Button} after the click.
     */
    public ViewActionButton(@Nonnull final String expectedText) {
        LOGGER.info("ViewActionButton");

        this.expectedText = expectedText;
        this.pressLongClick = false;
    }

    /**
     * The constructor for this class.
     *
     * @param expectedText   The expected text to show on the {@link Button} after the click.
     * @param pressLongClick Whether the click should be a long click or not.
     */
    ViewActionButton(@Nonnull final String expectedText, final boolean pressLongClick) {
        LOGGER.info("ViewActionButton");

        this.expectedText = expectedText;
        this.pressLongClick = pressLongClick;
    }

    /**
     * Helper method that waits until the {@link Button} shows the expected text.
     *
     * @param uiController The controller to use to interact with the UI.
     * @param button       The {@link Button} to act upon. never null.
     * @param expectedText The expected text shown in the {@link Button}.
     */
    private static void waitUntilTextIsShown(@Nonnull final UiController uiController,
                                             @Nonnull final Button button,
                                             @Nonnull final @NonNls String expectedText) {
        boolean textBeforeNotExpected = button.getText().toString().equals(expectedText);
        while (textBeforeNotExpected) {
            uiController.loopMainThreadForAtLeast(5000L);
            textBeforeNotExpected = button.getText().toString().equals(expectedText);
        }
        uiController.loopMainThreadUntilIdle();
    }

    @Nonnull
    @Override
    public final Matcher<View> getConstraints() {
        LOGGER.info("ViewActionButton#getConstraints");

        return ViewMatchers.isAssignableFrom(Button.class);
    }

    @Nonnull
    @Override
    public final String getDescription() {
        LOGGER.info("ViewActionButton#getDescription");

        return "Click button";
    }

    @Override
    public final void perform(@Nonnull final UiController uiController, @Nonnull final View view) {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        try {
            final Button button = (Button) view;

//            waitUntilTextIsShown(uiController, button, this.expectedText);

            // Click the button.
            boolean buttonNotClickedProperly = this.pressLongClick
                ? button.performLongClick() : !button.performClick();

            while (buttonNotClickedProperly) {
                uiController.loopMainThreadForAtLeast(5000L);
                buttonNotClickedProperly = this.pressLongClick
                    ? button.performLongClick() : !button.performClick();
            }

            boolean textEqualsNotExpected = !button.getText().toString().equals(this.expectedText);
            final long advanceSecs = 5L;

            // Wait until expected text is shown.
            for (long currentTimeSecs = 0L; currentTimeSecs < 60L && textEqualsNotExpected;
                 currentTimeSecs += advanceSecs) {
                uiController.loopMainThreadForAtLeast(advanceSecs * 1000L);
                Uninterruptibles.sleepUninterruptibly(advanceSecs, TimeUnit.SECONDS);
                textEqualsNotExpected = !button.getText().toString().equals(this.expectedText);
            }
            Assertions.assertEquals(this.expectedText, button.getText().toString(),
                "Button with wrong text!!!!!");
        } finally {
            Utils.handleInterruption(methodName);
        }
    }

}
