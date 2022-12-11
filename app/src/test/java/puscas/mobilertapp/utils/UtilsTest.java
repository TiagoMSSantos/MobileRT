package puscas.mobilertapp.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * The unit tests for the {@link Utils} util class.
 */
public class UtilsTest {

    /**
     * Tests that it's not possible to instantiate {@link Utils}.
     *
     * @throws NoSuchMethodException If Java reflection fails when using the private constructor.
     */
    @Test
    public void testDefaultUtils() throws NoSuchMethodException {
        final Constructor<Utils> constructor = Utils.class.getDeclaredConstructor();
        Assertions.assertThat(Modifier.isPrivate(constructor.getModifiers()))
            .as("The constructor is private")
            .isTrue();
        constructor.setAccessible(true);
        Assertions.assertThatThrownBy(constructor::newInstance)
            .as("The default constructor of Utils")
            .isNotNull()
            .isInstanceOf(InvocationTargetException.class);
    }

}
