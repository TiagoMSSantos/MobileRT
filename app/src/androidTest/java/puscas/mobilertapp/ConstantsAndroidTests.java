package puscas.mobilertapp;

import android.util.Log;

import java.io.File;

/**
 * Helper class which contains helper constants for the tests.
 */
public final class ConstantsAndroidTests {

    /**
     * Private constructor to avoid creating instances.
     */
    private ConstantsAndroidTests() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * The message for the assertions stating that there not enough (main) memory available.
     */
    public static final String NOT_ENOUGH_MEMORY_MESSAGE = "Not enough available memory: ";

    /**
     * The message for the assertions about the message for the rendering button.
     */
    public static final String BUTTON_MESSAGE = "Button message is not the expected.";

    /**
     * The {@link Log} message to be shown when mocking the external file manager application reply.
     */
    public static final String MOCK_FILE_MANAGER_REPLY = "Mocking the reply as the external file manager application, to select an OBJ file.";

    /**
     * The path to the OBJ {@link File} of Cornell Box Water scene.
     */
    public static final String CORNELL_BOX_WATER_OBJ = "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj";

    /**
     * The path to the MTL {@link File} of Cornell Box Water scene.
     */
    public static final String CORNELL_BOX_WATER_MTL = "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.mtl";

    /**
     * The path to the CAM {@link File} of Cornell Box Water scene.
     */
    public static final String CORNELL_BOX_WATER_CAM = "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.cam";
}
