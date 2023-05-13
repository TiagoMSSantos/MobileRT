package puscas.mobilertapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.widget.Button;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.Constants;
import puscas.mobilertapp.constants.ConstantsUI;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.constants.Shader;
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
    public static final TestRule timeoutClassRule = new Timeout(40L, TimeUnit.MINUTES);

    /**
     * The {@link Rule} for the {@link Timeout} for each test.
     */
    @NonNull
    @Rule
    public final TestRule timeoutRule = new Timeout(40L, TimeUnit.MINUTES);

    /**
     * The {@link Rule} to create the {@link MainActivity}.
     */
    @NonNull
    @Rule
    public final ActivityTestRule<MainActivity> mainActivityActivityTestRule =
        new ActivityTestRule<>(MainActivity.class, true, true);

    /**
     * The {@link MainActivity} to test.
     */
    @Nullable
    protected MainActivity activity = null;


    /**
     * Setup method called before each test.
     */
    @Before
    @CallSuper
    @OverridingMethodsMustInvokeSuper
    public void setUp() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info(methodName);

        final Intent intent = new Intent(Intent.ACTION_PICK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        this.activity = this.mainActivityActivityTestRule.launchActivity(intent);
        grantPermissions(this.activity);

        Preconditions.checkNotNull(this.activity, "The Activity didn't start as expected!");
        Intents.init();

        UtilsT.executeWithCatching(Espresso::onIdle);
        // Wait a bit for the permissions to be granted to the app before starting the test. Necessary for Android 12+.
        Uninterruptibles.sleepUninterruptibly(2L, TimeUnit.SECONDS);
    }

    /**
     * Grant permissions for the {@link MainActivity} to be able to load files from an external
     * storage.
     *
     * @param activity The {@link Activity}.
     */
    private static void grantPermissions(final Activity activity) {
        logger.info("Granting permissions to the MainActivity to be able to read files from an external storage.");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().getUiAutomation().grantRuntimePermission(
                androidx.test.core.app.ApplicationProvider.getApplicationContext().getPackageName(), Manifest.permission.READ_MEDIA_AUDIO
            );
            InstrumentationRegistry.getInstrumentation().getUiAutomation().grantRuntimePermission(
                androidx.test.core.app.ApplicationProvider.getApplicationContext().getPackageName(), Manifest.permission.READ_MEDIA_VIDEO
            );
            InstrumentationRegistry.getInstrumentation().getUiAutomation().grantRuntimePermission(
                androidx.test.core.app.ApplicationProvider.getApplicationContext().getPackageName(), Manifest.permission.READ_MEDIA_IMAGES
            );
            waitForPermission(activity, android.Manifest.permission.READ_MEDIA_AUDIO);
            waitForPermission(activity, android.Manifest.permission.READ_MEDIA_VIDEO);
            waitForPermission(activity, android.Manifest.permission.READ_MEDIA_IMAGES);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            InstrumentationRegistry.getInstrumentation().getUiAutomation().grantRuntimePermission(
                androidx.test.core.app.ApplicationProvider.getApplicationContext().getPackageName(), android.Manifest.permission.READ_EXTERNAL_STORAGE
            );
            waitForPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            logger.info("Granting permissions to the tests to be able to read files from an external storage.");
            InstrumentationRegistry.getInstrumentation().getUiAutomation().grantRuntimePermission(
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getContext().getPackageName(), android.Manifest.permission.READ_EXTERNAL_STORAGE
            );
        }
        logger.info("Permissions granted.");
    }

    /**
     * Waits for a permission to be granted.
     *
     * @param activity   The {@link Activity}.
     * @param permission The permission which should be granted.
     */
    private static void waitForPermission(final Activity activity, final String permission) {
        while (ContextCompat.checkSelfPermission(
            activity,
            permission
        ) != PackageManager.PERMISSION_GRANTED) {
            logger.info("Waiting for the permission '" + permission + "'to be granted to the app.");
            Espresso.onIdle();
            Uninterruptibles.sleepUninterruptibly(2L, TimeUnit.SECONDS);
        }
        logger.info("Permission '" + permission + "' granted to the app!");
    }

    /**
     * Tear down method called after each test.
     */
    @After
    @CallSuper
    @OverridingMethodsMustInvokeSuper
    public void tearDown() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info(methodName);

        Preconditions.checkNotNull(this.activity, "The Activity didn't finish as expected!");

        this.activity.finish();
        this.mainActivityActivityTestRule.finishActivity();
        this.activity = null;
        UtilsT.executeWithCatching(Espresso::onIdle);

        Intents.release();
    }

    /**
     * Helper method that clicks the Render {@link Button} and waits for the
     * Ray Tracing engine to render the whole scene and then checks if the resulted image in the
     * {@link Bitmap} has different values.
     *
     * @param numCores           The number of CPU cores to use in the Ray Tracing process.
     * @param scene              The desired scene to render.
     * @param shader             The desired shader to be used.
     * @param accelerator        The desired accelerator to be used.
     * @param spp                The desired number of samples per pixel.
     * @param spl                The desired number of samples per light.
     * @param expectedSameValues Whether the {@link Bitmap} should have have only one color.
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    protected void assertRenderScene(final int numCores,
                                     final Scene scene,
                                     final Shader shader,
                                     final Accelerator accelerator,
                                     final int spp,
                                     final int spl,
                                     final boolean expectedSameValues) throws TimeoutException {
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SCENE, R.id.pickerScene, scene.ordinal());
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_THREADS, R.id.pickerThreads, numCores);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SIZE, R.id.pickerSize, 1);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_PIXEL, R.id.pickerSamplesPixel, spp);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SAMPLES_LIGHT, R.id.pickerSamplesLight, spl);
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_ACCELERATOR, R.id.pickerAccelerator, accelerator.ordinal());
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SHADER, R.id.pickerShader, shader.ordinal());

        // Make sure these tests do not use preview feature.
        UiTest.clickPreviewCheckBox(false);

        UtilsT.startRendering(expectedSameValues);
        UtilsContextT.waitUntilRenderingDone(this.activity);

        UtilsT.assertRenderButtonText(Constants.RENDER);
        UtilsT.testStateAndBitmap(expectedSameValues);
        UtilsT.executeWithCatching(Espresso::onIdle);
    }

}
