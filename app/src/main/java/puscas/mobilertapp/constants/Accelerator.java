package puscas.mobilertapp.constants;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.util.logging.Logger;

import java8.util.J8Arrays;

/**
 * The available acceleration structures for the Ray Tracer engine.
 */
public enum Accelerator {

    /**
     * Nothing. So it doesn't even render the scene.
     */
    NONE("None"),

    /**
     * No accelerator.
     */
    NAIVE("Naive"),

    /**
     * The regular grid accelerator.
     */
    REG_GRID("RegGrid"),

    /**
     * The bounding volume hierarchy accelerator.
     */
    BVH("BVH");

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(Accelerator.class.getSimpleName());

    /**
     * The name of the acceleration structure.
     */
    private final String name;

    /**
     * The constructor.
     *
     * @param name The name.
     */
    Accelerator(final String name) {
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
     * Gets the names of all available accelerators.
     */
    @Contract(pure = true)
    @NonNull
    public static String[] getNames() {
        logger.info(ConstantsMethods.GET_NAMES);

        return J8Arrays.stream(values())
            .map(Accelerator::getName)
            .toArray(String[]::new);
    }
}
