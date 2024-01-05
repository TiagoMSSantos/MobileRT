package puscas.mobilertapp.utils;

import android.opengl.GLES20;

import org.assertj.core.api.Assertions;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import puscas.mobilertapp.exceptions.FailureException;

/**
 * The unit tests for the {@link UtilsShader} util class.
 */
// Annotations necessary for PowerMock to be able to mock final classes, and static and native methods.
@RunWith(PowerMockRunner.class)
@PrepareOnlyThisForTest({GLES20 .class})
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
        Assertions.assertThatThrownBy(constructor::newInstance)
            .as("The default constructor of UtilsShader")
            .isNotNull()
            .isInstanceOf(InvocationTargetException.class);
    }

    /**
     * Tests that the method {@link UtilsShader#checksShaderLinkStatus(int)} will throw
     * {@link FailureException} if the provided shader program was not proper linked.
     */
    @Test
    public void testChecksShaderLinkStatusFailure() {
        Assertions.assertThatThrownBy(() -> UtilsShader.checksShaderLinkStatus(0))
            .as("The 'checksShaderLinkStatus' should fail.")
            .isInstanceOf(FailureException.class);
    }

    /**
     * Tests that the method {@link UtilsShader#loadShader(int, String)} will throw
     * {@link FailureException} if the provided shader type is not valid.
     */
    @Test
    public void testLoadShaderNotValidShaderType() {
        PowerMock.mockStaticPartialNice(GLES20.class, "glCreateShader");
        EasyMock.expect(GLES20.glCreateShader(EasyMock.anyInt()))
            .andReturn(0)
            .anyTimes();

        PowerMock.replayAll();
        Assertions.assertThatThrownBy(() -> UtilsShader.loadShader(0, ""))
            .as("The 'loadShader' should fail.")
            .isInstanceOf(FailureException.class)
            .hasMessage("There was an error while creating the shader object: (0) null");
    }

    /**
     * Tests that the method {@link UtilsShader#loadShader(int, String)} will throw
     * {@link FailureException} if the provided shader was not properly compiled.
     */
    @Test
    public void testLoadShaderNotCompiledShader() {
        PowerMock.mockStaticPartialNice(GLES20.class, "glCreateShader", "glGetShaderInfoLog");
        EasyMock.expect(GLES20.glCreateShader(EasyMock.anyInt()))
            .andReturn(1)
            .anyTimes();
        final String expectedErrorMessage = "Expected error stated by GLES20.";
        EasyMock.expect(GLES20.glGetShaderInfoLog(EasyMock.anyInt()))
            .andReturn(expectedErrorMessage)
            .anyTimes();

        PowerMock.replayAll();
        Assertions.assertThatThrownBy(() -> UtilsShader.loadShader(GLES20.GL_VERTEX_SHADER, ""))
            .as("The 'loadShader' should fail.")
            .isInstanceOf(FailureException.class)
            .hasMessage(expectedErrorMessage);
    }

    /**
     * Tests that the method {@link UtilsShader#reCreateProgram(int)} will throw
     * {@link FailureException} if it was not possible to create a new shader program.
     */
    @Test
    public void testReCreateProgramFailure() {
        PowerMock.mockStaticPartialNice(GLES20.class, "glCreateProgram", "glGetProgramInfoLog");
        EasyMock.expect(GLES20.glCreateProgram())
            .andReturn(0)
            .anyTimes();
        final String expectedErrorMessage = "Expected error stated by GLES20.";
        EasyMock.expect(GLES20.glGetProgramInfoLog(EasyMock.anyInt()))
            .andReturn(expectedErrorMessage)
            .anyTimes();

        PowerMock.replayAll();
        Assertions.assertThatThrownBy(() -> UtilsShader.reCreateProgram(0))
            .as("The 'loadShader' should fail.")
            .isInstanceOf(FailureException.class)
            .hasMessage(expectedErrorMessage);
    }

}
