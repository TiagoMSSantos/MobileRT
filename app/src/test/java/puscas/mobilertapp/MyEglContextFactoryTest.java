package puscas.mobilertapp;

import android.content.Context;
import android.opengl.GLSurfaceView;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import lombok.extern.java.Log;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.Contract;
import org.junit.Ignore;
import org.junit.Test;

/**
 * The test suite for the {@link MyEglContextFactory} class.
 */
@Log
public final class MyEglContextFactoryTest {

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
            public boolean eglChooseConfig(final EGLDisplay display, final int[] attrib_list,
                                           final EGLConfig[] configs, final int config_size,
                                           final int[] num_config) {
                return false;
            }

            @Contract(pure = true)
            @Override
            public boolean eglCopyBuffers(final EGLDisplay display, final EGLSurface surface,
                                          final Object native_pixmap) {
                return false;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public EGLContext eglCreateContext(final EGLDisplay display, final EGLConfig config,
                                               final EGLContext share_context,
                                               final int[] attrib_list) {
                return null;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public EGLSurface eglCreatePbufferSurface(final EGLDisplay display,
                                                      final EGLConfig config,
                                                      final int[] attrib_list) {
                return null;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public EGLSurface eglCreatePixmapSurface(final EGLDisplay display,
                                                     final EGLConfig config,
                                                     final Object native_pixmap,
                                                     final int[] attrib_list) {
                return null;
            }

            @Contract(pure = true)
            @Nullable
            @Override
            public EGLSurface eglCreateWindowSurface(final EGLDisplay display,
                                                     final EGLConfig config,
                                                     final Object native_window,
                                                     final int[] attrib_list) {
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
            public boolean eglGetConfigAttrib(final EGLDisplay display, final EGLConfig config,
                                              final int attribute, final int[] value) {
                return false;
            }

            @Contract(pure = true)
            @Override
            public boolean eglGetConfigs(final EGLDisplay display, final EGLConfig[] configs,
                                         final int config_size, final int[] num_config) {
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
    @Ignore("Needs MobileRT library for some reason ...")
    @Test
    public void testInvalidCreateContext() {
        final Context context = new MainActivity();
        final DrawView drawView = new DrawView(context);

        final GLSurfaceView.EGLContextFactory myEGLContextFactory = new MyEglContextFactory(drawView);
        final EGL10 egl = createEGL();

        final EGLContext eglContext = myEGLContextFactory.createContext(egl, null, null);
        Assertions.assertThat(eglContext).isNull();
    }

}
