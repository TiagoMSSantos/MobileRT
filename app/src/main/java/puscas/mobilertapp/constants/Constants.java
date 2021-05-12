package puscas.mobilertapp.constants;

import android.widget.Button;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NonNls;

/**
 * Utility class with some constants for the Android interface.
 */
@UtilityClass
public final class Constants {

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
    @NonNls
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
     * The assert message used when checking the preview
     * {@link android.widget.CheckBox} text message.
     */
    public static final String CHECK_BOX_MESSAGE = "Check box message";

    /**
     * The number of bytes in a mega byte.
     */
    public static final int BYTES_IN_MEGABYTE = 1048576;

    /**
     * The number of bytes in a float (usually is 4 bytes).
     */
    public static final int BYTES_IN_FLOAT = Float.SIZE / Byte.SIZE;

    /**
     * The name of the folder which contains the data for MobileRT.
     */
    public static final String MOBILERT_FOLDER_NAME = "MobileRT";

    /**
     * The name of the folder which contains the OBJ files.
     */
    public static final String OBJ_FOLDER_NAME = "WavefrontOBJs";

    /**
     * The path to the Cornell Box OBJ file which should exist in the SD card.
     */
    public static final String OBJ_FILE_CORNELL_BOX = MOBILERT_FOLDER_NAME
        + ConstantsUI.FILE_SEPARATOR + OBJ_FOLDER_NAME
        + ConstantsUI.FILE_SEPARATOR + "CornellBox" + ConstantsUI.FILE_SEPARATOR
        + "CornellBox-Water.obj";

    /**
     * The name of the teapot model.
     */
    private static final String TEAPOT = "teapot";

    /**
     * The path to the teapot2 OBJ file which should not exist in the SD card.
     */
    public static final String OBJ_FILE_NOT_EXISTS_SD_CARD = OBJ_FOLDER_NAME
        + ConstantsUI.FILE_SEPARATOR + TEAPOT + ConstantsUI.FILE_SEPARATOR + "teapot2" + ".obj";

    /**
     * The number of bytes in an integer (usually is 4 bytes).
     */
    public static final int BYTES_IN_INTEGER = Integer.SIZE / Byte.SIZE;

    /**
     * The number of bytes of a memory pointer address.
     * <br>
     * For a 32bit system is usually 4 bytes.
     * For a 64bit system is usually 8 bytes.
     * <br>
     * For now its assumed the worst case scenario, which is 8 bytes.
     */
    public static final int BYTES_IN_POINTER = Double.SIZE / Byte.SIZE;

    /**
     * The relative path to the WavefrontOBJs path for the instrumentation tests.
     */
    private static final String OBJ_PATH =
        ConstantsUI.FILE_SEPARATOR + MOBILERT_FOLDER_NAME +
        ConstantsUI.FILE_SEPARATOR + OBJ_FOLDER_NAME;

    /**
     * The path to the teapot OBJ file which should exist.
     */
    public static final String OBJ_FILE_TEAPOT = OBJ_PATH
        + ConstantsUI.FILE_SEPARATOR + TEAPOT + ConstantsUI.FILE_SEPARATOR + "teapot.obj";

    /**
     * The path to the teapot2 OBJ file which should not exist in the Android device.
     */
    public static final String OBJ_FILE_NOT_EXISTS = OBJ_PATH
        + ConstantsUI.FILE_SEPARATOR + TEAPOT + ConstantsUI.FILE_SEPARATOR + "teapot2.obj";

}
