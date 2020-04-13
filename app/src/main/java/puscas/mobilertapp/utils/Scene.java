package puscas.mobilertapp.utils;

import org.jetbrains.annotations.Contract;

import java.util.logging.Logger;

import javax.annotation.Nonnull;

import java8.util.J8Arrays;

import static puscas.mobilertapp.utils.ConstantsMethods.GET_NAMES;

/**
 * The available scenes for the Ray Tracer engine.
 */
public enum Scene {

    /**
     * The Cornell Box scene.
     */
    CORNELL("Cornell"),

    /**
     * The Spheres scene.
     */
    SPHERES("Spheres"),

    /**
     * The 2nd Cornell Box scene.
     */
    CORNELL2("Cornell2"),

    /**
     * The 2nd Spheres scene.
     */
    SPHERES2("Spheres2"),

    /**
     * The Scene of an OBJ file.
     */
    OBJ("OBJ"),

    /**
     * A test scene.
     */
    TEST("Test"),

    /**
     * A scene of an OBJ file which doesn't exist.
     */
    WRONGFILE("Wrong file");

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(Scene.class.getName());

    /**
     * @see Scene#getName()
     */
    private final String name;

    /**
     * The constructor for this {@link Enum}.
     *
     * @param name The new scene for the Ray Tracer engine.
     */
    Scene(final String name) {
        this.name = name;
    }

    /**
     * Gets the name of the scene for the Ray Tracer engine.
     */
    @Contract(pure = true)
    private String getName() {
        return this.name;
    }

    /**
     * Gets the names of all available scenes.
     */
    @Nonnull
    public static String[] getNames() {
        LOGGER.info(GET_NAMES);

        return J8Arrays.stream(values())
            .map(Scene::getName)
            .toArray(String[]::new);
    }
}
