package puscas.mobilertapp.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import java8.util.J8Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

/**
 * The available scenes for the Ray Tracer engine.
 */
@RequiredArgsConstructor
@Log
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
     * A test scene from the internal storage.
     */
    TEST_INTERNAL_STORAGE("Test internal"),

    /**
     * A test scene from the external SD card.
     */
    TEST_SD_CARD("Test SD card"),

    /**
     * A scene of an OBJ file which doesn't exist.
     */
    WRONG_FILE("Wrong file");

    /**
     * The name of the scene.
     */
    @Getter
    private final String name;

    /**
     * Gets the names of all available scenes.
     */
    @NonNull
    public static String[] getNames() {
        log.info(ConstantsMethods.GET_NAMES);

        return J8Arrays.stream(values())
            .map(Scene::getName)
            .toArray(String[]::new);
    }
}
