package puscas.mobilertapp;

import android.view.View;

import java.util.logging.Logger;

/**
 * Utility class with the text constants for the names of methods.
 */
final class ConstantsMethods {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(ConstantsMethods.class.getName());

    /**
     * The name of the
     * {@link DrawView#renderScene(int, int, int, int, int, int, int, int, String, String, String, boolean)} method.
     */
    static final String RENDER_SCENE = "renderScene";

    /**
     * The name of the {@link MainActivity#startRender(View)} method.
     */
    static final String START_RENDER = "startRender";

    /**
     * The name of the {@link MainActivity#onDestroy()} method.
     */
    static final String ON_DESTROY = "onDestroy";

    /**
     * The name of the {@link MainActivity#onDetachedFromWindow()} and {@link DrawView#onDetachedFromWindow()} method.
     */
    static final String ON_DETACHED_FROM_WINDOW = "onDetachedFromWindow";

    /**
     * The name of the {@link RenderTask#onCancelled()} and {@link RenderTask#onCancelled(Void)} methods.
     */
    static final String ON_CANCELLED = "onCancelled";

    /**
     * The name of the "getNames" in the {@link Enum} methods.
     */
    static final String GET_NAMES = "getNames";

    /**
     * A private constructor in order to prevent instantiating this helper class.
     */
    private ConstantsMethods() {
        LOGGER.info("ConstantsMethods");
    }
}
