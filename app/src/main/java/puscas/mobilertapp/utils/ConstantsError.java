package puscas.mobilertapp.utils;

import android.content.Context;

import java.util.logging.Logger;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import puscas.mobilertapp.DrawView;
import puscas.mobilertapp.MyEGLContextFactory;

/**
 * Utility class with the text constants for the errors.
 */
public final class ConstantsError {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(ConstantsError.class.getName());

    /**
     * A message for when the {@link MyEGLContextFactory#destroyContext(EGL10, EGLDisplay, EGLContext)} method throws an
     * exception.
     */
    public static final String EGL_DESTROY_CONTEXT_FAILED = "eglDestroyContext failed: ";

    /**
     * A message for when the {@link DrawView#onPause()} method couldn't find a {@link Context}.
     */
    public static final String UNABLE_TO_FIND_AN_ACTIVITY = "Unable to find an activity: ";

    /**
     * A private constructor in order to prevent instantiating this helper class.
     */
    private ConstantsError() {
        LOGGER.info("ConstantsError");
    }
}
