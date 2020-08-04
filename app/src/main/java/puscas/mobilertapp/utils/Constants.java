package puscas.mobilertapp.utils;

import android.widget.Button;

import org.jetbrains.annotations.NonNls;

import java.util.logging.Logger;

/**
 * Utility class with some constants for the Android interface.
 */
public final class Constants {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(Constants.class.getName());

    /**
     * The path to the MobileRT instrumentation tests data.
     */
    private static final String RESOURCES_PATH =
        ConstantsUI.FILE_SEPARATOR + "data" + ConstantsUI.FILE_SEPARATOR + "MobileRT";

    /**
     * The relative path to the WavefrontOBJs path for the instrumentation tests.
     */
    private static final String OBJ_PATH =
        RESOURCES_PATH + ConstantsUI.FILE_SEPARATOR + "WavefrontOBJs";

    /**
     * The path to the teapot OBJ file which should exist.
     */
    public static final String OBJ_FILE_TEAPOT = OBJ_PATH +
        ConstantsUI.FILE_SEPARATOR + "teapot" + ConstantsUI.FILE_SEPARATOR + "teapot.obj";

    /**
     * The path to the teapot2 OBJ file which should not exist.
     */
    public static final String OBJ_FILE_NOT_EXISTS = OBJ_PATH +
        ConstantsUI.FILE_SEPARATOR + "teapot" + ConstantsUI.FILE_SEPARATOR + "teapot2" + ".obj";

    /**
     * An empty path which should not point to a file.
     */
    public static final String EMPTY_FILE = "";

    /**
     * The assert message used when a file should exist.
     */
    public static final String FILE_SHOULD_EXIST = "File should exist";

    /**
     * A render {@link Button} text message.
     */
    @NonNls public static final String RENDER = "Render";

    /**
     * A render {@link Button} text message.
     */
    public static final String STOP = "Stop";

    /**
     * The preview {@link android.widget.CheckBox} text message.
     */
    public static final String PREVIEW = "Preview";

    /**
     * The assert message used when checking the preview
     * {@link android.widget.CheckBox} text message.
     */
    public static final String CHECK_BOX_MESSAGE = "Check box message";

    /**
     * The number of bytes in a mega byte.
     */
    public static final int BYTES_IN_MEGABYTE = 1048576;

    /**
     * The number of bytes in an integer (usually is 4 bytes).
     */
    static final int BYTES_IN_INTEGER =  Integer.SIZE / Byte.SIZE;

    /**
     * The number of bytes in a float (usually is 4 bytes).
     */
    public static final int BYTES_IN_FLOAT =  Float.SIZE / Byte.SIZE;

    /**
     * The number of bytes of a memory pointer address.
     *
     * For a 32bit system is usually 4 bytes.
     * For a 64bit system is usually 8 bytes.
     *
     * For now its assumed the worst case scenario, which is 8 bytes.
     */
    static final int BYTES_IN_POINTER =  Double.SIZE / Byte.SIZE;

    /**
     * A private constructor in order to prevent instantiating this helper class.
     */
    private Constants() {
        LOGGER.info("Constants");
    }
}
