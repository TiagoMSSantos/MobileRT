package puscas.mobilertapp.constants;

import lombok.experimental.UtilityClass;

/**
 * Utility class with the text constants for the {@link android.widget.Toast}.
 */
@UtilityClass
public final class ConstantsToast {

    /**
     * A warning message for when the Android device doesn't have a File Manager
     * application.
     */
    public static final String PLEASE_INSTALL_FILE_MANAGER = "Please install a File Manager.";

    /**
     * A warning message for when the Android device doesn't have enough memory
     * to render the scene.
     */
    public static final String DEVICE_WITHOUT_ENOUGH_MEMORY =
        "Device without enough memory to render the scene!\n";

    /**
     * A warning message for when the Android device couldn't load the scene.
     */
    public static final String COULD_NOT_LOAD_THE_SCENE = "Could not load the scene!\n";

    /**
     * A warning message for when the Android device couldn't render the scene.
     */
    public static final String COULD_NOT_RENDER_THE_SCENE = "Could not render the scene!\n";

}
