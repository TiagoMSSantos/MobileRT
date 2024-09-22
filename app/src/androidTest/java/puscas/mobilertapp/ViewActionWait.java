package puscas.mobilertapp;

import android.view.View;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;

import java.util.logging.Logger;

/**
 * Auxiliary class which represents a {@link ViewAction} that will just wait for some time.
 * <p>
 * This class doesn't need a {@link Logger} because the method {@link ViewActionWait#getDescription()}
 * is already automatically called for logging purposes.
 */
public final class ViewActionWait implements ViewAction {

    /**
     * The time to wait in milliseconds.
     */
    private final int delayMillis;

    /**
     * Constructor.
     *
     * @param delayMillis Time to wait in milliseconds.
     */
    public ViewActionWait(final int delayMillis) {
        this.delayMillis = delayMillis;
    }

    @Override
    public Matcher<View> getConstraints() {
        return ViewMatchers.isAssignableFrom(View.class);
    }

    @Override
    public String getDescription() {
        return "Wait for " + this.delayMillis + " milliseconds";
    }

    @Override
    public void perform(final UiController uiController, final View view) {
        if (this.delayMillis == 0) {
            uiController.loopMainThreadUntilIdle();
        } else {
            uiController.loopMainThreadForAtLeast(this.delayMillis);
        }
    }

    /**
     * Let MobileRT continue processing for some time, or to become idle in case {@code 0} milliseconds
     * is provided.
     *
     * @param delayMillis The time to wait in milliseconds. If {@code 0}, then it waits for the app
     *                    to become idle.
     */
    public static void waitFor(final int delayMillis) {
        Espresso.onView(ViewMatchers.withId(R.id.renderButton))
            .perform(new ViewActionWait(delayMillis));
    }
}
