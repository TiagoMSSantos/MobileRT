package puscas.mobilertapp;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.CallSuper;
import androidx.test.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * The abstract class for the Android Instrumentation Tests.
 */
public class AbstractTest {

    /**
     * The {@link Logger} for this class.
     */
    @Nonnull
    private static final Logger LOGGER = Logger.getLogger(AbstractTest.class.getName());

    /**
     * The {@link Rule} for the {@link Timeout} for all the tests.
     */
    @Nonnull
    @ClassRule
    public static final TestRule timeoutClassRule = new Timeout(40L, TimeUnit.MINUTES);

    /**
     * The {@link Rule} for the {@link Timeout} for each test.
     */
    @Nonnull
    @Rule
    public final TestRule timeoutRule = new Timeout(40L, TimeUnit.MINUTES);

    /**
     * The {@link Rule} to create the {@link MainActivity}.
     */
    @Nonnull
    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule =
        new ActivityTestRule<>(MainActivity.class, true, true);

    /**
     * The {@link Rule} to access the external SD card.
     */
    @Nonnull
    @Rule
    public GrantPermissionRule grantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);

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
        LOGGER.info(methodName);

        final Intent intent = new Intent(Intent.ACTION_PICK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.activity = this.mainActivityActivityTestRule.launchActivity(intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            InstrumentationRegistry.getInstrumentation()
                .getUiAutomation().executeShellCommand(
                "pm grant " + InstrumentationRegistry.getTargetContext().getPackageName()
                    + " android.permission.READ_EXTERNAL_STORAGE"
            );
        }
    }

    /**
     * Tear down method called after each test.
     */
    @After
    @CallSuper
    @OverridingMethodsMustInvokeSuper
    public void tearDown() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        this.activity.finish();
        this.mainActivityActivityTestRule.finishActivity();
        this.activity = null;
    }

}
