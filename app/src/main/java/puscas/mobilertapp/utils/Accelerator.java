package puscas.mobilertapp.utils;

import java.util.logging.Logger;
import java8.util.J8Arrays;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Contract;

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
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(Accelerator.class.getName());

    /**
     * @see Accelerator#getName()
     */
    private final String name;

    /**
     * The constructor for this {@link Enum}.
     *
     * @param name The name of the acceleration structure for the Ray Tracer engine.
     */
    @Contract(pure = true)
    @Nonnull Accelerator(final String name) {
        this.name = name;
    }

    /**
     * Gets the names of all available accelerators.
     */
    @Contract(pure = true)
    @Nonnull
    public static String[] getNames() {
        LOGGER.info(ConstantsMethods.GET_NAMES);

        return J8Arrays.stream(values())
            .map(Accelerator::getName)
            .toArray(String[]::new);
    }

    /**
     * Gets the name of the accelerator for the Ray Tracer engine.
     */
    @Contract(pure = true)
    private String getName() {
        return this.name;
    }
}
