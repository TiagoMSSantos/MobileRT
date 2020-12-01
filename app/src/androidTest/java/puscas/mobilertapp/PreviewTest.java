package puscas.mobilertapp;

import android.widget.Button;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
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
import puscas.mobilertapp.utils.UtilsContextTest;
import puscas.mobilertapp.utils.UtilsTest;

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
     * Helper method that clicks the Render {@link Button} 2 times in a row,
     * so it starts and stops the Ray Tracing engine.
     */
    private static void startAndStopRendering() {
        final ViewInteraction viewInteraction = UtilsTest.startRendering();
        UtilsTest.assertRenderButtonText(Constants.STOP);
        viewInteraction.perform(new ViewActionButton(Constants.RENDER));
    }

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
     * @throws TimeoutException If the Ray Tracing engine didn't stop rendering the scene.
     */
    @Test(timeout = 3L * 60L * 1000L)
    public void testPreviewScene() throws TimeoutException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        UtilsContextTest.resetPickerValues(this.activity, Scene.CORNELL2.ordinal());

        startAndStopRendering();
        Espresso.onIdle();

        UtilsContextTest.waitUntilRenderingDone(this.activity);

        UtilsTest.assertRenderButtonText(Constants.RENDER);

        UtilsTest.testStateAndBitmap(false);

        final String message = methodName + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }

}
