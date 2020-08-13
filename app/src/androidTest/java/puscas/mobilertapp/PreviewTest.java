package puscas.mobilertapp;

import android.graphics.Bitmap;
import android.widget.Button;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runners.MethodSorters;
import puscas.mobilertapp.utils.Constants;
import puscas.mobilertapp.utils.ConstantsMethods;
import puscas.mobilertapp.utils.State;
import puscas.mobilertapp.utils.UtilsContextTest;
import puscas.mobilertapp.utils.UtilsTest;

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
     *
     * @return The {@link ViewInteraction} for the Render {@link Button}.
     */
    private static ViewInteraction startAndStopRendering() {
        return Espresso.onView(ViewMatchers.withId(R.id.renderButton))
            .check((view, exception) -> {
                LOGGER.info("GOING TO CLICK THE BUTTON 1.");
                final Button renderButton = view.findViewById(R.id.renderButton);
                LOGGER.info("GOING TO CLICK THE BUTTON 2.");
                Assertions.assertEquals(
                    Constants.RENDER,
                    renderButton.getText().toString(),
                    puscas.mobilertapp.Constants.BUTTON_MESSAGE
                );
                LOGGER.info("GOING TO CLICK THE BUTTON 3.");
            })
            .perform(new ViewActionButton(Constants.STOP))
            .check((view, exception) -> {
                LOGGER.info("GOING TO CLICK THE BUTTON 4.");
                final Button renderButton = view.findViewById(R.id.renderButton);
                LOGGER.info("GOING TO CLICK THE BUTTON 5.");
                Assertions.assertEquals(
                    Constants.STOP,
                    renderButton.getText().toString(),
                    puscas.mobilertapp.Constants.BUTTON_MESSAGE
                );
                LOGGER.info("GOING TO CLICK THE BUTTON 6.");
            })
            .perform(new ViewActionButton(Constants.RENDER));
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
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testPreviewScene() throws TimeoutException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        UtilsContextTest.resetPickerValues(this.activity, 2);

        final ViewInteraction viewInteraction = startAndStopRendering();
        Espresso.onIdle();

        UtilsContextTest.waitUntilRenderingDone(this.activity);

        viewInteraction.check((view, exception) -> {
            final Button renderButton = view.findViewById(R.id.renderButton);
            LOGGER.info("CHECKING RENDERING BUTTON.");
            Assertions.assertEquals(
                Constants.RENDER,
                renderButton.getText().toString(),
                puscas.mobilertapp.Constants.BUTTON_MESSAGE
            );
        });

        final DrawView drawView = UtilsTest.getPrivateField(this.activity, "drawView");
        final MainRenderer renderer = drawView.getRenderer();

        LOGGER.info("CHECKING RAY TRACING STATE.");
        Espresso.onView(ViewMatchers.withId(R.id.drawLayout))
            .check((view, exception) -> {
                final Bitmap bitmap = UtilsTest.getPrivateField(renderer, "bitmap");
                UtilsTest.assertRayTracingResultInBitmap(bitmap, false);

                Assertions.assertEquals(
                    State.IDLE,
                    renderer.getState(),
                    "State is not the expected"
                );
            });

        final String message = methodName + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }

}
