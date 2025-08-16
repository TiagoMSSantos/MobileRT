package puscas.mobilertapp.constants;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import puscas.mobilertapp.MyEglContextFactory;

/**
 * Utility class with the text constants for the errors.
 */
public final class ConstantsError {

    /**
     * Private constructor to avoid creating instances.
     */
    private ConstantsError() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * A message for when the
     * {@link MyEglContextFactory#destroyContext(EGL10, EGLDisplay, EGLContext)}
     * method throws an exception.
     */
    public static final String EGL_DESTROY_CONTEXT_FAILED = "eglDestroyContext failed: ";

}
