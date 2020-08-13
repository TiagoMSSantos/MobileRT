package puscas.mobilertapp.exceptions;

import javax.annotation.Nonnull;

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
    }

    /**
     * The constructor for rethrows.
     *
     * @param cause The {@link Throwable} to wrap with this exception.
     */
    public FailureException(@Nonnull final Throwable cause) {
        super(cause);
    }

    /**
     * The constructor with message.
     *
     * @param message The cause of the exception.
     */
    public FailureException(@Nonnull final String message) {
        super(message);
    }

}
