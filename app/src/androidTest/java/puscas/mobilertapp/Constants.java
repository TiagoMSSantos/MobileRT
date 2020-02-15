package puscas.mobilertapp;

import android.widget.Button;
import org.junit.jupiter.api.Assertions;
import java.util.logging.Logger;

import static puscas.mobilertapp.ConstantsUI.SDCARD_EMULATOR_NAME;

/**
 * Some text messages used in the Android tests.
 */
final class Constants {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(Constants.class.getName());

    /**
     * The instrumentation tests constants.
     */
    private static final String OBJ_PATH = "/storage/" + SDCARD_EMULATOR_NAME + "/WavefrontOBJs";

    /**
     * The path to the conference OBJ file which should exist.
     */
    static final String OBJ_FILE_CONFERENCE = OBJ_PATH + "/conference/conference.obj";

    /**
     * The path to the teapot2 OBJ file which should not exist.
     */
    static final String OBJ_FILE_NOT_EXISTS = OBJ_PATH + "/teapot/teapot2.obj";

    /**
     * The path to the teapot OBJ file which should exist.
     */
    static final String OBJ_FILE_TEAPOT = OBJ_PATH + "/teapot/teapot.obj";

    /**
     * An empty path which should not point to a file.
     */
    static final String EMPTY_FILE = "";

    /**
     * The {@link Assertions} message used when a file should exist.
     */
    static final String FILE_SHOULD_EXIST = "File should exist!";

    /**
     * A render {@link Button} text message.
     */
    static final String RENDER = "Render";

    /**
     * A render {@link Button} text message.
     */
    static final String STOP = "Stop";

    /**
     * The preview {@link android.widget.CheckBox} text message.
     */
    static final String PREVIEW = "Preview";

    /**
     * The {@link Assertions} message used when checking the preview {@link android.widget.CheckBox} text message.
     */
    static final String CHECK_BOX_MESSAGE = "Check box message";

    /**
     * The name of {@link MainActivityTest#setUpAll()} method.
     */
    static final String SET_UP_ALL = "setUpAll";

    /**
     * The name of {@link MainActivityTest#setUp()} method.
     */
    static final String SET_UP = "setUp";

    /**
     * The name of {@link MainActivityTest#tearDownAll()} method.
     */
    static final String TEAR_DOWN_ALL = "tearDownAll";

    /**
     * The name of {@link MainActivityTest#tearDown()} method.
     */
    static final String TEAR_DOWN = "tearDown";

    /**
     * A private constructor in order to prevent instantiating this helper class.
     */
    private Constants() {
        LOGGER.info("Constants");
    }
}
