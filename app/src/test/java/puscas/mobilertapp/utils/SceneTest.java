package puscas.mobilertapp.utils;

import lombok.extern.java.Log;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import puscas.mobilertapp.constants.Scene;

/**
 * The unit tests for the {@link Scene} util class.
 */
@Log
final class SceneTest {

    /**
     * Tests that the {@link Scene#getNames()} method contains all the expected scenes.
     */
    @Test
    void testGetNames() {
        Assertions.assertThat(Scene.getNames())
        .as("Check available scenes.")
        .containsExactly(
            "Cornell",
            "Spheres",
            "Cornell2",
            "Spheres2",
            "OBJ",
            "Test internal",
            "Test SD card",
            "Wrong file"
        );
    }

}
