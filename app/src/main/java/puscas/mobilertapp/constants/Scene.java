package puscas.mobilertapp.constants;

import androidx.annotation.NonNull;

import java.util.logging.Logger;

import java8.util.J8Arrays;

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
    OBJ("OBJ");

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(Scene.class.getSimpleName());

    /**
     * The name of the scene.
     */
    private final String name;

    /**
     * The constructor.
     *
     * @param name The name.
     */
    Scene(final String name) {
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
     * Gets the names of all available scenes.
     */
    @NonNull
    public static String[] getNames() {
        logger.info(ConstantsMethods.GET_NAMES);

        return J8Arrays.stream(values())
            .map(Scene::getName)
            .toArray(String[]::new);
    }
}
