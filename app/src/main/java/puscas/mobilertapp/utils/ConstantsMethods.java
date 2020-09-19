package puscas.mobilertapp.utils;

import java.util.logging.Logger;

/**
 * Utility class with the text constants for the names of methods.
 */
public final class ConstantsMethods {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(ConstantsMethods.class.getName());

    /**
     * The constant used when a method is about to return.
     */
    public static final String FINISHED = " finished";

    /**
     * The name of the setBitmap() method.
     */
    public static final String SET_BITMAP = "setBitmap";

    /**
     * The name of the renderScene(Config, int, boolean) method.
     */
    public static final String RENDER_SCENE = "renderScene";

    /**
     * The name of the startRender(View) method.
     */
    public static final String START_RENDER = "startRender";

    /**
     * The name of the onDestroy() method.
     */
    public static final String ON_DESTROY = "onDestroy";

    /**
     * The name of the onDetachedFromWindow() method.
     */
    public static final String ON_DETACHED_FROM_WINDOW = "onDetachedFromWindow";

    /**
     * The name of the onCancelled() method.
     */
    public static final String ON_CANCELLED = "onCancelled";

    /**
     * The name of the timer field.
     */
    public static final String TIMER = "RenderTask timer";

    /**
     * The name of the "getNames" in the {@link Enum} methods.
     */
    static final String GET_NAMES = "getNames";

    /**
     * The name of the {@link UtilsGL#run} methods.
     */
    static final String RUN = "run";

    /**
     * A private constructor in order to prevent instantiating this helper class.
     */
    private ConstantsMethods() {
        LOGGER.info("ConstantsMethods");
    }

}
