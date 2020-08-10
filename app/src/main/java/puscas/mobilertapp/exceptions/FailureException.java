package puscas.mobilertapp.exceptions;

import javax.annotation.Nonnull;
import puscas.mobilertapp.utils.UtilsLogging;

/**
 * An {@link Exception} which represents the system with a failure.
 */
public class FailureException extends RuntimeException {

    /**
     * The Serial UUID.
     */
    private static final long serialVersionUID = -7934346360661057805L;

    /**
     * The default constructor.
     */
    FailureException() {
        super();
        UtilsLogging.printStackTrace();
    }

    /**
     * The constructor for rethrows.
     *
     * @param cause The {@link Throwable} to wrap with this exception.
     */
    public FailureException(@Nonnull final Throwable cause) {
        super(cause);
        UtilsLogging.printStackTrace();
    }

    /**
     * The constructor with message.
     *
     * @param message The cause of the exception.
     */
    public FailureException(@Nonnull final String message) {
        super(message);
        UtilsLogging.printStackTrace();
    }
}
