package puscas.mobilertapp.utils;

import android.widget.NumberPicker;

import org.assertj.core.api.Assertions;
import org.easymock.EasyMock;
import org.easymock.MockType;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import kotlin.Pair;
import puscas.mobilertapp.constants.ConstantsUI;
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
        Assertions.assertThatThrownBy(() -> constructor.newInstance())
            .as("The default constructor of Utils")
            .isNotNull()
            .isInstanceOf(InvocationTargetException.class);
    }

    /**
     * Tests that {@link Utils#readTextFromInputStream(InputStream)} method will throw a
     * {@link FailureException} if it occurs an error while reading the file.
     */
    @Test
    public void testReadTextFromInputStreamFail() throws IOException {
        final InputStream inputStreamMocked = EasyMock.mock(InputStream.class);
        EasyMock.expect(inputStreamMocked.read(EasyMock.anyObject(), EasyMock.anyInt(), EasyMock.anyInt()))
            .andThrow(new IOException("Test error"))
            .anyTimes();

        EasyMock.replay(inputStreamMocked);
        Assertions.assertThatThrownBy(() -> Utils.readTextFromInputStream(inputStreamMocked))
            .as("The call to Utils#readTextFromInputStream method")
            .isInstanceOf(FailureException.class);
    }

    /**
     * Tests the method {@link Utils#readTextFromInputStream(InputStream)}.
     */
    @Test
    public void testReadTextFromInputStream() throws IOException {
        final InputStream inputStreamMocked = EasyMock.mock(MockType.NICE, InputStream.class);
        final String expectedString = "abc";
        final AtomicBoolean alreadyReadString = new AtomicBoolean(false);

        EasyMock.expect(inputStreamMocked.read(EasyMock.anyObject(), EasyMock.anyInt(), EasyMock.anyInt()))
            .andAnswer(() -> {
                if (!alreadyReadString.get()) {
                    alreadyReadString.set(true);
                    final byte[] inputParamToMutate = EasyMock.getCurrentArgument(0);
                    System.arraycopy(expectedString.getBytes(), 0, inputParamToMutate, 0, expectedString.length());
                    return expectedString.length();
                }
                return -1;
            })
            .anyTimes();

        EasyMock.replay(inputStreamMocked);
        final String output = Utils.readTextFromInputStream(inputStreamMocked);
        Assertions.assertThat(output)
            .as("The input stream")
            .isEqualTo(expectedString + ConstantsUI.LINE_SEPARATOR);
    }

    /**
     * Tests that {@link Utils#getValueFromPicker(NumberPicker)} method will throw a
     * {@link FailureException} if it occurs an error while parsing the {@link NumberPicker}.
     */
    @Test
    public void testGetValueFromPickerFail() {
        final NumberPicker numberPickerMocked = EasyMock.mock(MockType.NICE, NumberPicker.class);

        EasyMock.replay(numberPickerMocked);
        Assertions.assertThatThrownBy(() -> Utils.getValueFromPicker(numberPickerMocked))
            .as("The call to Utils#getValueFromPicker method")
            .isInstanceOf(FailureException.class);
    }

    /**
     * Tests that the method {@link Utils#getResolutionFromPicker(NumberPicker)} will throw a
     * {@link FailureException} if the {@link NumberPicker#getDisplayedValues()} does not contain
     * a resolution with the format: width x height.
     */
    @Test
    public void testGetResolutionFromPickerFail() {
        final NumberPicker numberPickerMocked = EasyMock.mock(MockType.NICE, NumberPicker.class);

        EasyMock.replay(numberPickerMocked);
        Assertions.assertThatThrownBy(() -> Utils.getResolutionFromPicker(numberPickerMocked))
            .as("The call to Utils#getResolutionFromPicker method")
            .isInstanceOf(FailureException.class);
    }

    /**
     * Tests the method {@link Utils#getResolutionFromPicker(NumberPicker)}.
     */
    @Test
    public void testGetResolutionFromPicker() {
        final NumberPicker numberPickerMocked = EasyMock.mock(MockType.NICE, NumberPicker.class);
        final int width = 123;
        final int height = 456;
        EasyMock.expect(numberPickerMocked.getDisplayedValues())
            .andReturn(new String[]{width + "x" + height})
            .anyTimes();
        EasyMock.expect(numberPickerMocked.getValue())
            .andReturn(1)
            .anyTimes();

        EasyMock.replay(numberPickerMocked);
        final Pair<Integer, Integer> resolutionFromPicker = Utils.getResolutionFromPicker(numberPickerMocked);
        Assertions.assertThat(resolutionFromPicker)
            .as("The resolution picker")
            .isEqualTo(new Pair<>(width, height));
    }

    /**
     * Tests that the method {@link Utils#waitExecutorToFinish(ExecutorService)} does not throw any
     * {@link Exception} even if the the {@link ExecutorService#awaitTermination(long, TimeUnit)}
     * method throws an {@link Exception}.
     *
     * @throws InterruptedException If the mock fails.
     */
    @Test
    public void testWaitExecutorToFinishInterruptsThread() throws InterruptedException {
        final ExecutorService executorServiceMocked = EasyMock.mock(ExecutorService.class);

        EasyMock.expect(executorServiceMocked.awaitTermination(1L, TimeUnit.DAYS))
            .andReturn(false)
            .andThrow(new InterruptedException("test"))
            .andReturn(true);

        EasyMock.replay(executorServiceMocked);
        Assertions.assertThatCode(() -> Utils.waitExecutorToFinish(executorServiceMocked))
            .as("The call to Utils#waitExecutorToFinish method")
            .doesNotThrowAnyException();
    }
}
