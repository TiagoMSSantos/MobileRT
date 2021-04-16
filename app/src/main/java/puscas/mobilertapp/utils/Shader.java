package puscas.mobilertapp.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.logging.Logger;
import java8.util.J8Arrays;
import org.jetbrains.annotations.Contract;

/**
 * The available shaders for the Ray Tracer engine.
 */
public enum Shader {

    /**
     * The NoShadows shader.
     */
    NO_SHADOWS("NoShadows"),

    /**
     * The Whitted shader.
     */
    WHITTED("Whitted"),

    /**
     * The Path Tracing shader.
     */
    PATH_TRACING("PathTracing"),

    /**
     * The DepthMap shader.
     */
    DEPTH_MAP("DepthMap"),

    /**
     * The Diffuse shader.
     */
    DIFFUSE("Diffuse");

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(Shader.class.getName());

    /**
     * The name.
     *
     * @see Shader#getName()
     */
    private final String name;

    /**
     * The constructor for this {@link Enum}.
     *
     * @param name The new shader for the Ray Tracer engine.
     */
    Shader(final String name) {
        this.name = name;
    }

    /**
     * Gets the names of all available shaders.
     */
    @NonNull
    public static String[] getNames() {
        LOGGER.info(ConstantsMethods.GET_NAMES);

        return J8Arrays.stream(values())
            .map(Shader::getName)
            .toArray(String[]::new);
    }

    /**
     * Gets the name of the shader for the Ray Tracer engine.
     */
    @Contract(pure = true)
    private String getName() {
        return this.name;
    }
}
