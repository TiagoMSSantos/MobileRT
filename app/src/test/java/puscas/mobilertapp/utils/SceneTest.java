package puscas.mobilertapp.utils;

import lombok.extern.java.Log;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import puscas.mobilertapp.constants.Scene;

/**
 * The unit tests for the {@link Scene} util class.
 */
@Log
public final class SceneTest {

    /**
     * Tests that the {@link Scene#getNames()} method contains all the expected scenes.
     */
    @Test
    public void testGetNames() {
        Assertions.assertThat(Scene.getNames()).containsExactly(
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
