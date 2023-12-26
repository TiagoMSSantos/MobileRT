package puscas.mobilertapp.exceptions;

import androidx.annotation.NonNull;

import java.io.Serial;

/**
 * An {@link Exception} which represents the system with a failure.
 */
public final class FailureException extends RuntimeException {

    /**
     * The Serial UUID.
     */
    @Serial
    private static final long serialVersionUID = -7199144687688639370L;

    /**
     * The constructor for rethrows.
     *
     * @param cause The {@link Throwable} to wrap with this exception.
     */
    public FailureException(@NonNull final Throwable cause) {
        super(cause);
    }

    /**
     * The constructor with message.
     *
     * @param message The cause of the exception.
     */
    public FailureException(@NonNull final String message) {
        super(message);
    }

}
