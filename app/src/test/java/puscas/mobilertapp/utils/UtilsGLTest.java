package puscas.mobilertapp.utils;

import android.opengl.GLES20;
import android.opengl.GLUtils;

import org.assertj.core.api.Assertions;
import org.easymock.EasyMock;
import org.easymock.MockType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import puscas.mobilertapp.exceptions.FailureException;

/**
 * The unit tests for the {@link UtilsGL} util class.
 */
// Annotations necessary for PowerMock to be able to mock final classes, and static and native methods.
@RunWith(PowerMockRunner.class)
@PrepareOnlyThisForTest({GLES20.class, GLUtils.class, EGLContext.class})
public final class UtilsGLTest {

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

    /**
     * Tests the behavior of method {@link UtilsGL#checksGlError()} when OpenGL has some failure.
     */
    @Test
    public void testChecksGlErrorFailure() {
        PowerMock.mockStaticPartialNice(GLES20.class, "glGetError");
        EasyMock.expect(GLES20.glGetError()).andReturn(1).anyTimes();

        PowerMock.mockStaticPartialNice(GLUtils.class, "getEGLErrorString");
        final String expectedErrorMessage = "test GL error message";
        EasyMock.expect(GLUtils.getEGLErrorString(1)).andReturn(expectedErrorMessage).anyTimes();

        PowerMock.replayAll();
        Assertions.assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(UtilsGL.class, "checksGlError"))
            .as("The 'checksGlError' should throw an exception")
            .isInstanceOf(FailureException.class)
            .hasMessage(expectedErrorMessage);

        PowerMock.verifyAll();
    }

    /**
     * Tests the behavior of method {@link UtilsGL#bindTexture()} when OpenGL has some failure.
     */
    @Test
    public void testBindTextureFailure() {
        PowerMock.mockStaticPartialNice(GLES20.class, "glGetError");
        EasyMock.expect(GLES20.glGetError()).andReturn(0).anyTimes();
        final String expectedErrorMessage = "Error loading texture.";

        PowerMock.replayAll();
        Assertions.assertThatThrownBy(UtilsGL::bindTexture)
            .as("The 'bindTexture' should throw an exception")
            .isInstanceOf(FailureException.class)
            .hasMessage(expectedErrorMessage);

        PowerMock.verifyAll();
    }

    /**
     * Tests the behavior of method {@link UtilsGL#checkGL20Support()} when the device doesn't
     * support GLES 2.0.
     */
    @Test
    public void testCheckGL20SupportFailure() {
        PowerMock.mockStaticPartialNice(EGLContext.class, "getEGL");
        final EGL10 mockedEgl10 = EasyMock.mock(MockType.NICE, EGL10.class);
        EasyMock.expect(mockedEgl10.eglInitialize(EasyMock.anyObject(EGLDisplay.class), EasyMock.anyObject(int[].class))).andReturn(false).anyTimes();
        EasyMock.expect(EGLContext.getEGL()).andReturn(mockedEgl10).anyTimes();

        PowerMock.replayAll(mockedEgl10);
        Assertions.assertThat(UtilsGL.checkGL20Support())
            .as("The 'checkGL20Support' should fail")
            .isFalse();
        PowerMock.verifyAll();
    }

}
