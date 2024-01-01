package puscas.mobilertapp.constants;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * The unit tests for the {@link ConstantsError} util class.
 */
public final class ConstantsErrorTest {

    /**
     * Tests that it's not possible to instantiate {@link ConstantsError}.
     *
     * @throws NoSuchMethodException If Java reflection fails when using the private constructor.
     */
    @Test
    public void testDefaultConstantsError() throws NoSuchMethodException {
        final Constructor<ConstantsError> constructor = ConstantsError.class.getDeclaredConstructor();
        Assertions.assertThat(Modifier.isPrivate(constructor.getModifiers()))
            .as("The constructor is private")
            .isTrue();
        constructor.setAccessible(true);
        Assertions.assertThatThrownBy(() -> constructor.newInstance())
            .as("The default constructor of ConstantsError")
            .isNotNull()
            .isInstanceOf(InvocationTargetException.class);
    }

}
