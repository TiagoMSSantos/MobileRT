package puscas.mobilertapp.utils;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * The unit tests for the {@link UtilsGlMatrices} util class.
 */
public final class UtilsGlMatricesTest {

    /**
     * Tests that it's not possible to instantiate {@link UtilsGlMatrices}.
     *
     * @throws NoSuchMethodException If Java reflection fails when using the private constructor.
     */
    @Test
    public void testDefaultUtilsGlMatrices() throws NoSuchMethodException {
        final Constructor<UtilsGlMatrices> constructor = UtilsGlMatrices.class.getDeclaredConstructor();
        Assertions.assertThat(Modifier.isPrivate(constructor.getModifiers()))
            .as("The constructor is private")
            .isTrue();
        constructor.setAccessible(true);
        Assertions.assertThatThrownBy(constructor::newInstance)
            .as("The default constructor of ConfigRenderTask")
            .isNotNull()
            .isInstanceOf(InvocationTargetException.class);
    }

}
