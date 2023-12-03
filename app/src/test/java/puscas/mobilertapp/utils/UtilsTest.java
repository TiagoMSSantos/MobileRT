package puscas.mobilertapp.utils;

import android.widget.NumberPicker;

import org.assertj.core.api.Assertions;
import org.easymock.EasyMock;
import org.junit.Test;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import puscas.mobilertapp.exceptions.FailureException;

/**
 * The unit tests for the {@link Utils} util class.
 */
public final class UtilsTest {

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

    /**
     * Tests that {@link Utils#readTextFromInputStream(InputStream)} method will throw a
     * {@link FailureException} if it occurs an error while reading the file.
     */
    @Test
    public void testReadTextFromInputStreamFail() {
        final InputStream inputStreamMocked = EasyMock.mock(InputStream.class);

        Assertions.assertThatThrownBy(() -> Utils.readTextFromInputStream(inputStreamMocked))
            .as("The call to Utils#readTextFromInputStream method")
            .isInstanceOf(FailureException.class);
    }

    /**
     * Tests that {@link Utils#getValueFromPicker(NumberPicker)} method will throw a
     * {@link FailureException} if it occurs an error while parsing the {@link NumberPicker}.
     */
    @Test
    public void testGetValueFromPickerFail() {
        final NumberPicker numberPickerMocked = EasyMock.mock(NumberPicker.class);

        Assertions.assertThatThrownBy(() -> Utils.getValueFromPicker(numberPickerMocked))
            .as("The call to Utils#getValueFromPicker method")
            .isInstanceOf(FailureException.class);
    }

    /**
     * Tests that the method {@link Utils#getResolutionFromPicker(NumberPicker)} will throw a
     * {@link FailureException}.
     */
    @Test
    public void testGetResolutionFromPickerFail() {
        final NumberPicker numberPickerMocked = EasyMock.mock(NumberPicker.class);

        Assertions.assertThatThrownBy(() ->Utils.getResolutionFromPicker(numberPickerMocked))
            .as("The call to Utils#getResolutionFromPicker method")
            .isInstanceOf(FailureException.class);
    }

    /**
     * Tests that the method {@link Utils#getResolutionFromPicker(NumberPicker)} does not throw any
     * {@link Exception} even if the the {@link ExecutorService#awaitTermination(long, TimeUnit)}
     * method throws an {@link Exception}.
     *
     * @throws InterruptedException If the mock fails.
     */
    @Test
    public void testWaitExecutorToFinishInterruptsThread() throws InterruptedException {
        final ExecutorService numberPickerMocked = EasyMock.mock(ExecutorService.class);

        EasyMock.expect(numberPickerMocked.awaitTermination(1L, TimeUnit.DAYS))
            .andReturn(false)
            .andThrow(new InterruptedException("test"))
            .andReturn(true);

        EasyMock.replay(numberPickerMocked);
        Assertions.assertThatCode(() ->Utils.waitExecutorToFinish(numberPickerMocked))
            .as("The call to Utils#waitExecutorToFinish method")
            .doesNotThrowAnyException();
    }
}
