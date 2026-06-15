package puscas.mobilertapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.NoActivityResumedException;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleCallback;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.rules.Timeout;
import org.junit.runner.Description;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.Constants;
import puscas.mobilertapp.constants.ConstantsUI;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.constants.Shader;
import puscas.mobilertapp.constants.State;
import puscas.mobilertapp.utils.UtilsContext;
import puscas.mobilertapp.utils.UtilsContextT;
import puscas.mobilertapp.utils.UtilsPickerT;
import puscas.mobilertapp.utils.UtilsT;

/**
 * The abstract class for the Android Instrumentation Tests.
 */
public abstract class AbstractTest {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(AbstractTest.class.getSimpleName());

    /**
     * The {@link Rule} for the {@link Timeout} for all the tests.
     */
    @NonNull
    @ClassRule
    public static final TestRule timeoutClassRule = new Timeout(30L, TimeUnit.MINUTES);

    /**
    * Whether one test already failed or not.
    */
    private static boolean oneTestFailed = false;

    /** Whether storage permissions were granted in this process (grant once; resets per process). */
    private static volatile boolean permissionsGranted;

    /** Cross-process fail-fast marker (SD card); persists {@link #oneTestFailed} across processes. */
    private static final String FAIL_FAST_MARKER = "/MobileRT/.one_test_failed";

    /** @return The {@link File} for the cross-process fail-fast marker. */
    @NonNull
    private static File getFailFastMarkerFile() {
        final String sdCardPath = UtilsContext.getSdCardPath(
            InstrumentationRegistry.getInstrumentation().getTargetContext()
        );
        return new File(sdCardPath + FAIL_FAST_MARKER);
    }

    /** Marks the run failed: sets {@link #oneTestFailed} + writes {@link #FAIL_FAST_MARKER} (best-effort). */
    private static void markTestRunFailed() {
        oneTestFailed = true;
        try {
            final File marker = getFailFastMarkerFile();
            if (marker.createNewFile()) {
                logger.warning("Wrote fail-fast marker: " + marker.getAbsolutePath());
            }
        } catch (final Exception ex) {
            logger.severe("Could not write fail-fast marker (remaining tests will still run): " + ex.getMessage());
        }
    }

    /** Whether {@link Intents#init()} ran this test, so {@code finished()} only releases what setUp init'd. */
    private boolean intentsInitialized;

    /** The per-test {@link Timeout}; on API 26 setUp() spends part of it acquiring window focus. */
    @NonNull
    public final TestRule timeoutRule = new Timeout(200L, TimeUnit.SECONDS);

    /**
     * The {@link Rule} to get the name of the current test.
     */
    public final TestName testName = new TestName();

    /**
     * The {@link Rule} to validate all {@link #closeActions} when a test succeeds.
     */
    public final TestRule testWatcher = new TestWatcher() {
        @Override
        protected void starting(final Description description) {
            logger.info(description.getDisplayName() + ": test starting");
        }

        @Override
        protected void failed(final Throwable exception, final Description description) {
            logger.severe(testName.getMethodName() + ": test failed");

            if (!oneTestFailed) {
                markTestRunFailed();
                // Do NOT throw: failed() runs in failedQuietly(), a throw crashes Dalvik runners (API <= 16).
                if (exception != null) {
                    logger.severe(testName.getMethodName() + ": " + exception.getMessage());
                }
            }
        }

        @Override
        protected void succeeded(final Description description) {
            logger.info(testName.getMethodName() + ": test succeeded");
            Assume.assumeFalse(oneTestFailed);

            for (final Runnable method : closeActions) {
                method.run();
            }
        }

        @Override
        protected void finished(final Description description) {
            logger.info(testName.getMethodName() + ": test finished");

            // Release only if setUp init'd Intents: release() on uninitialized Intents throws.
            if (intentsInitialized) {
                Intents.release();
                intentsInitialized = false;
            }
        }

        @Override
        protected void skipped(final AssumptionViolatedException exception, final Description description) {
            logger.warning(testName.getMethodName() + ": test skipped");
        }
    };

    /** Per-test rules in explicit nesting order: testWatcher outermost (sees Timeout failure) -> timeoutRule -> testName. */
    @Rule
    public final RuleChain ruleChain = RuleChain
        .outerRule(testWatcher)
        .around(timeoutRule)
        .around(testName);

    /**
     * The {@link Rule} of {@link MainActivity} to test.
     */
    @Nullable
    protected static MainActivity activity = null;

    /** {@link Runnable}s run at end of test by {@link #tearDown()} (e.g. close a resource). */
    private final Deque<Runnable> closeActions = new ArrayDeque<>();

    /** Setup method called before all tests. */
    @BeforeClass
    @CallSuper
    public static void setUpAll() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info(methodName);

        // Cross-process fail fast: marker present means a previous test failed -> skip this class.
        try {
            if (getFailFastMarkerFile().isFile()) {
                AbstractTest.oneTestFailed = true;
                logger.warning(methodName + ": fail-fast marker found; skipping this class because a previous test already failed this attempt.");
                return;
            }
        } catch (final Exception ex) {
            logger.warning(methodName + ": could not check fail-fast marker: " + ex.getMessage());
        }

