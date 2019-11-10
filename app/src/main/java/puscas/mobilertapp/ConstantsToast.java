package puscas.mobilertapp;

import java.util.logging.Logger;

/**
 * Utility class with the text constants for the {@link android.widget.Toast}.
 */
final class ConstantsToast {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(ConstantsToast.class.getName());

    /**
     * A warning message for when the Android device doesn't have a File Manager application.
     */
    static final String PLEASE_INSTALL_FILE_MANAGER = "Please install a File Manager.";

    /**
     * A warning message for when the Android device doesn't have enough memory to render the scene.
     */
    static final String DEVICE_WITHOUT_ENOUGH_MEMORY = "Device without enough memory to render the scene.";

    /**
     * A warning message for when the Android device couldn't load the scene.
     */
    static final String COULD_NOT_LOAD_THE_SCENE = "Could not load the scene.";

    /**
     * A private constructor in order to prevent instantiating this helper class.
     */
    private ConstantsToast() {
        LOGGER.info("ConstantsToast");
    }
}
