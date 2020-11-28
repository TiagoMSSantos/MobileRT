package puscas.mobilertapp.utils;

import java.util.logging.Logger;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The unit tests for the {@link Scene} util class.
 */
public final class SceneTest {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(SceneTest.class.getName());

    /**
     * Setup method called before each test.
     */
    @Before
    public void setUp() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Tests that the {@link Scene#getNames()} method contains all the expected scenes.
     */
    @Test
    public void testGetNames() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        Assertions.assertThat(Scene.getNames()).containsExactly(
            "Cornell",
            "Spheres",
            "Cornell2",
            "Spheres2",
            "OBJ",
            "Test internal",
            "Test SD card",
            "Wrong file"
        );
    }

}
