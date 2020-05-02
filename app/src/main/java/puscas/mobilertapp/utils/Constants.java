package puscas.mobilertapp.utils;

import android.widget.Button;

import org.junit.jupiter.api.Assertions;

import java.util.logging.Logger;

/**
 * Some text messages used in the Android tests.
 */
public final class Constants {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(Constants.class.getName());

    /**
     * The instrumentation tests constants.
     */
    private static final String OBJ_PATH = ConstantsUI.FILE_SEPARATOR + "WavefrontOBJs";

    /**
     * The path to the conference OBJ file which should exist.
     */
    public static final String OBJ_FILE_CONFERENCE =
        OBJ_PATH + ConstantsUI.FILE_SEPARATOR + "conference" + ConstantsUI.FILE_SEPARATOR + "conference.obj";

    /**
     * The path to the teapot2 OBJ file which should not exist.
     */
    public static final String OBJ_FILE_NOT_EXISTS =
        OBJ_PATH + ConstantsUI.FILE_SEPARATOR + "teapot" + ConstantsUI.FILE_SEPARATOR + "teapot2" + ".obj";

    /**
     * The path to the teapot OBJ file which should exist.
     */
    public static final String OBJ_FILE_TEAPOT =
        OBJ_PATH + ConstantsUI.FILE_SEPARATOR + "teapot" + ConstantsUI.FILE_SEPARATOR + "teapot.obj";

    /**
     * An empty path which should not point to a file.
     */
    public static final String EMPTY_FILE = "";

    /**
     * The {@link Assertions} message used when a file should exist.
     */
    public static final String FILE_SHOULD_EXIST = "File should exist!";

    /**
     * A render {@link Button} text message.
     */
    public static final String RENDER = "Render";

    /**
     * A render {@link Button} text message.
     */
    public static final String STOP = "Stop";

    /**
     * The preview {@link android.widget.CheckBox} text message.
     */
    public static final String PREVIEW = "Preview";

    /**
     * The {@link Assertions} message used when checking the preview
     * {@link android.widget.CheckBox} text message.
     */
    public static final String CHECK_BOX_MESSAGE = "Check box message";

    /**
     * The name of setUpAll method.
     */
    public static final String SET_UP_ALL = "setUpAll";

    /**
     * The name of setUp method.
     */
    public static final String SET_UP = "setUp";

    /**
     * The name of tearDownAll method.
     */
    public static final String TEAR_DOWN_ALL = "tearDownAll";

    /**
     * The name of tearDown method.
     */
    public static final String TEAR_DOWN = "tearDown";

    /**
     * The name of testGetNames method.
     */
    public static final String TEST_GET_NAMES = "testGetNames";

    /**
     * A private constructor in order to prevent instantiating this helper class.
     */
    private Constants() {
        LOGGER.info("Constants");
    }
}
