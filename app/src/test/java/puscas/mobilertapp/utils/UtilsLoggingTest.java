package puscas.mobilertapp.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * The unit tests for the {@link UtilsLogging} util class.
 */
public final class UtilsLoggingTest {

    /**
     * Tests that it's not possible to instantiate {@link UtilsLogging}.
     *
     * @throws NoSuchMethodException If Java reflection fails when using the private constructor.
     */
    @Test
    public void testDefaultUtilsLogging() throws NoSuchMethodException {
        final Constructor<UtilsLogging> constructor = UtilsLogging.class.getDeclaredConstructor();
        Assertions.assertThat(Modifier.isPrivate(constructor.getModifiers()))
            .as("The constructor is private")
            .isTrue();
        constructor.setAccessible(true);
        Assertions.assertThatThrownBy(constructor::newInstance)
            .as("The default constructor of UtilsLogging")
            .isNotNull()
            .isInstanceOf(InvocationTargetException.class);
    }

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
