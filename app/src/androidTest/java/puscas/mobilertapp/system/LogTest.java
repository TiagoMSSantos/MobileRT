package puscas.mobilertapp.system;

import android.util.Log;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * The Android tests for logging.
 */
@lombok.extern.java.Log
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class LogTest {

    /**
     * A setup method which is called first.
     */
    @BeforeClass
    public static void setUpAll() {
        Log.d("Test", "Android_log_test_setupAll");
        log.info("Lombok_log_test_setupAll");
    }

    /**
     * A tear down method which is called last.
     */
    @AfterClass
    public static void tearDownAll() {
        Log.d("Test", "Android_log_test_tearDownAll");
        log.info("Lombok_log_test_tearDownAll");
    }

    /**
     * Setup method called before each test.
     */
    @Before
    public void setUp() {
        Log.d("Test", "Android_log_test_setUp");
        log.info("Lombok_log_test_setUp");
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        Log.d("Test", "Android_log_test_tearDown");
        log.info("Lombok_log_test_tearDown");
    }

    /**
     * Tests that the Android logging in Android Instrumentation Tests works properly.
     */
    @Test
    public void testLogging() {
        Log.d("Test", "Android_log_test");
        log.info("Lombok_log_test");
    }
}
