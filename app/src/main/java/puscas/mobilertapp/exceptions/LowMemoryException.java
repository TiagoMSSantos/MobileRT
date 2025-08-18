package puscas.mobilertapp.exceptions;

import androidx.annotation.NonNull;

import java.io.Serial;

/**
 * An {@link Exception} which represents the system with low memory.
 */
public final class LowMemoryException extends RuntimeException {

    /**
     * The Serial UUID.
     */
    @Serial
    private static final long serialVersionUID = -6179575914997567912L;

    /**
     * The constructor for rethrows.
     *
     * @param cause The {@link Throwable} to wrap with this exception.
     */
    public LowMemoryException(@NonNull final Throwable cause) {
        super(cause);
    }

    /**
     * The constructor with message.
     *
     * @param message The cause of the exception.
     */
    public LowMemoryException(@NonNull final String message) {
        super(message);
    }

    /**
     * The constructor for rethrows with message.
     *
     * @param message The cause of the exception.
     * @param cause   The {@link Throwable} to wrap with this exception.
     */
    public LowMemoryException(@NonNull final String message, @NonNull final Throwable cause) {
        super(message, cause);
    }

}
