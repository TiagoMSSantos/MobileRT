package puscas.mobilertapp;

import android.content.Context;
import android.opengl.GLSurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.assertj.core.api.Assertions;
import org.easymock.EasyMock;
import org.jetbrains.annotations.Contract;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import puscas.mobilertapp.exceptions.FailureException;

/**
 * The test suite for the {@link MyEglContextFactory} class.
 */
// Annotations necessary for PowerMock to be able to mock final classes, and static and native methods.
@RunWith(PowerMockRunner.class)
@PrepareOnlyThisForTest({MainActivity.class, MainRenderer.class, DrawView.class})
public final class MyEglContextFactoryTest {

    /**
     * The {@link EGL10} value to return by the mocked {@link EGL10} when the {@link EGL10#eglGetError()}
     * is called.
     */
    static int eglErrorReturnedByMock = EGL10.EGL_SUCCESS;

    /**
     * The {@link EGLContext} value to return by the mocked {@link EGL10} when the
     * {@link EGL10#eglCreateContext(EGLDisplay, EGLConfig, EGLContext, int[])} is called.
     */
    static EGLContext eglContextReturnedByMock = null;

    /**
     * The {@link Boolean} value to return by the mocked {@link EGL10} when the
     * {@link EGL10#eglDestroyContext(EGLDisplay, EGLContext)} is called.
     */
    static boolean eglContextDestroy = false;

