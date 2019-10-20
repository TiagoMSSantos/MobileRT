package puscas.mobilertapp;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

class MainActivityTest {


    @BeforeAll
    static void setUpAll() {
    }

    @BeforeEach
    void setup() {
    }

    @Test
    void succeedingTest() {
        final File file = new File("/storage/1D19-170B/WavefrontOBJs/conference/conference.obj");

        Assertions.assertTrue(file.exists());
        Assertions.assertTrue(file.canRead());
    }


    @AfterEach
    void tearDown() {
    }

    @AfterAll
    static void tearDownAll() {
    }

    @Test
    void testFileNotExists() {
        final File file = new File("");

        Assertions.assertFalse(file.exists());
        Assertions.assertFalse(file.canRead());
    }
}
