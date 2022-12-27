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

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import puscas.mobilertapp.utils.Utils;

/**
 * Auxiliary class which represents the render {@link Button}.
 */
@Log
@RequiredArgsConstructor
public final class ViewActionButton implements ViewAction {

    /**
     * The expected text for the {@link Button}.
     */
    @NonNls private final String expectedText;

    /**
     * Whether to do a long click or not.
     */
    private final boolean pressLongClick;

    @NonNull
    @Override
    public Matcher<View> getConstraints() {
        log.info("ViewActionButton#getConstraints");

        return ViewMatchers.isAssignableFrom(Button.class);
    }

    @NonNull
    @Override
    public String getDescription() {
        log.info("ViewActionButton#getDescription");

        return "Click button";
    }

    @Override
    public void perform(@NonNull final UiController uiController, @NonNull final View view) {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.info(methodName);

        try {
            final Button button = (Button) view;

            // Click the button.
            boolean buttonNotClickedProperly = this.pressLongClick
                ? button.performLongClick() : !button.performClick();

            while (buttonNotClickedProperly) {
                uiController.loopMainThreadForAtLeast(5000L);
                buttonNotClickedProperly = this.pressLongClick
                    ? button.performLongClick() : !button.performClick();
            }

            if (this.expectedText == null) {
                uiController.loopMainThreadUntilIdle();
                return;
            }
            boolean textEqualsNotExpected = !button.getText().toString().equals(this.expectedText);
            final long advanceSecs = 1L;

            // Wait until expected text is shown.
            for (long currentTimeSecs = 0L; currentTimeSecs < 10L && textEqualsNotExpected;
                 currentTimeSecs += advanceSecs) {
                uiController.loopMainThreadForAtLeast(advanceSecs * 1000L);
                textEqualsNotExpected = !button.getText().toString().equals(this.expectedText);
            }
            Assert.assertEquals("Button with wrong text!!!!!",
                this.expectedText, button.getText().toString()
            );
        } finally {
            Utils.handleInterruption(methodName);
        }
    }

}
