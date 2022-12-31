package puscas.mobilertapp;

import android.Manifest;
import android.content.Intent;

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

import javax.annotation.OverridingMethodsMustInvokeSuper;

import lombok.extern.java.Log;
import puscas.mobilertapp.utils.UtilsT;

/**
 * The abstract class for the Android Instrumentation Tests.
 */
@Log
public abstract class AbstractTest {

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
    protected MainActivity activity = null;


    /**
     * Setup method called before each test.
     */
    @Before
    @CallSuper
    @OverridingMethodsMustInvokeSuper
    public void setUp() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.info(methodName);

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
        log.info(methodName);

        Preconditions.checkNotNull(this.activity, "The Activity didn't finish as expected!");

        this.activity.finish();
        this.mainActivityActivityTestRule.finishActivity();
        this.activity = null;
        UtilsT.executeWithCatching(Espresso::onIdle);

        Intents.release();
    }

}
