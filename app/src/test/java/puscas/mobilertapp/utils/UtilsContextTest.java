package puscas.mobilertapp.utils;

import static org.junit.Assert.*;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * The unit tests for the {@link UtilsContext} util class.
 */
public class UtilsContextTest {

    /**
     * Tests that it's not possible to instantiate {@link UtilsContext}.
     *
     * @throws NoSuchMethodException If Java reflection fails when using the private constructor.
     */
    @Test
    public void testDefaultUtilsContext() throws NoSuchMethodException {
        final Constructor<UtilsContext> constructor = UtilsContext.class.getDeclaredConstructor();
        Assertions.assertThat(Modifier.isPrivate(constructor.getModifiers()))
            .as("The constructor is private")
            .isTrue();
        constructor.setAccessible(true);
        Assertions.assertThatThrownBy(constructor::newInstance)
            .as("The default constructor of UtilsContext")
            .isNotNull()
            .isInstanceOf(InvocationTargetException.class);
    }

}
