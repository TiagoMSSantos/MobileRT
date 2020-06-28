package puscas.mobilertapp;

import android.content.Context;
import android.opengl.GLSurfaceView;

import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.Contract;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * The test suite for the {@link MyEGLContextFactory} class.
 */
public final class MyEGLContextFactoryTest {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(MyEGLContextFactoryTest.class.getName());

    /**
     * Setup method called before each test.
     */
    @Before
    public void setUp() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Helper method that creates a {@link EGL10}.
     *
     * @return A new {@link EGL10}.
     */
    @Contract(pure = true)
    @Nonnull
    private static EGL10 createEGL() {
        return new EGL10() {
            @Contract(pure = true)
            @Override
            public boolean eglChooseConfig(final EGLDisplay display, final int[] attrib_list, final EGLConfig[] configs, final int config_size, final int[] num_config) {
                return false;
            }

            @Contract(pure = true)
            @Override
            public boolean eglCopyBuffers(final EGLDisplay display, final EGLSurface surface, final Object native_pixmap) {
                return false;
            }

            @Contract(pure = true)
            @Override
            @Nullable
            public EGLContext eglCreateContext(final EGLDisplay display, final EGLConfig config, final EGLContext share_context, final int[] attrib_list) {
                return null;
            }

            @Contract(pure = true)
            @Override
            @Nullable
            public EGLSurface eglCreatePbufferSurface(final EGLDisplay display, final EGLConfig config, final int[] attrib_list) {
                return null;
            }

            @Contract(pure = true)
            @Override
            @Nullable
            public EGLSurface eglCreatePixmapSurface(final EGLDisplay display, final EGLConfig config, final Object native_pixmap, final int[] attrib_list) {
                return null;
            }

            @Contract(pure = true)
            @Override
            @Nullable
            public EGLSurface eglCreateWindowSurface(final EGLDisplay display, final EGLConfig config, final Object native_window, final int[] attrib_list) {
                return null;
            }

            @Contract(pure = true)
            @Override
            public boolean eglDestroyContext(final EGLDisplay display, final EGLContext context) {
                return false;
            }

            @Contract(pure = true)
            @Override
            public boolean eglDestroySurface(final EGLDisplay display, final EGLSurface surface) {
                return false;
            }

            @Contract(pure = true)
            @Override
            public boolean eglGetConfigAttrib(final EGLDisplay display, final EGLConfig config, final int attribute, final int[] value) {
                return false;
            }

            @Contract(pure = true)
            @Override
            public boolean eglGetConfigs(final EGLDisplay display, final EGLConfig[] configs, final int config_size, final int[] num_config) {
                return false;
            }

            @Contract(pure = true)
            @Override
            @Nullable
            public EGLContext eglGetCurrentContext() {
                return null;
            }

            @Contract(pure = true)
            @Override
            @Nullable
            public EGLDisplay eglGetCurrentDisplay() {
                return null;
            }

            @Contract(pure = true)
            @Override
            @Nullable
            public EGLSurface eglGetCurrentSurface(final int readdraw) {
                return null;
            }

            @Contract(pure = true)
            @Override
            @Nullable
            public EGLDisplay eglGetDisplay(final Object native_display) {
                return null;
            }

            @Contract(pure = true)
            @Override
            public int eglGetError() {
                return 0;
            }

            @Contract(pure = true)
            @Override
            public boolean eglInitialize(final EGLDisplay display, final int[] major_minor) {
                return false;
            }

            @Contract(pure = true)
            @Override
            public boolean eglMakeCurrent(final EGLDisplay display, final EGLSurface draw, final EGLSurface read, EGLContext context) {
                return false;
            }

            @Contract(pure = true)
            @Override public boolean eglQueryContext(final EGLDisplay display, final EGLContext context, final int attribute, final int[] value) {
                return false;
            }

            @Contract(pure = true)
            @Override
            @Nullable
            public String eglQueryString(final EGLDisplay display, final int name) {
                return null;
            }

            @Contract(pure = true)
            @Override
            public boolean eglQuerySurface(final EGLDisplay display, final EGLSurface surface, int attribute, int[] value) {
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
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final Context context = new MainActivity();
        final DrawView drawView = new DrawView(context);

        final GLSurfaceView.EGLContextFactory myEGLContextFactory = new MyEGLContextFactory(drawView);
        final EGL10 egl = createEGL();

        final EGLContext eglContext = myEGLContextFactory.createContext(egl, null, null);
        Assertions.assertThat(eglContext).isNull();
    }
}
