package puscas.mobilertapp.utils;

import android.content.Context;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import puscas.mobilertapp.DrawView;
import puscas.mobilertapp.MyEglContextFactory;

/**
 * Utility class with the text constants for the errors.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConstantsError {

    /**
     * A message for when the
     * {@link MyEglContextFactory#destroyContext(EGL10, EGLDisplay, EGLContext)}
     * method throws an exception.
     */
    public static final String EGL_DESTROY_CONTEXT_FAILED = "eglDestroyContext failed: ";

    /**
     * A message for when the {@link DrawView#onPause()} method couldn't find a {@link Context}.
     */
    public static final String UNABLE_TO_FIND_AN_ACTIVITY = "Unable to find an activity: ";

}
