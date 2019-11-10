package puscas.mobilertapp;

import android.content.Context;
import android.os.Bundle;

import java.util.logging.Logger;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * Utility class with the text constants for the errors.
 */
final class ConstantsError {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(ConstantsError.class.getName());

    /**
     * A message for when the {@link MyEGLContextFactory#destroyContext(EGL10, EGLDisplay, EGLContext)} method throws an
     * exception.
     */
    static final String EGL_DESTROY_CONTEXT_FAILED = "eglDestroyContext failed: ";

    /**
     * A message for when the {@link DrawView#onPause()} method couldn't find a {@link Context}.
     */
    static final String UNABLE_TO_FIND_AN_ACTIVITY = "Unable to find an activity: ";

    /**
     * A message for when the {@link MainActivity#onCreate(Bundle)} method couldn't find the number of CPU cores.
     */
    static final String CANT_GET_NUMBER_OF_CORES_AVAILABLE = "Can't get number of cores available!!!";

    /**
     * A private constructor in order to prevent instantiating this helper class.
     */
    private ConstantsError() {
        LOGGER.info("ConstantsError");
    }
}
