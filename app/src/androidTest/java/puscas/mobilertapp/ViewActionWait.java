package puscas.mobilertapp;

import com.google.common.util.concurrent.Uninterruptibles;

import java.util.concurrent.TimeUnit;

/**
 * Auxiliary class with the focus-free waits used by the instrumentation tests.
 */
public final class ViewActionWait {

    /**
     * Private constructor to avoid creating instances.
     */
    private ViewActionWait() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Settles the app: an optional test-thread sleep + one bounded main-thread round-trip,
     * focus-free (see {@link DirectInteraction}); the headless emulator grants no window focus
     * for Espresso's RootViewPicker.
     *
     * @param delayMillis The time to wait in milliseconds; {@code 0} just settles.
     */
    public static void waitForButtonUpdate(final int delayMillis) {
        settle(delayMillis);
    }

    /**
     * Alias of {@link #waitForButtonUpdate(int)} kept for call-site readability.
     *
     * @param delayMillis The time to wait in milliseconds; {@code 0} just settles.
     */
    public static void waitForBitmapUpdate(final int delayMillis) {
        settle(delayMillis);
    }

    /**
     * Optional test-thread sleep, then one bounded main-thread round-trip.
     *
     * @param delayMillis The time to sleep in milliseconds before the round-trip; {@code 0} skips it.
     */
    private static void settle(final int delayMillis) {
        if (delayMillis > 0) {
            Uninterruptibles.sleepUninterruptibly(delayMillis, TimeUnit.MILLISECONDS);
        }
        DirectInteraction.settle();
    }
}
