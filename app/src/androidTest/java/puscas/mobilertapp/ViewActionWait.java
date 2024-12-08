package puscas.mobilertapp;

import android.opengl.GLSurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;

import java.util.Objects;
import java.util.logging.Logger;

import javax.annotation.Nullable;

/**
 * Auxiliary class which represents a {@link ViewAction} that will just wait for some time.
 * <p>
 * This class doesn't need a {@link Logger} because the method {@link ViewActionWait#getDescription()}
 * is already automatically called for logging purposes.
 */
public final class ViewActionWait<T extends TextView> implements ViewAction {

    /**
     * The time to wait in milliseconds.
     */
    private final int delayMillis;

    /**
    * The ID of the {@link View} element.
    */
    private final int viewId;

    /**
    * The expected value from the {@link View} element.
    */
    @Nullable
    private final Object expectedValue;

    /**
     * Constructor.
     *
     * @param delayMillis Time to wait in milliseconds.
     * @param viewId      The {@link View} element ID.
     */
    public ViewActionWait(final int delayMillis, final int viewId) {
        this(delayMillis, viewId, null);
    }

    /**
     * Constructor.
     *
     * @param delayMillis   Time to wait in milliseconds.
     * @param viewId        The {@link View} element ID.
     * @param expectedValue The expected value from the {@link View} element.
     */
    public ViewActionWait(final int delayMillis, final int viewId, @Nullable final Object expectedValue) {
        this.delayMillis = delayMillis;
        this.viewId = viewId;
        this.expectedValue = expectedValue;
    }

    @Override
    public Matcher<View> getConstraints() {
        return ViewMatchers.isAssignableFrom(View.class);
    }

    @Override
    public String getDescription() {
        return "Wait view (" + this.viewId + ") for " + this.delayMillis + " milliseconds";
    }

    @Override
    public void perform(final UiController uiController, final View view) {
        if (this.delayMillis == 0) {
            Object value = null;
            do {
                uiController.loopMainThreadUntilIdle();
                if (this.viewId == R.id.preview) {
                    final CheckBox checkbox = view.findViewById(this.viewId);
                    value = checkbox.isChecked();
                }
            } while (this.expectedValue != null && !Objects.equals(value, this.expectedValue));
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
     * @implNote This method awaits for render {@link Button} to become idle.
     */
    public static void waitForButtonUpdate(final int delayMillis) {
        final int viewId = R.id.renderButton;
        Espresso.onView(ViewMatchers.withId(viewId))
            .perform(new ViewActionWait<>(delayMillis, viewId, null));
    }

    /**
     * Let MobileRT continue processing for some time, or to become idle in case {@code 0} milliseconds
     * is provided.
     *
     * @param delayMillis The time to wait in milliseconds. If {@code 0}, then it waits for the app
     *                    to become idle.
     * @implNote This method awaits for {@link GLSurfaceView} to become idle.
     */
    public static void waitForBitmapUpdate(final int delayMillis) {
        final int viewId = R.id.drawLayout;
        Espresso.onView(ViewMatchers.withId(viewId))
            .perform(new ViewActionWait<>(delayMillis, viewId, null));
    }
}
