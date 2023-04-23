package puscas.mobilertapp.constants;

import androidx.annotation.NonNull;

import java.util.logging.Logger;

import java8.util.J8Arrays;

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
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(Shader.class.getSimpleName());

    /**
     * The name of the shader for the Ray Tracer engine.
     */
    private final String name;

    /**
     * The constructor.
     *
     * @param name The name.
     */
    Shader(final String name) {
        this.name = name;
    }

    /**
     * Gets the name.
     *
     * @return The name.
     */
    private String getName() {
        return name;
    }

    /**
     * Gets the names of all available shaders.
     */
    @NonNull
    public static String[] getNames() {
        logger.info(ConstantsMethods.GET_NAMES);

        return J8Arrays.stream(values())
            .map(Shader::getName)
            .toArray(String[]::new);
    }
}
