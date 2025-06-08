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
import android.os.ParcelFileDescriptor;
import android.widget.Button;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoActivityResumedException;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.VerificationModes;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.rules.Timeout;
import org.junit.runner.Description;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.util.ArrayDeque;
import java.util.Collections;
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
import puscas.mobilertapp.exceptions.FailureException;
import puscas.mobilertapp.utils.UtilsContext;
import puscas.mobilertapp.utils.UtilsContextT;
import puscas.mobilertapp.utils.UtilsLogging;
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
     * The {@link Rule} of {@link ActivityScenario} to create the {@link MainActivity}.
     */
    @NonNull
    @ClassRule
    public static final ActivityScenarioRule<MainActivity> mainActivityActivityTestRule = new ActivityScenarioRule<>(MainActivity.class);

    /**
    * Whether one test already failed or not.
    */
    private static boolean oneTestFailed = false;

    /**
     * The {@link Rule} for the {@link Timeout} for each test.
     * <p>
     * Note that in MacOS the Android emulator is slower and thus might need a higher timeout.
     * Using 180 seconds seems to be enough.
     */
    @NonNull
    @Rule
    public final TestRule timeoutRule = new Timeout(180L, TimeUnit.SECONDS);

    /**
     * The {@link Rule} to get the name of the current test.
     */
    @Rule
    public final TestName testName = new TestName();

    /**
     * The {@link Rule} to validate all {@link #closeActions} when a test succeeds.
     */
    @Rule
    public final TestRule testWatcher = new TestWatcher() {
        @Override
        protected void starting(final Description description) {
            logger.info(description.getDisplayName() + ": test starting");
        }

        @Override
        protected void failed(final Throwable exception, final Description description) {
            logger.severe(testName.getMethodName() + ": test failed");

            if (!oneTestFailed) {
                oneTestFailed = true;
                if (exception != null) {
                    // Throw exception to print the test name in the error message.
                    final String errorMessage = testName.getMethodName() + ": " + exception.getMessage();
                    throw new FailureException(errorMessage, exception);
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

            if (!oneTestFailed) {
                Intents.release();
            }
        }

        @Override
        protected void skipped(final AssumptionViolatedException exception, final Description description) {
            logger.warning(testName.getMethodName() + ": test skipped");
        }
    };

    /**
     * The {@link Rule} of {@link MainActivity} to test.
     */
    @Nullable
    protected static MainActivity activity = null;

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
     * Setup method called before all tests.
     */
    @BeforeClass
    @CallSuper
    public static void setUpAll() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info(methodName);

        mainActivityActivityTestRule.getScenario().onActivity(newActivity -> activity = newActivity);
        grantPermissions();

        logger.info("---------------------------------------------------");
        final String messageDevice = "Device: " + Build.DEVICE;
        logger.info(messageDevice);
        final String messageUser = "User: " + Build.USER;
        logger.info(messageUser);
        final String messageType = "Type: " + Build.TYPE;
        logger.info(messageType);
        final String messageTags = "Tags: " + Build.TAGS;
        logger.info(messageTags);
        final String messageHost = "Host: " + Build.HOST;
        logger.info(messageHost);
        final String messageFingerPrint = "Fingerprint: " + Build.FINGERPRINT;
        logger.info(messageFingerPrint);
        final String messageDisplay = "Display: " + Build.DISPLAY;
        logger.info(messageDisplay);
        final String messageBrand = "Brand: " + Build.BRAND;
        logger.info(messageBrand);
        final String messageModel = "Model: " + Build.MODEL;
        logger.info(messageModel);
        final String messageProduct = "Product: " + Build.PRODUCT;
        logger.info(messageProduct);
        logger.info("---------------------------------------------------");
    }

    /**
     * Setup method called before each test.
     */
    @Before
    @CallSuper
    public void setUp() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info(methodName + ": " + this.testName.getMethodName());
        Assume.assumeFalse(oneTestFailed);

        Preconditions.checkNotNull(activity, "The Activity didn't start as expected!");

        Intents.init();
        final List<Intent> intents = Intents.getIntents();
        if (!intents.isEmpty()) {
            logger.info(this.testName.getMethodName() + ": Resetting Intents that were missing from previous test.");
            Intents.intended(
                Matchers.anyOf(IntentMatchers.hasAction(Intent.ACTION_GET_CONTENT), IntentMatchers.hasAction(Intent.ACTION_MAIN)),
                VerificationModes.times(intents.size())
            );
            Intents.assertNoUnverifiedIntents();
        }
        try {
            ViewActionWait.waitForButtonUpdate(0);
        } catch (final NoActivityResumedException ex) {
            UtilsLogging.logThrowable(ex, this.testName.getMethodName() + ": AbstractTest#setUp");
            logger.warning(this.testName.getMethodName() + ": The MainActivity didn't start as expected. Forcing a restart.");
            mainActivityActivityTestRule.getScenario().close();
            final ActivityScenario<MainActivity> newActivityScenario = ActivityScenario.launch(MainActivity.class);
            newActivityScenario.onActivity(newActivity -> activity = newActivity);
        }
        final List<Intent> intentsToVerify = Intents.getIntents();
        logger.info(this.testName.getMethodName() + ": " + methodName + " validating '" + intentsToVerify.size() + "' Intents");
        Intents.intended(
            Matchers.allOf(IntentMatchers.hasCategories(Collections.singleton(Intent.CATEGORY_LAUNCHER)), IntentMatchers.hasAction(Intent.ACTION_MAIN)),
            VerificationModes.times(intentsToVerify.size())
        );
        Intents.assertNoUnverifiedIntents();

        logger.info(this.testName.getMethodName() + " started");
    }

    /**
     * Tear down method called after each test.
     */
    @After
    @CallSuper
    public void tearDown() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info(methodName + ": " + this.testName.getMethodName());

        if (!oneTestFailed) {
            Preconditions.checkNotNull(activity, "The Activity didn't finish as expected!");
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
            Preconditions.checkNotNull(activity, "The Activity didn't finish as expected!");

            final int timeToWaitSecs = 20;
            logger.info(methodName + ": Will wait for the Activity triggered by the test to finish. Max timeout in secs: " + timeToWaitSecs);
            final int waitInSecs = 1;
            int currentTimeSecs = 0;
            while (isActivityRunning(activity) && currentTimeSecs < timeToWaitSecs) {
                logger.info(methodName + ": Finishing the Activity.");
                activity.finish();
                currentTimeSecs += waitInSecs;
            }
            // Wait for the app to be closed. Necessary for Android 12+.
            mainActivityActivityTestRule.getScenario().close();
            logger.info(methodName + ": Activity finished: " + !isActivityRunning(activity) + " (" + currentTimeSecs + "secs)");
        }

        logger.info(methodName + " finished");
    }

    /**
     * Checks whether the {@link #activity} is running or not.
     *
     * @param activity The {@link Activity} used by the tests.
     * @return {@code true} if it is still running, otherwise {@code false}.
     */
    @SuppressWarnings({"deprecation"})
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
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SIZE, R.id.pickerSize, 2);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_PIXEL, R.id.pickerSamplesPixel, spp);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_LIGHT, R.id.pickerSamplesLight, spl);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_ACCELERATOR, R.id.pickerAccelerator, accelerator.ordinal());
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SHADER, R.id.pickerShader, shader.ordinal());

        logger.info("Making sure these tests do not use preview feature.");
        UiTest.clickPreviewCheckBox(false);

        UtilsT.startRendering(showRenderWhenPressingButton);
        if (!expectedSameValues) {
            UtilsContextT.waitUntil(this.testName.getMethodName(), activity, Constants.STOP, State.BUSY);
        }
        UtilsContextT.waitUntil(this.testName.getMethodName(), activity, Constants.RENDER, State.IDLE, State.FINISHED);
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
    protected void mockFileManagerReply(final boolean externalSdcard, @NonNull final String... filesPath) {
        logger.info(ConstantsAndroidTests.MOCK_FILE_MANAGER_REPLY);
        final Intent expectedIntent = MainActivity.createIntentToLoadFiles(InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName());
        final Intent resultIntent = MainActivity.createIntentToLoadFiles(InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName());
        final String storagePath = externalSdcard
            ? UtilsContext.getSdCardPath(InstrumentationRegistry.getInstrumentation().getTargetContext())
            : UtilsContext.getInternalStoragePath(InstrumentationRegistry.getInstrumentation().getTargetContext());
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

        // Temporarily store the assertion that verifies if the application received the expected Intent.
        // And call it in the `teardown` method after every test in order to avoid duplicated code.
        this.closeActions.add(() ->
            Intents.intended(IntentMatchers.filterEquals(expectedIntent), VerificationModes.times(1))
        );
    }

    /**
     * Click on device to dismiss any "Application Not Responding" (ANR) system dialog that might have appeared.
     */
    private static void dismissANRSystemDialog() {
        executeShellCommand("input keyevent 82");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            executeShellCommand("input tap 800 400");
        }
    }

    /**
     * Execute a shell command on Android device.
     *
     * @param shellCommand The shell command to execute.
     */
    private static void executeShellCommand(final String shellCommand) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final ParcelFileDescriptor parcelFileDescriptor = InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(shellCommand);
                parcelFileDescriptor.checkError();
            } else {
                final Process process = Runtime.getRuntime().exec(shellCommand.split(" "));
                final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                int read;
                final char[] buffer = new char[4096];
                final StringBuilder output = new StringBuilder();
                while ((read = reader.read(buffer)) > 0) {
                    output.append(buffer, 0, read);
                }
                reader.close();
                process.waitFor();
                if (process.exitValue() != 0) {
                    final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    int readError;
                    final char[] bufferError = new char[4096];
                    final StringBuilder outputError = new StringBuilder();
                    while ((readError = errorReader.read(bufferError)) > 0) {
                        outputError.append(bufferError, 0, readError);
                    }
                    errorReader.close();
                    throw new FailureException("Command '" + shellCommand + "' failed with: " + outputError);
                }
            }
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        }
    }
}
