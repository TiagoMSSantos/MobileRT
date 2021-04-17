package puscas.mobilertapp.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.logging.Logger;
import java8.util.J8Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;

/**
 * The available acceleration structures for the Ray Tracer engine.
 */
@RequiredArgsConstructor
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
     * The name of the acceleration structure.
     */
    @Getter
    private final String name;

    /**
     * Gets the names of all available accelerators.
     */
    @Contract(pure = true)
    @NonNull
    public static String[] getNames() {
        LOGGER.info(ConstantsMethods.GET_NAMES);

        return J8Arrays.stream(values())
            .map(Accelerator::getName)
            .toArray(String[]::new);
    }
}
