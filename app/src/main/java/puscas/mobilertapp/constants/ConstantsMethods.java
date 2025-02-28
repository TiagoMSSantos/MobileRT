package puscas.mobilertapp.constants;

/**
 * Utility class with the text constants for the names of methods.
 */
public final class ConstantsMethods {

    /**
     * Private constructor to avoid creating instances.
     */
    private ConstantsMethods() {
        throw new UnsupportedOperationException("Not implemented.");
    }

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
     * The name of the timer field.
     */
    public static final String TIMER = "RenderTask timer";

    /**
     * The name of the "getNames" in the {@link Enum} methods.
     */
    public static final String GET_NAMES = "getNames";
}
