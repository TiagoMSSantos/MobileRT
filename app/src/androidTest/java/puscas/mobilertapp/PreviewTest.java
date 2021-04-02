package puscas.mobilertapp;

import com.google.common.base.Preconditions;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import puscas.mobilertapp.utils.Constants;
import puscas.mobilertapp.utils.ConstantsMethods;
import puscas.mobilertapp.utils.Scene;
import puscas.mobilertapp.utils.UtilsContextT;
import puscas.mobilertapp.utils.UtilsT;

/**
 * The test suite for the preview feature (rasterize one frame of the scene).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class PreviewTest extends AbstractTest {

    /**
     * The {@link Logger} for this class.
     */
    @Nonnull
    private static final Logger LOGGER = Logger.getLogger(PreviewTest.class.getName());

    /**
     * Setup method called before each test.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();

        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Tear down method called after each test.
     */
    @After
    @Override
    public void tearDown() {
        super.tearDown();

        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Tests the preview feature in a scene.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 5L * 60L * 1000L)
    public void testPreviewScene() throws TimeoutException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
        Preconditions.checkNotNull(this.activity, "Activity can't be null");

        UtilsContextT.resetPickerValues(this.activity, Scene.CORNELL2.ordinal());

        UtilsT.startRendering();
        UtilsT.stopRendering();

        UtilsContextT.waitUntilRenderingDone(this.activity);

        UtilsT.assertRenderButtonText(Constants.RENDER);

        UtilsT.testStateAndBitmap(false);

        final String message = methodName + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }

}
