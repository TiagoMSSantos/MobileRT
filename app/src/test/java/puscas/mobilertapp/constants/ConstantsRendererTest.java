package puscas.mobilertapp.constants;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * The unit tests for the {@link ConstantsRenderer} util class.
 */
public class ConstantsRendererTest {

    /**
     * Tests that it's not possible to instantiate {@link ConstantsRenderer}.
     *
     * @throws NoSuchMethodException If Java reflection fails when using the private constructor.
     */
    @Test
    public void testDefaultUtilsGlMatrices() throws NoSuchMethodException {
        final Constructor<ConstantsRenderer> constructor = ConstantsRenderer.class.getDeclaredConstructor();
        Assertions.assertThat(Modifier.isPrivate(constructor.getModifiers()))
            .as("The constructor is private")
            .isTrue();
        constructor.setAccessible(true);
        Assertions.assertThatThrownBy(constructor::newInstance)
            .as("The default constructor of ConstantsRenderer")
            .isNotNull()
            .isInstanceOf(InvocationTargetException.class);
    }

}
