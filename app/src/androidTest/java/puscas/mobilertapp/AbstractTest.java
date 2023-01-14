package puscas.mobilertapp;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.Button;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.google.common.base.Preconditions;

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
     * The {@link Rule} to access (read) the external SD card.
     */
    @TargetApi(JELLY_BEAN)
    @NonNull
    @Rule
    public final GrantPermissionRule grantPermissionReadExternalStorageRule =
        GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);

    /**
     * The {@link Rule} to access (write) the external SD card.
     */
    @NonNull
    @Rule
    public final GrantPermissionRule grantPermissionWriteExternalStorageRule =
        GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    /**
     * The {@link Rule} to access the Internet.
     */
    @NonNull
    @Rule
    public final GrantPermissionRule grantPermissionInternetRule =
        GrantPermissionRule.grant(Manifest.permission.INTERNET);

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
        UtilsT.executeWithCatching(Espresso::onIdle);
        this.activity = this.mainActivityActivityTestRule.launchActivity(intent);

        Preconditions.checkNotNull(this.activity, "The Activity didn't start as expected!");
        UtilsT.executeWithCatching(Espresso::onIdle);

        Intents.init();
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
