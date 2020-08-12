package puscas.mobilertapp;

import android.graphics.Bitmap;
import android.widget.Button;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
import puscas.mobilertapp.utils.ConstantsUI;
import puscas.mobilertapp.utils.State;
import puscas.mobilertapp.utils.UtilsContext;

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
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testPreviewScene() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final int numCores = UtilsContext.getNumOfCores(this.activity);

        Utils.changePickerValue(ConstantsUI.PICKER_SCENE, R.id.pickerScene, 2);
        Utils.changePickerValue(ConstantsUI.PICKER_THREADS, R.id.pickerThreads, numCores);
        Utils.changePickerValue(ConstantsUI.PICKER_SIZE, R.id.pickerSize, 8);
        Utils.changePickerValue(ConstantsUI.PICKER_SAMPLES_PIXEL, R.id.pickerSamplesPixel, 1);
        Utils.changePickerValue(ConstantsUI.PICKER_SAMPLES_LIGHT, R.id.pickerSamplesLight, 1);
        Utils.changePickerValue(ConstantsUI.PICKER_ACCELERATOR, R.id.pickerAccelerator, 3);
        Utils.changePickerValue(ConstantsUI.PICKER_SHADER, R.id.pickerShader, 2);

        LOGGER.info("GOING TO CLICK THE BUTTON.");
        final ViewInteraction viewInteraction =
            Espresso.onView(ViewMatchers.withId(R.id.renderButton))
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
        LOGGER.info("RENDERING STARTED AND STOPPED 1.");
        Espresso.onIdle();
        LOGGER.info("RENDERING STARTED AND STOPPED 2.");

        final long advanceSecs = 3L;
        final AtomicBoolean done = new AtomicBoolean(false);
        final DrawView drawView = Utils.getPrivateField(this.activity, "drawView");
        final MainRenderer renderer = drawView.getRenderer();
        LOGGER.info("RENDERING STARTED AND STOPPED 3.");
        for (long currentTimeSecs = 0L; currentTimeSecs < 120L && !done.get();
             currentTimeSecs += advanceSecs) {
            LOGGER.info("WAITING FOR RENDERING TO FINISH.");
            Uninterruptibles.sleepUninterruptibly(advanceSecs, TimeUnit.SECONDS);
            LOGGER.info("WAITING FOR RENDERING TO FINISH 2.");

            viewInteraction.check((view, exception) -> {
                final Button renderButton = view.findViewById(R.id.renderButton);
                LOGGER.info("CHECKING IF RENDERING DONE.");
                final String message = "Render button: " + renderButton.getText().toString();
                LOGGER.info(message);
                final String messageState = "State: " + renderer.getState().name();
                LOGGER.info(messageState);
                if (renderButton.getText().toString().equals(Constants.RENDER)
                    && renderer.getState() == State.IDLE) {
                    done.set(true);
                    LOGGER.info("RENDERING DONE.");
                }
            });
        }

        viewInteraction.check((view, exception) -> {
            final Button renderButton = view.findViewById(R.id.renderButton);
            LOGGER.info("CHECKING RENDERING BUTTON.");
            Assertions.assertEquals(
                Constants.RENDER,
                renderButton.getText().toString(),
                puscas.mobilertapp.Constants.BUTTON_MESSAGE
            );
        });

        LOGGER.info("CHECKING RAY TRACING STATE.");
        Espresso.onView(ViewMatchers.withId(R.id.drawLayout))
            .check((view, exception) -> {
                final Bitmap bitmap = Utils.getPrivateField(renderer, "bitmap");
                Utils.assertRayTracingResultInBitmap(bitmap, false);

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
