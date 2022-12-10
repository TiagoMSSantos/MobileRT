package puscas.mobilertapp.utils;

import static org.junit.Assert.*;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * The unit tests for the {@link UtilsGL} util class.
 */
public class UtilsGLTest {

    /**
     * Tests that it's not possible to instantiate {@link UtilsGL}.
     *
     * @throws NoSuchMethodException If Java reflection fails when using the private constructor.
     */
    @Test
    public void testDefaultUtilsGL() throws NoSuchMethodException {
        final Constructor<UtilsGL> constructor = UtilsGL.class.getDeclaredConstructor();
        Assertions.assertThat(Modifier.isPrivate(constructor.getModifiers()))
            .as("The constructor is private")
            .isTrue();
        constructor.setAccessible(true);
        Assertions.assertThatThrownBy(constructor::newInstance)
            .as("The default constructor of UtilsGL")
            .isNotNull()
            .isInstanceOf(InvocationTargetException.class);
    }

}
