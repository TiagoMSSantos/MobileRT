package puscas.mobilertapp;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.util.concurrent.Uninterruptibles;

import org.junit.Assert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Focus-free replacement for {@code Espresso.onView(...)}, which throws {@code RootViewWithoutFocusException} on the headless {@code -no-window} emulator (any API; never grants decor-view focus). Skips {@code RootViewPicker}: clicks via {@link Button#performClick()}, picker via {@link NumberPicker#setValue(int)}, asserts read fields — none need focus.
 *
 * <p>Work runs on the main thread via a bounded {@link Handler#post(Runnable)} + {@link CountDownLatch#await(long, TimeUnit)}, not {@code waitForIdleSync()} (idle never reached under {@code RENDERMODE_CONTINUOUSLY}). Used unconditionally by the instrumentation tests.
 */
public final class DirectInteraction {

    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(DirectInteraction.class.getSimpleName());

    /** Deadline (ms) for a single main-thread round-trip; trips only if the main thread is wedged. */
    private static final long MAIN_THREAD_DEADLINE_MS = 5_000L;

    /** Sleep (ms) between value-poll iterations on the test thread. */
    private static final long POLL_INTERVAL_MS = 10L;

    /** Deadline (ms) for a view to become resolvable and enabled before a click; a relaunched activity's views settle late on slow software-GL legs. */
    private static final long CLICKABLE_DEADLINE_MS = 5_000L;

    /** Reads a value of type {@code T} from a {@link View} on the main thread. */
    interface ViewReader<T> {
        T read(@NonNull View view);
    }

    /** Private constructor to avoid creating instances. */
    private DirectInteraction() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** Runs {@code action} on the main thread, waiting at most {@code deadlineMs}. A caught {@link RuntimeException} is logged (uncaught on the main {@link Looper} would crash the process). */
    private static void runOnMain(@NonNull final Runnable action, final long deadlineMs) {
        final CountDownLatch latch = new CountDownLatch(1);
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                action.run();
            } catch (final RuntimeException ex) {
                LOGGER.severe("main-thread action failed: " + ex);
            } finally {
                latch.countDown();
            }
        });
        try {
            if (!latch.await(deadlineMs, TimeUnit.MILLISECONDS)) {
                LOGGER.warning("runOnMain timed out after " + deadlineMs + "ms; main thread busy");
            }
        } catch (final InterruptedException ex) {
            LOGGER.severe("Catched InterruptedException " + ex);
            Thread.currentThread().interrupt();
        }
    }

    /** Bounded main-thread round-trip draining one queue pass; replaces API 26 idle waits (never reached under continuous GL). */
    public static void settle() {
        LOGGER.fine("settle: bounded main-thread round-trip (replaces never-reached GL idle wait)");
        runOnMain(() -> LOGGER.fine("settle: main thread responded"), MAIN_THREAD_DEADLINE_MS);
    }

    /** Resolves a view by id from {@link AbstractTest#activity} (main thread only); {@code null} if not found. */
    @Nullable
    private static View resolveView(final int viewId) {
        final MainActivity current = AbstractTest.activity;
        if (current != null) {
            final View view = current.findViewById(viewId);
            if (view != null) {
                return view;
            }
        }
        return null;
    }

    /** Reads a value off a view on the main thread; {@code null} if the view is unresolved. */
    @Nullable
    private static <T> T readView(final int viewId, @NonNull final ViewReader<T> reader) {
        final AtomicReference<T> result = new AtomicReference<>();
        runOnMain(() -> {
            final View view = resolveView(viewId);
            if (view != null) {
                result.set(reader.read(view));
            }
        }, MAIN_THREAD_DEADLINE_MS);
        return result.get();
    }

    /** @return {@link TextView}/{@link Button} text by id, or {@code null} if unresolved. */
    @Nullable
    public static String readText(final int viewId) {
        return readView(viewId, view -> ((TextView) view).getText().toString());
    }

    /** @return {@link CompoundButton} (e.g. {@code CheckBox}) checked state by id, or {@code null} if unresolved. */
    @Nullable
    public static Boolean readChecked(final int viewId) {
        return readView(viewId, view -> ((CompoundButton) view).isChecked());
    }

    /** @return {@link NumberPicker} value by id, or {@code null} if unresolved. */
    @Nullable
    public static Integer readPickerValue(final int viewId) {
        return readView(viewId, view -> ((NumberPicker) view).getValue());
    }

    /** @return the {@link DrawView}'s {@link MainRenderer} by id, or {@code null} if unresolved. */
    @Nullable
    public static MainRenderer readRenderer(final int viewId) {
        return readView(viewId, view -> ((DrawView) view).getRenderer());
    }

    /** Polls (bounded by {@code timeoutMs}) until {@link #readText(int)} equals {@code expected}, tolerating a transient {@code null} while a restarted/relaunched activity re-resolves its views (e.g. the long-press test restarts the activity, leaving a destroy&rarr;create gap where {@link AbstractTest#activity} is stale/null on slow software-GL legs). Returns the last value read so the caller still asserts (and fails if it never reaches {@code expected}). */
    @Nullable
    public static String awaitText(final int viewId, @NonNull final String expected, final long timeoutMs) {
        final long endMs = System.currentTimeMillis() + timeoutMs;
        String current = readText(viewId);
        while (!expected.equals(current) && System.currentTimeMillis() < endMs) {
            Uninterruptibles.sleepUninterruptibly(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
            current = readText(viewId);
        }
        return current;
    }

    /** Polls (bounded by {@link #CLICKABLE_DEADLINE_MS}) until the view by id resolves and is enabled, so {@code performClick()} is actually delivered; a relaunched activity exposes its views before they finish settling. */
    private static void awaitClickable(final int viewId) {
        final long endMs = System.currentTimeMillis() + CLICKABLE_DEADLINE_MS;
        Boolean enabled = readView(viewId, View::isEnabled);
        while ((enabled == null || !enabled) && System.currentTimeMillis() < endMs) {
            Uninterruptibles.sleepUninterruptibly(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
            enabled = readView(viewId, View::isEnabled);
        }
        if (enabled == null || !enabled) {
            LOGGER.warning("awaitClickable: view " + viewId + " still unresolved/disabled after " + CLICKABLE_DEADLINE_MS + "ms; clicking anyway");
        }
    }

    /** Clicks a {@link Button} on the main thread; with non-null {@code expectedText} waits until the button shows it ({@code null} clicks without waiting, {@code longClick} long-clicks). A {@link TextWatcher} registered before the click catches a fast GL render that cycles the text within one frame ({@code sawExpected} then skips the assertion). */
    public static void clickButton(final int viewId, @Nullable final String expectedText,
                                   final boolean longClick, final long timeoutMs) {
        awaitClickable(viewId);
        final AtomicBoolean sawExpected = new AtomicBoolean(false);
        runOnMain(() -> {
            final View view = resolveView(viewId);
            if (!(view instanceof Button)) {
                LOGGER.severe("clickButton: view " + viewId + " not found or not a Button");
                return;
            }
            final Button button = (Button) view;
            if (expectedText != null) {
                button.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
                        // No-op.
                    }

                    @Override
                    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                        // No-op.
                    }

                    @Override
                    public void afterTextChanged(final Editable s) {
                        if (expectedText.equals(s.toString())) {
                            sawExpected.set(true);
                        }
                    }
                });
            }
            // 'delivered' = a listener consumed the click; with enabled it isolates a lost click from a render-start that started then reset.
            final boolean delivered = longClick ? button.performLongClick() : button.performClick();
            LOGGER.info("clickButton: view=" + viewId + " longClick=" + longClick + " delivered=" + delivered
                + " enabled=" + button.isEnabled());
        }, MAIN_THREAD_DEADLINE_MS);

        if (expectedText == null) {
            return;
        }
        final long endMs = System.currentTimeMillis() + timeoutMs;
        String current = readText(viewId);
        while (!expectedText.equals(current) && !sawExpected.get() && System.currentTimeMillis() < endMs) {
            Uninterruptibles.sleepUninterruptibly(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
            current = readText(viewId);
        }
        if (!sawExpected.get()) {
            Assert.assertEquals("Button with wrong text!!!!!", expectedText, current);
        }
    }

    /** Clicks a view on the main thread via {@link View#performClick()} (no text wait). */
    public static void clickView(final int viewId) {
        runOnMain(() -> {
            final View view = resolveView(viewId);
            if (view == null) {
                LOGGER.severe("clickView: view " + viewId + " not found");
                return;
            }
            view.performClick();
        }, MAIN_THREAD_DEADLINE_MS);
    }

    /** Sets a {@link NumberPicker}'s value on the main thread. */
    public static void setPicker(final int pickerId, final int value) {
        runOnMain(() -> {
            final View view = resolveView(pickerId);
            if (!(view instanceof NumberPicker)) {
                LOGGER.severe("setPicker: view " + pickerId + " not found or not a NumberPicker");
                return;
            }
            ((NumberPicker) view).setValue(value);
        }, MAIN_THREAD_DEADLINE_MS);
    }

    /** Polls until a {@link CompoundButton}'s checked state equals {@code expected} or times out. */
    public static void waitChecked(final int viewId, final boolean expected, final long timeoutMs) {
        final long endMs = System.currentTimeMillis() + timeoutMs;
        Boolean checked = readChecked(viewId);
        while ((checked == null || checked != expected) && System.currentTimeMillis() < endMs) {
            Uninterruptibles.sleepUninterruptibly(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
            checked = readChecked(viewId);
        }
    }
}
