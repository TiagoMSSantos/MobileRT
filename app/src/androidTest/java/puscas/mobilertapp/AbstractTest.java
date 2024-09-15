package puscas.mobilertapp;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.widget.Button;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.rules.Timeout;
import org.junit.runner.Description;

import java.io.File;
import java.nio.file.FileSystem;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
    public static final TestRule timeoutClassRule = new Timeout(20L, TimeUnit.MINUTES);

    /**
     * The {@link Rule} for the {@link Timeout} for each test.
     */
    @NonNull
    @Rule
    public final TestRule timeoutRule = new Timeout(2L, TimeUnit.MINUTES);

    /**
     * The {@link ActivityScenario} to create the {@link MainActivity}.
     */
    @NonNull
    @Rule
    public final ActivityScenarioRule<MainActivity> mainActivityActivityTestRule = new ActivityScenarioRule<>(MainActivity.class);

    /**
     * The {@link MainActivity} to test.
     */
    @Nullable
    protected MainActivity activity = null;

    /**
     * The {@link Rule} to get the name of the current test.
     */
    @Rule
    final public TestName testName = new TestName();

    /**
     * The {@link Rule} to validate all {@link #closeActions} when a test succeeds.
     */
    @Rule
    public final TestRule testWatcher = new TestWatcher() {
        @Override
        protected void starting(final Description description) {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            Espresso.onIdle();
        }

        @Override
        protected void failed(final Throwable exception, final Description description) {
            logger.severe(testName.getMethodName() + ": failed");
        }

        @Override
        protected void succeeded(final Description description) {
            logger.info(testName.getMethodName() + " succeeded");
            for (final Runnable method : closeActions) {
                method.run();
            }
        }

        @Override
        protected void finished(final Description description) {
            logger.info(testName.getMethodName() + " finished");
            Intents.release();
        }
    };

    /**
     * A {@link Deque} to store {@link Runnable}s which should be called at the end of the test.
     * The {@link #tearDown()} method which is called after every test will call use this field and
     * call the method {@link Runnable#run()} of every {@link Runnable} stored temporarily here.
     * <p>
     * For example, this is useful to store temporarily {@link Runnable}s of methods that will close
     * a resource at the end of the test.
     */
    final private Deque<Runnable> closeActions = new ArrayDeque<>();


    /**
     * A setup method which is called first.
     */
    @BeforeClass
    @CallSuper
    public static void setUpAll() {
        dismissANRSystemDialog();
    }

    /**
     * Setup method called before each test.
     */
    @Before
    @CallSuper
    public void setUp() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info(methodName + ": " + this.testName.getMethodName());

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Espresso.onIdle();
        this.mainActivityActivityTestRule.getScenario().onActivity(activity -> this.activity = activity);

        Preconditions.checkNotNull(this.activity, "The Activity didn't start as expected!");
        grantPermissions();

        Intents.init();
        final List<Intent> intents = Intents.getIntents();
        if (!intents.isEmpty()) {
            logger.info("Resetting Intents that were missing from previous test.");
            Intents.intended(Matchers.anyOf(IntentMatchers.hasAction(Intent.ACTION_GET_CONTENT), IntentMatchers.hasAction(Intent.ACTION_MAIN)));
            Intents.assertNoUnverifiedIntents();
            Intents.release();
            Intents.init();
            Intents.assertNoUnverifiedIntents();
        }
        logger.info(methodName + " validating Intents");
        Intents.assertNoUnverifiedIntents();

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Espresso.onIdle();
        logger.info(methodName + ": " + this.testName.getMethodName() + " started");
    }

    /**
     * Tear down method called after each test.
     */
    @After
    @CallSuper
    public void tearDown() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info(methodName + ": " + this.testName.getMethodName());

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Espresso.onIdle();
        Preconditions.checkNotNull(this.activity, "The Activity didn't finish as expected!");

        final int timeToWaitSecs = 20;
        logger.info("Will wait for the Activity triggered by the test to finish. Max timeout in secs: " + timeToWaitSecs);
        final int waitInSecs = 1;
        int currentTimeSecs = 0;
        while (isActivityRunning(this.activity) && currentTimeSecs < timeToWaitSecs) {
            logger.info("Finishing the Activity.");
            this.activity.finish();
            // Wait for the app to be closed. Necessary for Android 12+.
            this.mainActivityActivityTestRule.getScenario().close();
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            Espresso.onIdle();
            Uninterruptibles.sleepUninterruptibly(waitInSecs, TimeUnit.SECONDS);
            currentTimeSecs += waitInSecs;
        }
        logger.info("Activity finished: " + !isActivityRunning(this.activity) + " (" + currentTimeSecs + "secs)");

        logger.info(methodName + ": " + this.testName.getMethodName() + " finished");
    }

    /**
     * Checks whether the {@link #activity} is running or not.
     *
     * @param activity The {@link Activity} used by the tests.
     * @return {@code true} if it is still running, otherwise {@code false}.
     */
    private boolean isActivityRunning(@NonNull final Activity activity) {
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
    private static void grantPermissions() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Espresso.onIdle();
        logger.info("Granting permissions to the MainActivity to be able to read files from an external storage.");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Necessary for the tests and MobileRT to be able to read any file from SD Card on Android 11+, by having the permission: 'MANAGE_EXTERNAL_STORAGE'.
            InstrumentationRegistry.getInstrumentation().getUiAutomation().adoptShellPermissionIdentity();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            InstrumentationRegistry.getInstrumentation().getUiAutomation().grantRuntimePermission(
                InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName(), Manifest.permission.READ_EXTERNAL_STORAGE
            );
            InstrumentationRegistry.getInstrumentation().getUiAutomation().grantRuntimePermission(
                InstrumentationRegistry.getInstrumentation().getContext().getPackageName(), Manifest.permission.READ_EXTERNAL_STORAGE
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("pm grant puscas.mobilertapp android.permission.READ_EXTERNAL_STORAGE");
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("pm grant puscas.mobilertapp.test android.permission.READ_EXTERNAL_STORAGE");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            waitForPermission(InstrumentationRegistry.getInstrumentation().getTargetContext(), Manifest.permission.MANAGE_EXTERNAL_STORAGE);
            waitForPermission(InstrumentationRegistry.getInstrumentation().getContext(), Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            waitForPermission(InstrumentationRegistry.getInstrumentation().getTargetContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            waitForPermission(InstrumentationRegistry.getInstrumentation().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Espresso.onIdle();

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
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
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
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SIZE, R.id.pickerSize, 1);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_PIXEL, R.id.pickerSamplesPixel, spp);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_LIGHT, R.id.pickerSamplesLight, spl);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_ACCELERATOR, R.id.pickerAccelerator, accelerator.ordinal());
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SHADER, R.id.pickerShader, shader.ordinal());

        // Make sure these tests do not use preview feature.
        UiTest.clickPreviewCheckBox(false);

        UtilsT.startRendering(showRenderWhenPressingButton);
        if (!expectedSameValues) {
            UtilsContextT.waitUntil(this.testName.getMethodName(), this.activity, Constants.STOP, State.BUSY);
        }
        UtilsContextT.waitUntil(this.testName.getMethodName(), this.activity, Constants.RENDER, State.IDLE, State.FINISHED);
        ViewActionWait.waitFor(0);

        UtilsT.assertRenderButtonText(Constants.RENDER);
        UtilsT.testStateAndBitmap(expectedSameValues);
        ViewActionWait.waitFor(0);
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
    protected void mockFileManagerReply(final boolean externalSdcard, @NonNull final String... filesPath) {
        logger.info(ConstantsAndroidTests.MOCK_FILE_MANAGER_REPLY);
        final Intent resultData = MainActivity.createIntentToLoadFiles(InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName());
        final String storagePath = externalSdcard
            ? UtilsContext.getSdCardPath(InstrumentationRegistry.getInstrumentation().getTargetContext())
            : UtilsContext.getInternalStoragePath(InstrumentationRegistry.getInstrumentation().getTargetContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final Uri firstFile = Uri.fromFile(new File(storagePath + ConstantsUI.FILE_SEPARATOR + filesPath[0]));
            final ClipData clipData = new ClipData(new ClipDescription("Scene", new String[]{"*" + ConstantsUI.FILE_SEPARATOR + "*"}), new ClipData.Item(firstFile));
            for (int index = 1; index < filesPath.length; ++index) {
                clipData.addItem(new ClipData.Item(Uri.fromFile(new File(storagePath + ConstantsUI.FILE_SEPARATOR + filesPath[index]))));
            }
            resultData.setClipData(clipData);
        } else {
            resultData.setData(Uri.fromFile(new File(storagePath + ConstantsUI.FILE_SEPARATOR + filesPath[0])));
        }
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        Intents.intending(IntentMatchers.hasAction(resultData.getAction())).respondWith(result);

        // Temporarily store the assertion that verifies if the application received the expected Intent.
        // And call it in the `teardown` method after every test in order to avoid duplicated code.
        this.closeActions.add(() -> Intents.intended(IntentMatchers.hasAction(resultData.getAction())));
    }

    /**
     * Dismiss any "Application Not Responding" (ANR) system dialog that might have appeared.
     */
    private static void dismissANRSystemDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS");
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("settings put global window_animation_scale 0");
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("settings put global transition_animation_scale 0");
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("settings put global animator_duration_scale 0");
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("pm grant puscas.mobilertapp android.permission.SET_ANIMATION_SCALE");
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("input keyevent 82");
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("input tap 800 400");
        }
    }

}
