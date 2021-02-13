package puscas.mobilertapp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runners.MethodSorters;
import puscas.mobilertapp.utils.Constants;
import puscas.mobilertapp.utils.ConstantsUI;
import puscas.mobilertapp.utils.Scene;
import puscas.mobilertapp.utils.UtilsContextT;
import puscas.mobilertapp.utils.UtilsPickerT;
import puscas.mobilertapp.utils.UtilsT;
import static puscas.mobilertapp.utils.Constants.BYTES_IN_MEGABYTE;

/**
 * The test suite for the Ray Tracing engine used in {@link MainActivity} and some system fault might happen.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class FailuresTest extends AbstractTest {

    /**
     * The {@link Logger} for this class.
     */
    @Nonnull
    private static final Logger LOGGER = Logger.getLogger(FailuresTest.class.getName());

    /**
     * Dummies big arrays with garbage to allocate.
     * These array is to allocate a big chunk of memory and simulate a system with low memory available.
     */
    private final Collection<ByteBuffer> dummyArrays = new ArrayList<>();

    /**
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

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
     * Tests rendering a scene when the system has not enough memory available.
     */
    @Ignore
    @Test
    public void testRenderWhenLowMemory() throws TimeoutException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final int scene = Scene.TEST_INTERNAL_STORAGE.ordinal();
        UtilsPickerT.changePickerValue(ConstantsUI.PICKER_SCENE, R.id.pickerScene, scene);

        final DrawView drawView = UtilsT.getPrivateField(this.activity, "drawView");
        final MainRenderer mainRenderer = drawView.getRenderer();
        final long bytesAvailable = mainRenderer.getAvailMem();

        Assertions.assertTrue(bytesAvailable > 3L * (long) BYTES_IN_MEGABYTE,
            "Available memory must greater than 3MB.");

        final int integersToAllocate = (int) (bytesAvailable - (3L * (long) BYTES_IN_MEGABYTE) / 4L);

        // Math.min(integersToAllocate, 50000000)

        for (int i = 0; i < 100000; i+=1) {
            LOGGER.info("i: " + i);
            try {
                // Number of mega bytes to allocate.
                final int memToAlloc = 1 * (BYTES_IN_MEGABYTE / 4);
                this.dummyArrays.add(ByteBuffer.allocateDirect(BYTES_IN_MEGABYTE));
            } catch (final java.lang.OutOfMemoryError ex) {
                if (mainRenderer.getAvailMem() < (3000L * (long) BYTES_IN_MEGABYTE)) {
                    break;
//                    throw new OutOfMemoryError(ex.getMessage() + "; i1 :" + i + "; mem: " + mainRenderer.getAvailMem() / BYTES_IN_MEGABYTE+ " MB");
                }
                throw new OutOfMemoryError(ex.getMessage() + "; i2 :" + i + "; mem: " + mainRenderer.getAvailMem() / BYTES_IN_MEGABYTE+ " MB");
            }
            if (mainRenderer.getAvailMem() < (300L * (long) BYTES_IN_MEGABYTE)) {
                LOGGER.info("LOW MEMORY DETECTED!!!");
                LOGGER.info("LOW MEMORY DETECTED!!!");
                LOGGER.info("LOW MEMORY DETECTED!!!");
                LOGGER.info("LOW MEMORY DETECTED!!!");
                LOGGER.info("LOW MEMORY DETECTED!!!");
                LOGGER.info("LOW MEMORY DETECTED!!!");
                LOGGER.info("LOW MEMORY DETECTED!!!");
                LOGGER.info("LOW MEMORY DETECTED!!!");
                LOGGER.info("LOW MEMORY DETECTED!!!");
                LOGGER.info("LOW MEMORY DETECTED!!!");
                LOGGER.info("LOW MEMORY DETECTED!!!");
                LOGGER.info("LOW MEMORY DETECTED!!!");
                LOGGER.info("LOW MEMORY DETECTED!!!");
                break;
            }
        }

        UtilsT.startRendering();
        UtilsContextT.waitUntilRenderingDone(this.activity);

        /*UtilsT.assertRenderButtonText(Constants.RENDER);
        UtilsT.testStateAndBitmap(true);*/
    }
}
