package puscas.mobilertapp;

import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import com.google.common.util.concurrent.Uninterruptibles;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NonNls;
import org.junit.jupiter.api.Assertions;

import java.util.concurrent.TimeUnit;

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

    /**
     * Helper method that waits until the {@link Button} shows the expected text.
     *
     * @param uiController The controller to use to interact with the UI.
     * @param button       The {@link Button} to act upon. never null.
     * @param expectedText The expected text shown in the {@link Button}.
     */
    private static void waitUntilTextIsShown(@NonNull final UiController uiController,
                                             @NonNull final Button button,
                                             @NonNull @NonNls final String expectedText) {
        boolean textBeforeNotExpected = button.getText().toString().equals(expectedText);
        while (textBeforeNotExpected) {
            uiController.loopMainThreadForAtLeast(5000L);
            textBeforeNotExpected = button.getText().toString().equals(expectedText);
        }
        uiController.loopMainThreadUntilIdle();
    }

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