        // Cap Espresso idle/root waits at 45s; runWithWatchdog() backstops waits that ignore this.
        IdlingPolicies.setMasterPolicyTimeout(45L, TimeUnit.SECONDS);
        IdlingPolicies.setIdlingResourceTimeout(45L, TimeUnit.SECONDS);

        // API >= P: grant before launch (else onCreate's dialog holds focus -> later launches land PAUSED); API < P post-launch.
        try {
            grantPermissions();
        } catch (final RuntimeException grantFailure) {
            markTestRunFailed();
            throw grantFailure;
        }

        // Launch via lost-wakeup-immune launchMainActivityBounded (startActivitySync hangs on API 29).
        final int maxLaunchAttempts = 5;
        IllegalStateException lastLaunchFailure = null;
        for (int attempt = 1; attempt <= maxLaunchAttempts; attempt++) {
            try {
                AbstractTest.activity = launchMainActivityBounded(30_000L);
                lastLaunchFailure = null;
                break;
            } catch (final IllegalStateException launchFailure) {
                lastLaunchFailure = launchFailure;
                logger.warning("class-launch attempt " + attempt + '/' + maxLaunchAttempts
                    + " failed: " + launchFailure.getMessage());
                AbstractTest.activity = null;
            }
        }
        if (lastLaunchFailure != null) {
            // No attempt reached RESUMED. Mark failed so tearDown/tearDownAll skip cleanup (no NPE).
            markTestRunFailed();
            throw lastLaunchFailure;
        }
        // Post-launch block: every wait runWithWatchdog-bounded (main thread can wedge after RESUMED); any trip marks failed.
        try {
            // Quiesce the never-idling GLSurfaceView right after launch (wedges AndroidX infra); setUp() restores CONTINUOUS.
            setDrawViewRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            // API 21-27: granting WRITE_EXTERNAL_STORAGE spawns a focus-stealing orphan MainActivity; finish it (sleep 500ms so it registers).
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                grantPermissions();
                Uninterruptibles.sleepUninterruptibly(500L, TimeUnit.MILLISECONDS);
                // Bounded settle, not waitForIdleSync() (GL surface never idles -> 15s orphan-idle watchdog false-trips the first class on slow software-GL).
                DirectInteraction.settle();
                runWithWatchdog("orphan-finish", 15_000L,
                    () -> InstrumentationRegistry.getInstrumentation().runOnMainSync(
                        () -> finishTrackedMainActivities(AbstractTest.activity, "setUpAll")
                    )
                );
                DirectInteraction.settle();
            }
            // setDrawViewRenderMode swallows its trip; probe the looper with a bounded no-op to redden a wedged main thread here.
            runWithWatchdog("post-launch-probe", 15_000L,
            () -> InstrumentationRegistry.getInstrumentation().runOnMainSync(
                () -> logger.fine("setUpAll: main thread alive after launch")
            ));
            // A still-CONTINUOUS GLSurfaceView hangs ActivityFinisher; fail fast (-1 unreadable is tolerated).
            final int renderModeAfterQuiesce = getDrawViewRenderMode();
            if (renderModeAfterQuiesce == GLSurfaceView.RENDERMODE_CONTINUOUSLY) {
                throw new IllegalStateException(
                    "setUpAll: DrawView still RENDERMODE_CONTINUOUSLY after quiesce; the "
                    + "renderer would hang ActivityFinisher between tests. Failing fast.");
            }
        } catch (final RuntimeException postLaunchFailure) {
            markTestRunFailed();
            throw postLaunchFailure;
        }

        logger.info("---------------------------------------------------");
        logger.info("Device: " + Build.DEVICE + ", User: " + Build.USER + ", Type: " + Build.TYPE
            + ", Tags: " + Build.TAGS + ", Host: " + Build.HOST + ", Fingerprint: " + Build.FINGERPRINT
            + ", Display: " + Build.DISPLAY + ", Brand: " + Build.BRAND + ", Model: " + Build.MODEL
            + ", Product: " + Build.PRODUCT);
        logger.info("---------------------------------------------------");
    }

    /**
     * Setup method called before each test.
     */
    @Before
    @CallSuper
    @SuppressWarnings("deprecation") // FLAG_DISMISS_KEYGUARD etc. were deprecated in API 27, but we still test on API < 27.
    public void setUp() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info(methodName + ": " + this.testName.getMethodName());
        Assume.assumeFalse(oneTestFailed);

        Preconditions.checkNotNull(AbstractTest.activity, "The Activity didn't start as expected!");

        Intents.init();
        this.intentsInitialized = true;
        final List<Intent> intents = Intents.getIntents();
        if (!intents.isEmpty()) {
            logger.info(this.testName.getMethodName() + ": Resetting Intents that were missing from previous test.");
            final Matcher<Intent> resetMatcher = Matchers.anyOf(IntentMatchers.hasAction(Intent.ACTION_GET_CONTENT), IntentMatchers.hasAction(Intent.ACTION_MAIN));
            // Count focus-free (countMatchingRecordedIntents): matching == total => every leftover Intent is GET_CONTENT or MAIN, none unverified.
            Assert.assertEquals("Leftover Intents must all be GET_CONTENT or MAIN", intents.size(), countMatchingRecordedIntents(resetMatcher));
        }
        // Check activity alive BEFORE stabilizing focus: runner finishes all activities at test start, leaving a non-null but dead activity that Espresso would spin ~100s on.
        if (!isActivityAlive(AbstractTest.activity)) {
            logger.warning(this.testName.getMethodName() + ": AbstractTest.activity is dead (finishing=" + AbstractTest.activity.isFinishing() + "). Forcing a restart.");
            relaunchMainActivity();
        } else {
            // Stabilize focus + quiesce GLSurfaceView, then run first Espresso step (CONTINUOUS restored after).
            runWithStabilizedFocus(() -> ViewActionWait.waitForButtonUpdate(0));
        }
        final List<Intent> intentsToVerify = Intents.getIntents();
        logger.info(this.testName.getMethodName() + ": " + methodName + " will validate '" + intentsToVerify.size() + "' Intents");
        final Matcher<Intent> matcher = Matchers.allOf(IntentMatchers.hasCategories(Collections.singleton(Intent.CATEGORY_LAUNCHER)), IntentMatchers.hasAction(Intent.ACTION_MAIN));
        logger.info(this.testName.getMethodName() + ": " + methodName + " validating '" + intentsToVerify.size() + "' Intents");
        // Count focus-free (countMatchingRecordedIntents): matching == total => every recorded Intent is LAUNCHER+MAIN, none unverified. Render stays quiesced until restored below.
        Assert.assertEquals("Expected all recorded Intents to be LAUNCHER+MAIN", intentsToVerify.size(), countMatchingRecordedIntents(matcher));
        // Every Espresso-dependent setup step succeeded: restore continuous rendering.
        setDrawViewRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        logger.info(this.testName.getMethodName() + " started");
    }

    /**
     * Whether an {@link Activity} is usable for Espresso (alive, not finishing, RESUMED); a non-RESUMED reference fires the relaunch path (Espresso throws with zero RESUMED activities).
     *
     * @return {@code true} if alive, not finishing, and RESUMED.
     */
    @SuppressLint("ObsoleteSdkInt")
    private static boolean isActivityAlive(@NonNull final Activity activity) {
        if (activity.isFinishing()) {
            logger.warning("isActivityAlive: Activity is finishing: " + activity);
            return false;
        }
        // Activity.isDestroyed() exists from API 17; on older APIs assume not-destroyed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) {
            logger.warning("isActivityAlive: Activity is destroyed: " + activity);
            return false;
        }
        // Espresso requires RESUMED; getLifecycleStageOf must run on the main thread.
        final Stage[] stageHolder = new Stage[1];
        InstrumentationRegistry.getInstrumentation().runOnMainSync(
            () -> stageHolder[0] = ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity)
        );
        logger.info("isActivityAlive: Activity lifecycle stage: " + stageHolder[0] + " for " + activity);
        return stageHolder[0] == Stage.RESUMED;
    }

    /**
     * Launches a fresh {@link MainActivity} via {@link Context#startActivity} + a {@link CountDownLatch} fed by the {@link ActivityLifecycleMonitor} (not startActivitySync, which loses its monitor wakeup on slow API 29 and hangs in {@code Object.wait}).
     *
     * @return The launched, RESUMED {@link MainActivity}.
     * @throws IllegalStateException If RESUMED is not reached within {@code timeoutMs}.
     */
    private static MainActivity launchMainActivityBounded(final long timeoutMs) {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        final CountDownLatch resumedLatch = new CountDownLatch(1);
        final AtomicReference<MainActivity> launchedActivity = new AtomicReference<>();
        final ActivityLifecycleCallback callback = (activity, stage) -> {
            if (activity instanceof MainActivity && stage == Stage.RESUMED) {
                launchedActivity.compareAndSet(null, (MainActivity) activity);
                resumedLatch.countDown();
            }
        };
        final ActivityLifecycleMonitor monitor = ActivityLifecycleMonitorRegistry.getInstance();
        monitor.addLifecycleCallback(callback);
        try {
            // makeMainActivity (ACTION_MAIN + CATEGORY_LAUNCHER); an explicit-component intent would fail setUp()'s LAUNCHER+MAIN matcher on the relaunch path.
            final Intent intent = Intent.makeMainActivity(
                new ComponentName(instrumentation.getTargetContext(), MainActivity.class)
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            instrumentation.getTargetContext().startActivity(intent);
            final boolean resumed = Uninterruptibles.awaitUninterruptibly(
                resumedLatch, timeoutMs, TimeUnit.MILLISECONDS
            );
            if (!resumed) {
                throw new IllegalStateException("launch: MainActivity did not reach RESUMED within "
                    + timeoutMs + "ms (startActivitySync-free launch)."
                );
            }
            final MainActivity result = launchedActivity.get();
            if (result == null) {
                throw new IllegalStateException("launch: RESUMED latch released but no MainActivity captured.");
            }
            return result;
        } finally {
            monitor.removeLifecycleCallback(callback);
        }
    }

    /** Adds keyguard-bypass flags and requests decor-view focus on the main thread (API <= 23). */
    @SuppressWarnings("deprecation") // FLAG_DISMISS_KEYGUARD etc. deprecated in API 27; still tested on API < 27.
    private void addFocusFlagsAndRequestFocus() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            if (AbstractTest.activity.getWindow() != null && AbstractTest.activity.getWindow().getDecorView() != null) {
                logger.info(this.testName.getMethodName() + " requesting focus");
                // No FLAG_SHOW_WHEN_LOCKED / keyguard-occlusion poking: the CI emulator has no secure
                // keyguard so it shows nothing, and any keyguard-occluded transition crashes SystemUI on
                // API 26/27 (NavigationBarFragment.onKeyguardOccludedChanged null-fragment NPE -> "System
                // UI has stopped"). Focus comes from requestFocus() + forceWindowFocusViaTap, not this flag.
                AbstractTest.activity.getWindow().addFlags(
                      WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                );
                AbstractTest.activity.getWindow().getDecorView().requestFocus();
                logger.info(this.testName.getMethodName() + " requested focus");
            } else {
                logger.severe(this.testName.getMethodName() + " couldn't request focus");
            }
        });
    }

    /** Finishes every MainActivity the monitor still tracks except {@code keep}. Must run on the main thread. */
    private static void finishTrackedMainActivities(@Nullable final Activity keep, @NonNull final String label) {
        for (final Stage stage : Stage.values()) {
            final Collection<Activity> activitiesInStage =
                new ArrayList<>(ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage));
            for (final Activity activity : activitiesInStage) {
                if (activity instanceof MainActivity && !activity.isFinishing() && activity != keep) {
                    logger.warning(label + ": Finishing " + stage + " MainActivity: " + activity);
                    activity.finish();
                }
            }
        }
    }

    /** Forces a fresh {@link MainActivity} when the runner's finishAll() destroyed it, or setUp's first Espresso call throws {@link NoActivityResumedException}. Updates {@link #activity}. */
    void relaunchMainActivity() {
        // Finish any MainActivity still tracked by the monitor before relaunching.
        InstrumentationRegistry.getInstrumentation().runOnMainSync(
            () -> finishTrackedMainActivities(null, this.testName.getMethodName()));
        // Bounded settle, not waitForIdleSync() (GL surface never idles -> ~16 min block).
        Uninterruptibles.sleepUninterruptibly(500L, TimeUnit.MILLISECONDS);
        // Watchdog-wrap the launch too (runs OUTSIDE the per-test Timeout).
        runWithWatchdog("relaunch-launch", 60_000L,
            () -> AbstractTest.activity = launchMainActivityBounded(30_000L)
        );

        // Give the activity window focus (flags dismiss the keyguard on slow API <= 23).
        addFocusFlagsAndRequestFocus();
        // Bounded settle, not waitForIdleSync() (GL surface never idles, same hang as above).
        Uninterruptibles.sleepUninterruptibly(500L, TimeUnit.MILLISECONDS);
    }

    /** Runs an Espresso action after stabilizing window focus + quiescing the GLSurfaceView (Espresso needs a focused, stable root; DrawView never quiesces and the window can exceed Espresso's ~10s focus wait). Each bounded attempt re-fronts the task, re-requests focus, quiesces. */
    private void runWithStabilizedFocus(final Runnable espressoAction) {
        // 4 attempts, each watchdog-bounded to ~30s below, so the loop can never spin forever.
        final int maxFocusAttempts = 4;
        RuntimeException lastFocusFailure = null;
        for (int focusAttempt = 1; focusAttempt <= maxFocusAttempts; focusAttempt++) {
            // Re-front our task so system_server moves focus back (same-process calls cannot).
            bringActivityTaskToFront();
            addFocusFlagsAndRequestFocus();
            // Headless API 26 never grants the input-focus token; inject a tap (touch-to-focus).
            forceWindowFocusViaTap();
            setDrawViewRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            // Bound the uninterruptible waitForIdleSync()/onIdle() the JUnit Timeout cannot break.
            runWithWatchdog("focus-idle", 15_000L, () -> {
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                Espresso.onIdle();
            });
            try {
                // RootViewPicker spins uninterruptibly (~16 min on API 26) with no focused root; cap it.
                runWithWatchdog("focus-espresso", 30_000L, espressoAction);
                return;
            } catch (final RuntimeException focusEx) {
                logger.severe(this.testName.getMethodName() + ": runWithWatchdog failed with: " + focusEx);

                final String focusMsg = focusEx.getMessage();
                final boolean isFocusTimeout =
                    focusEx.getClass().getName().contains("RootViewWithoutFocus") ||
                    (focusMsg != null && focusMsg.contains("window focus") && focusMsg.contains("request layout"));
                // A watchdog timeout here = "Root not ready" hang; count as a failed attempt, retry.
                final boolean isFocusWatchdogTimeout =
                    focusEx instanceof IllegalStateException && focusMsg != null && focusMsg.contains("watchdog: 'focus");
                if (!isFocusTimeout && !isFocusWatchdogTimeout) {
                    throw focusEx;
                }
                lastFocusFailure = focusEx;
                logger.warning(this.testName.getMethodName()
                    + ": Espresso could not get a focused root (attempt " + focusAttempt
                    + '/' + maxFocusAttempts + "); retrying. " + focusMsg
                );
                // Sleep on top of waitForIdleSync() so system_server can dispatch focus-changed events.
                runWithWatchdog("focus-retry-idle", 15_000L, () -> InstrumentationRegistry.getInstrumentation().waitForIdleSync());
                Uninterruptibles.sleepUninterruptibly(500L, TimeUnit.MILLISECONDS);
            }
        }
        // Focus never granted after all bounded tries: fail (the real fix is environmental).
        throw lastFocusFailure;
    }

    /** Runs {@code body} on a throw-away daemon thread, waiting at most {@code timeoutMs}. Bounds the API 26 hangs (RootViewPicker / waitForIdleSync() waits that the JUnit {@code Timeout} and {@code IdlingPolicies} cannot break): can't kill the wedged thread, but stops waiting and fails fast via {@link IllegalStateException}. */
    private static void runWithWatchdog(@NonNull final String label, final long timeoutMs, @NonNull final Runnable body) {
        final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
            final Thread thread = new Thread(runnable, "watchdog-" + label);
            thread.setDaemon(true);
            return thread;
        });
        try {
            final Future<?> future = executor.submit(body);
            try {
                future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (final TimeoutException timeoutEx) {
                future.cancel(true);
                throw new IllegalStateException("watchdog: '" + label + "' exceeded "
                    + timeoutMs + "ms (uninterruptible Espresso/idle hang); failing fast.", timeoutEx
                );
            } catch (final ExecutionException executionEx) {
                final Throwable cause = executionEx.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                if (cause instanceof Error) {
                    throw (Error) cause;
                }
                throw new IllegalStateException("watchdog: '" + label + "' failed.", cause);
            } catch (final InterruptedException interruptedEx) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("watchdog: '" + label + "' interrupted.", interruptedEx);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    /** Re-fronts the test activity's task via {@link ActivityManager#moveTaskToFront(int, int)} so system_server moves WINDOW focus back: no relaunch/Intent, so no orphan activity and setUp's intent counting stays intact. API 26+, needs {@code REORDER_TASKS}. */
    @SuppressLint("ObsoleteSdkInt")
    private void bringActivityTaskToFront() {
        try {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                final ActivityManager activityManager =
                    (ActivityManager) AbstractTest.activity.getSystemService(Context.ACTIVITY_SERVICE);
                if (activityManager != null) {
                    logger.info(this.testName.getMethodName() + ": moveTaskToFront");
                    activityManager.moveTaskToFront(AbstractTest.activity.getTaskId(), 0);
                }
            });
        } catch (final RuntimeException ex) {
            logger.severe(this.testName.getMethodName() + ": moveTaskToFront failed: " + ex);
        }
    }

    /** Runs a shell command on every API and discards stdout (side-effect only), blocking until the command finishes so callers can rely on its side effect (e.g. a screenshot file existing). API >= 21 uses {@link UiAutomation#executeShellCommand} (a binder call, no app_process fork) and drains its stdout to EOF: the command runs asynchronously, so reading the returned pipe to the end is what waits for it to complete - merely closing the pipe returns immediately (and can SIGPIPE-kill the command mid-write, e.g. {@code screencap} producing no file). {@code executeShellCommand} was added in API 21, so older APIs fall back to {@link Runtime#exec(String)} + {@link Process#waitFor()}, which the permissive emulator builds allow as the instrumentation uid. Errors are logged and rethrown. Used by {@link #grantPermissions()} and {@link UtilsT#captureScreenshot(String)}. */
    public static void runShellCommand(@NonNull final String command) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
                final ParcelFileDescriptor pfd = uiAutomation.executeShellCommand(command);
                // Drain stdout to EOF: this blocks until the command process exits, so the side effect
                // (e.g. the screenshot file) is in place before this method returns.
                try (InputStream in = new ParcelFileDescriptor.AutoCloseInputStream(pfd)) {
                    final byte[] buffer = new byte[4096];
                    int bytesRead = 0;
                    do {
                        bytesRead += in.read(buffer);
                    } while(bytesRead >= 0);
                    logger.info("runShellCommand: '" + command + "' stdout: " + bytesRead + " bytes");
                }
            } else {
                // executeShellCommand is API 21+; on older APIs spawn the command as the app uid.
                Runtime.getRuntime().exec(command).waitFor();
            }
        } catch (final IOException ex) {
            logger.severe("shell command '" + command + "' failed: " + ex);
            throw new RuntimeException("Failed to run shell command: " + command, ex);
        } catch (final InterruptedException ex) {
            logger.severe("shell command '" + command + "' interrupted: " + ex);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while running shell command: " + command, ex);
        }
    }

    /**
     * Tear down method called after each test.
     */
    @After
    @CallSuper
    public void tearDown() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info(methodName + ": " + this.testName.getMethodName());

        // Stop the render so the next test's rtStartRender(wait=true) does not block on "waiting for previous render to finish" (stacked latency past the deadline on slow software-GL legs).
        if (AbstractTest.activity != null) {
            try {
                final DrawView drawView = UtilsT.getPrivateField(AbstractTest.activity, "drawView");
                runWithWatchdog("stop-render", 30_000L, drawView::stopDrawing);
            } catch (final Exception ex) {
                logger.warning("could not stop render in tearDown: " + ex);
            }
        }

        if (!oneTestFailed) {
            Preconditions.checkNotNull(AbstractTest.activity, "The Activity didn't finish as expected!");
        }

        logger.info(this.testName.getMethodName() + " finished");
    }

    /**
     * A tear down method which is called last.
     */
    @AfterClass
    public static void tearDownAll() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info(methodName);

        if (!oneTestFailed) {
            Preconditions.checkNotNull(AbstractTest.activity, "The Activity didn't finish as expected!");

            final int timeToWaitSecs = 20;
            logger.info(methodName + ": Will wait for the Activity triggered by the test to finish. Max timeout in secs: " + timeToWaitSecs);
            final int waitInSecs = 1;
            int currentTimeSecs = 0;
            while (isActivityRunning(AbstractTest.activity) && currentTimeSecs < timeToWaitSecs) {
                logger.info(methodName + ": Finishing the Activity.");
                AbstractTest.activity.finish();
                Uninterruptibles.sleepUninterruptibly(waitInSecs, TimeUnit.SECONDS);
                currentTimeSecs += waitInSecs;
            }
            logger.info(methodName + ": Activity finished: " + !isActivityRunning(AbstractTest.activity) + " (" + currentTimeSecs + "secs)");
        }

        // Drop the static reference so the next test class starts clean.
        AbstractTest.activity = null;

        logger.info(methodName + " finished");
    }

    /** Sets the {@code drawView}'s render mode to quiesce ({@code RENDERMODE_WHEN_DIRTY}) or restore ({@code RENDERMODE_CONTINUOUSLY}) the continuous render loop. Failures logged and ignored. */
    private static void setDrawViewRenderMode(final int renderMode) {
        try {
            final GLSurfaceView drawView = UtilsT.getPrivateField(AbstractTest.activity, "drawView");
            // Watchdog caps the unbounded runOnMainSync at 15s. Retry until getRenderMode() reports the mode: a swallowed trip used to leave it CONTINUOUS, hanging ActivityFinisher.
            for (int attempt = 0; attempt < 3 && drawView.getRenderMode() != renderMode; attempt++) {
                try {
                    runWithWatchdog("render-mode", 15_000L,
                        () -> InstrumentationRegistry.getInstrumentation().runOnMainSync(
                            () -> drawView.setRenderMode(renderMode)
                        )
                    );
                } catch (final IllegalStateException watchdogTrip) {
                    logger.warning("render-mode attempt " + (attempt + 1)
                        + " tripped watchdog, retrying: " + watchdogTrip);
                }
            }
        } catch (final Exception ex) {
            logger.warning("could not change DrawView render mode: " + ex);
        }
    }

    /**
     * Reads the {@code drawView}'s render mode ({@code getRenderMode()} reads GLThread state under its own lock, so it is safe off the main thread).
     *
     * @return the render mode, or {@code -1} when the view could not be read.
     */
    private static int getDrawViewRenderMode() {
        try {
            final GLSurfaceView drawView = UtilsT.getPrivateField(AbstractTest.activity, "drawView");
            return drawView.getRenderMode();
        } catch (final Exception ex) {
            logger.warning("could not read DrawView render mode: " + ex);
            return -1;
        }
    }

    /** Injects a synthetic tap on {@code R.id.drawLayout} so {@code WindowManager} assigns the input-focus token (Java {@code requestFocus()} is intra-process only). API 26+; best-effort. */
    @SuppressLint("ObsoleteSdkInt")
    private void forceWindowFocusViaTap() {
        try {
            final float[] xy = new float[2];
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                final View draw = AbstractTest.activity.findViewById(R.id.drawLayout);
                if (draw == null || draw.getWidth() == 0 || draw.getHeight() == 0) {
                    return;
                }
                final int[] location = new int[2];
                draw.getLocationOnScreen(location);
                xy[0] = location[0] + draw.getWidth() / 2f;
                xy[1] = location[1] + draw.getHeight() / 2f;
            });
            if (xy[0] <= 0f && xy[1] <= 0f) {
                logger.warning(
                    this.testName.getMethodName() + ": drawLayout not measurable; skipping focus tap"
                );
                return;
            }
            final UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
            final long downTime = SystemClock.uptimeMillis();
            final MotionEvent down = MotionEvent.obtain(
                downTime, downTime, MotionEvent.ACTION_DOWN, xy[0], xy[1], 0
            );
            down.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            uiAutomation.injectInputEvent(down, true);
            down.recycle();
            final long upTime = SystemClock.uptimeMillis();
            final MotionEvent up = MotionEvent.obtain(downTime, upTime, MotionEvent.ACTION_UP, xy[0], xy[1], 0);
            up.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            uiAutomation.injectInputEvent(up, true);
            up.recycle();
            logger.info(this.testName.getMethodName() + ": injected focus tap at (" + xy[0] + ", " + xy[1] + ')');
        } catch (final RuntimeException ex) {
            logger.severe(this.testName.getMethodName() + ": focus tap failed: " + ex);
        }
    }

    /**
     * Checks whether the {@link #activity} is running or not.
     *
     * @param activity The {@link Activity} used by the tests.
     * @return {@code true} if it is still running, otherwise {@code false}.
     */
    @SuppressWarnings({"deprecation"})
    @SuppressLint("ObsoleteSdkInt")
    private static boolean isActivityRunning(@NonNull final Activity activity) {
        // Note that 'Activity#isDestroyed' only exists on Android API 17+.
        // More info: https://developer.android.com/reference/android/app/Activity#isDestroyed()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !activity.isDestroyed();
        } else {
            final ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
            final List<ActivityManager.RunningTaskInfo> tasksRunning = activityManager.getRunningTasks(Integer.MAX_VALUE);
            for (final ActivityManager.RunningTaskInfo taskRunning : tasksRunning) {
                if (taskRunning.baseActivity != null && Objects.equals(activity.getPackageName(), taskRunning.baseActivity.getPackageName())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Grant permissions for the {@link MainActivity} to be able to load files from an external
     * storage.
     */
    @SuppressLint("ObsoleteSdkInt")
    private static void grantPermissions() {
        // Grant once: permissions persist for the process lifetime (see permissionsGranted).
        if (permissionsGranted) {
            return;
        }
        // Bound waitForIdleSync so a post-RESUMED wedge reddens the test in seconds, not at step timeout.
        runWithWatchdog("grant-idle", 15_000L, () -> InstrumentationRegistry.getInstrumentation().waitForIdleSync());
        Espresso.onIdle();
        logger.info("Granting permissions to the MainActivity to be able to read files from an external storage.");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Necessary for the tests and MobileRT to be able to read any file from SD Card on Android 11+, by having the permission: 'MANAGE_EXTERNAL_STORAGE'.
            InstrumentationRegistry.getInstrumentation().getUiAutomation().adoptShellPermissionIdentity();
            // adoptShellPermissionIdentity changes only this process's CHECKS, granting the app nothing, so grant for real (API < 33; 33+ is non-grantable and opens no dialog).
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                InstrumentationRegistry.getInstrumentation().getUiAutomation().grantRuntimePermission(
                    InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName(), Manifest.permission.READ_EXTERNAL_STORAGE
                );
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // API 28-29: UiAutomation.grantRuntimePermission() exists from API 28 only.
            InstrumentationRegistry.getInstrumentation().getUiAutomation().grantRuntimePermission(
                InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName(), Manifest.permission.READ_EXTERNAL_STORAGE
            );
            InstrumentationRegistry.getInstrumentation().getUiAutomation().grantRuntimePermission(
                InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName(), Manifest.permission.WRITE_EXTERNAL_STORAGE
            );
            InstrumentationRegistry.getInstrumentation().getUiAutomation().grantRuntimePermission(
                InstrumentationRegistry.getInstrumentation().getContext().getPackageName(), Manifest.permission.READ_EXTERNAL_STORAGE
            );
            InstrumentationRegistry.getInstrumentation().getUiAutomation().grantRuntimePermission(
                InstrumentationRegistry.getInstrumentation().getContext().getPackageName(), Manifest.permission.WRITE_EXTERNAL_STORAGE
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // API 21-27: pm grant only. The orphan MainActivity it spawns is killed by setUpAll's cleanup.
            runShellCommand("pm grant puscas.mobilertapp android.permission.READ_EXTERNAL_STORAGE");
            runShellCommand("pm grant puscas.mobilertapp android.permission.WRITE_EXTERNAL_STORAGE");
            runShellCommand("pm grant puscas.mobilertapp.test android.permission.READ_EXTERNAL_STORAGE");
            runShellCommand("pm grant puscas.mobilertapp.test android.permission.WRITE_EXTERNAL_STORAGE");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            waitForPermission(InstrumentationRegistry.getInstrumentation().getTargetContext(), Manifest.permission.MANAGE_EXTERNAL_STORAGE);
            waitForPermission(InstrumentationRegistry.getInstrumentation().getContext(), Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            waitForPermission(InstrumentationRegistry.getInstrumentation().getTargetContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            waitForPermission(InstrumentationRegistry.getInstrumentation().getTargetContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            waitForPermission(InstrumentationRegistry.getInstrumentation().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            waitForPermission(InstrumentationRegistry.getInstrumentation().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        runWithWatchdog("grant-idle-end", 15_000L, () -> InstrumentationRegistry.getInstrumentation().waitForIdleSync());
        Espresso.onIdle();

        permissionsGranted = true;
        logger.info("Permissions granted.");
    }

    /**
     * Waits for a permission to be granted.
     *
     * @param context    The {@link Context}.
     * @param permission The permission which should be granted.
     */
    private static void waitForPermission(@NonNull final Context context, @NonNull final String permission) {
        final int timeToWaitMs = 10 * 1000;
        final int waitInMilliSecs = 1000;
        int currentTimeMs = 0;
        while (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED && currentTimeMs < timeToWaitMs) {
            logger.info("Waiting for the permission '" + permission + "' to be granted to the app: " + context.getPackageName());
            // Bound each idle wait: the 10s loop cap is useless if one waitForIdleSync never returns.
            runWithWatchdog("perm-idle", 15_000L, () -> InstrumentationRegistry.getInstrumentation().waitForIdleSync());
            Espresso.onIdle();
            Uninterruptibles.sleepUninterruptibly(waitInMilliSecs, TimeUnit.MILLISECONDS);
            currentTimeMs += waitInMilliSecs;
        }
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            logger.info("Permission '" + permission + "' granted to the app: " + context.getPackageName());
        } else {
            throw new RuntimeException("Permission '" + permission + "' NOT granted to the app: " + context.getPackageName());
        }
    }

    /**
     * Helper method that clicks the Render {@link Button} and waits for the
     * Ray Tracing engine to render the whole scene and then checks if the resulted image in the
     * {@link Bitmap} has different values.
     *
     * @param scene                        The desired scene to render.
     * @param shader                       The desired shader to be used.
     * @param accelerator                  The desired accelerator to be used.
     * @param spp                          The desired number of samples per pixel.
     * @param spl                          The desired number of samples per light.
     * @param showRenderWhenPressingButton Whether to show the {@link Constants#RENDER} text in the render button after pressing it.
     * @param expectedSameValues           Whether the {@link Bitmap} should have have only one color.
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    protected void assertRenderScene(final Scene scene,
                                     final Shader shader,
                                     final Accelerator accelerator,
                                     final int spp,
                                     final int spl,
                                     final boolean showRenderWhenPressingButton,
                                     final boolean expectedSameValues) throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(InstrumentationRegistry.getInstrumentation().getTargetContext());

        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SCENE, R.id.pickerScene, scene.ordinal());
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_THREADS, R.id.pickerThreads, numCores);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SIZE, R.id.pickerSize, 2);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_PIXEL, R.id.pickerSamplesPixel, spp);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_LIGHT, R.id.pickerSamplesLight, spl);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_ACCELERATOR, R.id.pickerAccelerator, accelerator.ordinal());
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SHADER, R.id.pickerShader, shader.ordinal());

        logger.info("Making sure these tests do not use preview feature.");
        UiTest.clickPreviewCheckBox(false);

        UtilsT.startRendering(showRenderWhenPressingButton);
        if (!expectedSameValues) {
            UtilsContextT.waitUntil(this.testName.getMethodName(), AbstractTest.activity, Constants.STOP, State.BUSY);
        }
        UtilsContextT.waitUntil(this.testName.getMethodName(), AbstractTest.activity, Constants.RENDER, State.IDLE, State.FINISHED);
        ViewActionWait.waitForButtonUpdate(0);

        UtilsT.assertRenderButtonText(Constants.RENDER);
        UtilsT.testStateAndBitmap(expectedSameValues);
        ViewActionWait.waitForButtonUpdate(0);
    }

    /**
     * Helper method that mocks the reply of an external file manager.
     * It's expected that the provided path to the {@link File}, is relative to the SD card, whether
     * external or internal storage.
     *
     * @param externalSdcard Whether the {@link File} is in the external SD Card or in the internal
     *                       storage.
     * @param filesPath      The relative path to multiple {@link File}s. The path should be
     *                       relative to the external SD card path or to the internal storage path
     *                       in the Android {@link FileSystem}.
     * @implNote This method stores a {@link Runnable} into the {@link #closeActions} in order to
     * call it in the {@link #tearDown()} method after every test. This {@link Runnable} verifies
     * whether the expected mocked {@link Intent} used by this method was really received by the
     * tested application. This is done to avoid duplicated code.
     */
    @SuppressLint("ObsoleteSdkInt")
    protected void mockFileManagerReply(final boolean externalSdcard, @NonNull final String... filesPath) {
        logger.info(ConstantsAndroid.MOCK_FILE_MANAGER_REPLY);
        final Intent expectedIntent = MainActivity.createIntentToLoadFiles();
        final Intent resultIntent = MainActivity.createIntentToLoadFiles();
        final String storagePath = externalSdcard
            ? UtilsContext.getSdCardPath(InstrumentationRegistry.getInstrumentation().getTargetContext())
            : UtilsContext.getInternalStoragePath();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final Uri firstFile = Uri.fromFile(new File(storagePath + ConstantsUI.FILE_SEPARATOR + filesPath[0]));
            final ClipData clipData = new ClipData(new ClipDescription("Scene", new String[]{"*" + ConstantsUI.FILE_SEPARATOR + "*"}), new ClipData.Item(firstFile));
            for (int index = 1; index < filesPath.length; ++index) {
                clipData.addItem(new ClipData.Item(Uri.fromFile(new File(storagePath + ConstantsUI.FILE_SEPARATOR + filesPath[index]))));
            }
            resultIntent.setClipData(clipData);
        } else {
            resultIntent.setData(Uri.fromFile(new File(storagePath + ConstantsUI.FILE_SEPARATOR + filesPath[0])));
        }
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent);
        Intents.intending(IntentMatchers.filterEquals(expectedIntent)).respondWith(result);

        // Store the received-Intent assertion to run in tearDown after every test (avoids dup code).
        this.closeActions.add(() ->
            assertIntentFiredOnce(IntentMatchers.filterEquals(expectedIntent))
        );
    }

    /** Verifies the app fired exactly one {@link Intent} matching {@code matcher} (focus-free, via {@link #countMatchingRecordedIntents}). */
    protected void assertIntentFiredOnce(@NonNull final Matcher<Intent> matcher) {
        Assert.assertEquals("Expected exactly one matching Intent to have been fired", 1, countMatchingRecordedIntents(matcher));
    }

    /** @return count of recorded {@link Intents#getIntents()} matching {@code matcher}; focus-free (no Espresso {@code RootViewPicker}), safe on any-API headless emulator. */
    private static int countMatchingRecordedIntents(@NonNull final Matcher<Intent> matcher) {
        int matches = 0;
        for (final Intent recorded : Intents.getIntents()) {
            if (matcher.matches(recorded)) {
                matches++;
            }
        }
        return matches;
    }
}
