package puscas.mobilertapp.utils;

import org.jetbrains.annotations.Contract;

import java.util.logging.Logger;

import javax.annotation.Nonnull;

import java8.util.J8Arrays;

/**
 * The available shaders for the Ray Tracer engine.
 */
public enum Shader {

    /**
     * The NoShadows shader.
     */
    NOSHADOWS("NoShadows"),

    /**
     * The Whitted shader.
     */
    WHITTED("Whitted"),

    /**
     * The PathTracer shader.
     */
    PATHTRACER("PathTracer"),

    /**
     * The DepthMap shader.
     */
    DEPTHMAP("DepthMap"),

    /**
     * The Diffuse shader.
     */
    DIFFUSE("Diffuse");

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(Shader.class.getName());

    /**
     * @see Shader#getName()
     */
    private final String name;

    /**
     * The constructor for this {@link Enum}.
     *
     * @param name The new shader for the Ray Tracer engine.
     */
    @Contract(pure = true)
    Shader(final String name) {
        this.name = name;
    }

    /**
     * Gets the name of the shader for the Ray Tracer engine.
     */
    @Contract(pure = true)
    private String getName() {
        return this.name;
    }

    /**
     * Gets the names of all available shaders.
     */
    @Nonnull
    public static String[] getNames() {
        LOGGER.info(ConstantsMethods.GET_NAMES);

        return J8Arrays.stream(values())
            .map(Shader::getName)
            .toArray(String[]::new);
    }
}
