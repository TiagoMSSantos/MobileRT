package puscas.mobilertapp.constants;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java8.util.J8Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

/**
 * The available acceleration structures for the Ray Tracer engine.
 */
@RequiredArgsConstructor
@Log
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
        log.info(ConstantsMethods.GET_NAMES);

        return J8Arrays.stream(values())
            .map(Accelerator::getName)
            .toArray(String[]::new);
    }
}
