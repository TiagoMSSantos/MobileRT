package puscas.mobilertapp.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * The unit tests for the {@link UtilsLogging} util class.
 */
public class UtilsLoggingTest {

    /**
     * Tests the {@link UtilsLogging#printStackTrace()} method.
     */
    @Test
    public void testPrintStackTrace() {
        Assertions.assertThatCode(UtilsLogging::printStackTrace)
            .as("The UtilsLogging#printStackTrace method")
            .doesNotThrowAnyException();
    }
}
