package puscas.mobilertapp.exceptions;

import javax.annotation.Nonnull;

import puscas.mobilertapp.utils.Utils;

/**
 * An {@link Exception} which represents the system with low memory.
 */
public class LowMemoryException extends Exception {

    /**
     * The Serial UUID.
     */
    private static final long serialVersionUID = -7934346360661057805L;

    /**
     * The default constructor.
     */
    public LowMemoryException() {
        super();
        Utils.printStackTrace();
    }

    /**
     * The constructor for rethrows.
     *
     * @param cause The {@link Throwable} to wrap with this exception.
     */
    public LowMemoryException(@Nonnull final Throwable cause) {
        super(cause);
        Utils.printStackTrace();
    }

    /**
     * The constructor with message.
     *
     * @param message The cause of the exception.
     */
    public LowMemoryException(@Nonnull final String message) {
        super(message);
        Utils.printStackTrace();
    }
}
