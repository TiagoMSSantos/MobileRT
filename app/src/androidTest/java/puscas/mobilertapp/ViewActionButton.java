package puscas.mobilertapp;

import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NonNls;
import org.junit.Assert;

import java.util.Objects;
import java.util.logging.Logger;

import puscas.mobilertapp.utils.Utils;
import puscas.mobilertapp.utils.UtilsT;

/**
 * Auxiliary class which represents the render {@link Button}.
 */
public final class ViewActionButton implements ViewAction {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(ViewActionButton.class.getSimpleName());

    /**
     * The expected text for the {@link Button}.
     */
    @NonNls private final String expectedText;

    /**
     * Whether to do a long click or not.
     */
    private final boolean pressLongClick;

    /**
     * The constructor.
     *
     * @param expectedText   The expected text for the {@link Button}.
     * @param pressLongClick Whether to do a long click or not.
     */
    public ViewActionButton(final String expectedText, final boolean pressLongClick) {
        this.expectedText = expectedText;
        this.pressLongClick = pressLongClick;
    }

    @NonNull
    @Override
    public Matcher<View> getConstraints() {
        logger.info("ViewActionButton#getConstraints");

        return ViewMatchers.isAssignableFrom(Button.class);
    }

    @NonNull
    @Override
    public String getDescription() {
        logger.info("ViewActionButton#getDescription");

        return "Click button";
    }

    @Override
    public void perform(@NonNull final UiController uiController, @NonNull final View view) {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info(methodName);

        try {
            final Button button = (Button) view;

            // Click the button.
            boolean buttonNotClickedProperly = this.pressLongClick
                ? button.performLongClick() : !button.performClick();

            while (buttonNotClickedProperly) {
                UtilsT.executeWithCatching(() -> uiController.loopMainThreadForAtLeast(5000L));
                buttonNotClickedProperly = this.pressLongClick
                    ? button.performLongClick() : !button.performClick();
            }

            if (this.expectedText == null) {
                UtilsT.executeWithCatching(uiController::loopMainThreadUntilIdle);
                logger.info("Clicked button: " + null + ", long click: " + this.pressLongClick);
                return;
            }
            boolean textEqualsNotExpected = !Objects.equals(button.getText().toString(), this.expectedText);
            final long advanceSecs = 1L;

            // Wait until expected text is shown.
            for (long currentTimeSecs = 0L; currentTimeSecs < 10L && textEqualsNotExpected;
                 currentTimeSecs += advanceSecs) {
                UtilsT.executeWithCatching(() -> uiController.loopMainThreadForAtLeast(advanceSecs * 1000L));
                textEqualsNotExpected = !Objects.equals(button.getText().toString(), this.expectedText);
            }
            Assert.assertEquals("Button with wrong text!!!!!",
                this.expectedText, button.getText().toString()
            );
            // Let the engine boot for a while.
            UtilsT.executeWithCatching(() -> uiController.loopMainThreadForAtLeast(500L));
            logger.info("Clicked button: " + this.expectedText + ", long click: " + this.pressLongClick);
        } finally {
            Utils.handleInterruption(methodName);
        }
    }

}
