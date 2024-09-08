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
 */
public final class ViewActionWait implements ViewAction {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(ViewActionWait.class.getSimpleName());

    /**
     * The time to wait in milliseconds.
     */
    private final int delayMillis;

    /**
     * Constructor.
     *
     * @param delayMillis Time to wait in milliseconds.
     */
    private ViewActionWait(final int delayMillis) {
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
        if (delayMillis == 0) {
            logger.info("Waiting for MobileRT to become idle.");
        } else if (delayMillis >= 2000) {
            logger.info("Letting MobileRT to continue process for '" + delayMillis + "'ms.");
        }
        Espresso.onView(ViewMatchers.withId(R.id.renderButton))
            .perform(new ViewActionWait(delayMillis));
    }
}