    /**
     * Helper method that creates a {@link EGL10}.
     *
     * @return A new {@link EGL10}.
     */
    @Contract(pure = true)
    @NonNull
    private static EGL10 createEGL() {
        return new EGL10() {
            @Contract(pure = true)
            @Override
            public boolean eglChooseConfig(final EGLDisplay display, final int[] attribList,
                                           final EGLConfig[] configs, final int configSize,
                                           final int[] numConfig) {
                return false;
            }

            @Contract(pure = true)
            @Override
            public boolean eglCopyBuffers(final EGLDisplay display, final EGLSurface surface,
                                          final Object nativePixmap) {
                return false;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public EGLContext eglCreateContext(final EGLDisplay display, final EGLConfig config,
                                               final EGLContext shareContext,
                                               final int[] attribList) {
                return eglContextReturnedByMock;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public EGLSurface eglCreatePbufferSurface(final EGLDisplay display,
                                                      final EGLConfig config,
                                                      final int[] attribList) {
                return null;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            @SuppressWarnings({"deprecation"})
            public EGLSurface eglCreatePixmapSurface(final EGLDisplay display,
                                                     final EGLConfig config,
                                                     final Object nativePixmap,
                                                     final int[] attribList) {
                return null;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public EGLSurface eglCreateWindowSurface(final EGLDisplay display,
                                                     final EGLConfig config,
                                                     final Object nativeWindow,
                                                     final int[] attribList) {
                return null;
            }

            @Contract(pure = true)
            @Override
            public boolean eglDestroyContext(final EGLDisplay display, final EGLContext context) {
                return eglContextDestroy;
            }

            @Contract(pure = true)
            @Override
            public boolean eglDestroySurface(final EGLDisplay display, final EGLSurface surface) {
                return false;
            }

            @Contract(pure = true)
            @Override
            public boolean eglGetConfigAttrib(final EGLDisplay display, final EGLConfig config,
                                              final int attribute, final int[] value) {
                return false;
            }

            @Contract(pure = true)
            @Override
            public boolean eglGetConfigs(final EGLDisplay display, final EGLConfig[] configs,
                                         final int configSize, final int[] numConfig) {
                return false;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public EGLContext eglGetCurrentContext() {
                return null;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public EGLDisplay eglGetCurrentDisplay() {
                return null;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public EGLSurface eglGetCurrentSurface(final int readdraw) {
                return null;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public EGLDisplay eglGetDisplay(final Object nativeDisplay) {
                return null;
            }

            @Contract(pure = true)
            @Override
            public int eglGetError() {
                return eglErrorReturnedByMock;
            }

            @Contract(pure = true)
            @Override
            public boolean eglInitialize(final EGLDisplay display, final int[] majorMinor) {
                return false;
            }

            @Contract(pure = true)
            @Override
            public boolean eglMakeCurrent(final EGLDisplay display, final EGLSurface draw,
                                          final EGLSurface read, final EGLContext context) {
                return false;
            }

            @Contract(pure = true)
            @Override
            public boolean eglQueryContext(final EGLDisplay display, final EGLContext context,
                                           final int attribute, final int[] value) {
                return false;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public String eglQueryString(final EGLDisplay display, final int name) {
                return null;
            }

            @Contract(pure = true)
            @Override
            public boolean eglQuerySurface(final EGLDisplay display, final EGLSurface surface,
                                           final int attribute, final int[] value) {
                return false;
            }

            @Contract(pure = true)
            @Override
            public boolean eglSwapBuffers(final EGLDisplay display, final EGLSurface surface) {
                return false;
            }

            @Contract(pure = true)
            @Override
            public boolean eglTerminate(final EGLDisplay display) {
                return false;
            }

            @Contract(pure = true)
            @Override
            public boolean eglWaitGL() {
                return false;
            }

            @Contract(pure = true)
            @Override
            public boolean eglWaitNative(final int engine, final Object bindTarget) {
                return false;
            }
        };
    }

    /**
     * Tests that without providing {@link EGLDisplay} and {@link EGLConfig},
     * the {@link GLSurfaceView.EGLContextFactory} doesn't create the
     * {@link EGLContext}.
     */
    @Test
    public void testInvalidCreateContext() {
        MemberModifier.suppress(MemberModifier.method(MainRenderer.class, "setBitmap"));

        final Context context = new MainActivity();
        final DrawView drawView = new DrawView(context);

        final GLSurfaceView.EGLContextFactory myEGLContextFactory = new MyEglContextFactory(drawView::isChangingConfigs);
        final EGL10 egl = createEGL();

        // Values to be returned by the EGL10 mocked.
        eglContextDestroy = false;
        eglErrorReturnedByMock = EGL10.EGL_SUCCESS;
        eglContextReturnedByMock = null;

        final EGLContext eglContext = myEGLContextFactory.createContext(egl, null, null);
        Assertions.assertThat(eglContext)
            .as("The EGL context created")
            .isNull();
    }

    /**
     * Tests that if {@link EGL10#eglGetError()} does not provide {@link EGL10#EGL_SUCCESS}, then
     * the {@link MyEglContextFactory#createContext(EGL10, EGLDisplay, EGLConfig)} method does not
     * allow to create an {@link EGLContext}.
     */
    @Test
    public void testExceptionWhenCreatingContext() {
        MemberModifier.suppress(MemberModifier.method(MainRenderer.class, "setBitmap"));

        final Context context = new MainActivity();
        final DrawView drawView = new DrawView(context);

        final GLSurfaceView.EGLContextFactory myEGLContextFactory = new MyEglContextFactory(drawView::isChangingConfigs);
        final EGL10 egl = createEGL();

        // Values to be returned by the EGL10 mocked.
        eglContextDestroy = false;
        eglErrorReturnedByMock = EGL10.EGL_NONE;
        eglContextReturnedByMock = null;

        Assertions.assertThatThrownBy(() -> myEGLContextFactory.createContext(egl, null, null))
            .as("The EGL context created")
            .isInstanceOf(FailureException.class);
    }

    /**
     * Tests that the method {@link MyEglContextFactory#createContext(EGL10, EGLDisplay, EGLConfig)}
     * creates an {@link EGLContext}.
     */
    @Test
    public void testCreateContext() {
        MemberModifier.suppress(MemberModifier.method(MainRenderer.class, "setBitmap"));

        final Context context = new MainActivity();
        final DrawView drawView = new DrawView(context);

        final GLSurfaceView.EGLContextFactory myEGLContextFactory = new MyEglContextFactory(drawView::isChangingConfigs);
        final EGL10 egl = createEGL();

        // Values to be returned by the EGL10 mocked.
        eglContextDestroy = false;
        eglErrorReturnedByMock = EGL10.EGL_SUCCESS;
        eglContextReturnedByMock = EasyMock.mock(EGLContext.class);

        final EGLContext eglContext = myEGLContextFactory.createContext(egl, null, null);
        Assertions.assertThat(eglContext)
            .as("The EGL context created")
            .isNotNull();
    }

    /**
     * Tests that the method {@link MyEglContextFactory#createContext(EGL10, EGLDisplay, EGLConfig)}
     * destroys the previously created {@link EGLContext}.
     */
    @Test
    public void testCreateNewContext() {
        MemberModifier.suppress(MemberModifier.method(MainRenderer.class, "setBitmap"));

        final Context context = new MainActivity();
        final DrawView drawView = new DrawView(context);

        final GLSurfaceView.EGLContextFactory myEGLContextFactory = new MyEglContextFactory(drawView::isChangingConfigs);
        final EGL10 egl = createEGL();

        // Values to be returned by the EGL10 mocked.
        // Mock the 1st EGLContext.
        eglContextDestroy = false;
        eglErrorReturnedByMock = EGL10.EGL_SUCCESS;
        eglContextReturnedByMock = EasyMock.mock(EGLContext.class);

        final EGLContext eglContext = myEGLContextFactory.createContext(egl, null, null);
        Assertions.assertThat(eglContext)
            .as("The EGL context created")
            .isNotNull();

        // Mock the 2nd EGLContext.
        eglContextDestroy = true;
        eglErrorReturnedByMock = EGL10.EGL_SUCCESS;
        eglContextReturnedByMock = EasyMock.mock(EGLContext.class);

        final EGLContext newEglContext = myEGLContextFactory.createContext(egl, null, null);
        Assertions.assertThat(newEglContext)
            .as("The new EGL context created")
            .isNotSameAs(eglContext)
            .isNull();
    }

    /**
     * Tests that the method {@link MyEglContextFactory#destroyContext(EGL10, EGLDisplay, EGLContext)}
     * throws an {@link Exception} when {@link EGL10#eglDestroyContext(EGLDisplay, EGLContext)}
     * returns {@code false}.
     */
    @Test
    public void testInvalidDestroyContext() {
        MemberModifier.suppress(MemberModifier.method(MainRenderer.class, "setBitmap"));

        final Context context = new MainActivity();
        final DrawView drawView = new DrawView(context);

        final GLSurfaceView.EGLContextFactory myEGLContextFactory = new MyEglContextFactory(drawView::isChangingConfigs);
        final EGL10 egl = createEGL();

        // Values to be returned by the EGL10 mocked.
        eglContextDestroy = false;
        eglErrorReturnedByMock = EGL10.EGL_SUCCESS;
        eglContextReturnedByMock = EasyMock.mock(EGLContext.class);

        final EGLContext eglContext = myEGLContextFactory.createContext(egl, null, null);
        Assertions.assertThat(eglContext)
            .as("The EGL context created")
            .isNotNull();
        Assertions.assertThatThrownBy(() -> myEGLContextFactory.destroyContext(egl, null, null))
            .as("The EGL context destroyed")
            .isInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Tests that the method {@link MyEglContextFactory#destroyContext(EGL10, EGLDisplay, EGLContext)}
     * replaces the {@link MyEglContextFactory#eglContext} by the new one received via parameter.
     */
    @Test
    public void testDestroyContext() {
        final DrawView drawViewMocked = EasyMock.mock(DrawView.class);
        EasyMock.expect(drawViewMocked.isChangingConfigs())
            .andReturn(true)
            .anyTimes();

        final GLSurfaceView.EGLContextFactory myEGLContextFactory = new MyEglContextFactory(drawViewMocked::isChangingConfigs);
        final EGL10 egl = createEGL();

        // Values to be returned by the EGL10 mocked.
        eglContextDestroy = false;
        eglErrorReturnedByMock = EGL10.EGL_SUCCESS;
        eglContextReturnedByMock = EasyMock.mock(EGLContext.class);

        final EGLContext eglContext = myEGLContextFactory.createContext(egl, null, null);
        Assertions.assertThat(eglContext)
            .as("The EGL context created")
            .isNotNull();

        final EGLContext eglContextMocked = EasyMock.mock(EGLContext.class);

        EasyMock.replay(drawViewMocked, eglContextReturnedByMock, eglContextMocked);
        myEGLContextFactory.destroyContext(egl, null, eglContextMocked);

        final EGLContext newEglContext = (EGLContext) ReflectionTestUtils.getField(myEGLContextFactory, "eglContext");
        Assertions.assertThat(newEglContext)
            .as("The new EGL context created")
            .isNotSameAs(eglContext)
            .isSameAs(eglContextMocked)
            .isNotNull();
    }

}
