package puscas.mobilertapp.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * The unit tests for the {@link UtilsShader} util class.
 */
public final class UtilsShaderTest {

    /**
     * Tests that it's not possible to instantiate {@link UtilsShader}.
     *
     * @throws NoSuchMethodException If Java reflection fails when using the private constructor.
     */
    @Test
    public void testDefaultUtilsShader() throws NoSuchMethodException {
        final Constructor<UtilsShader> constructor = UtilsShader.class.getDeclaredConstructor();
        Assertions.assertThat(Modifier.isPrivate(constructor.getModifiers()))
            .as("The constructor is private")
            .isTrue();
        constructor.setAccessible(true);
        Assertions.assertThatThrownBy(() -> constructor.newInstance())
            .as("The default constructor of UtilsShader")
            .isNotNull()
            .isInstanceOf(InvocationTargetException.class);
    }

}
