package puscas.mobilertapp.exceptions;

import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.NoArgsConstructor;

/**
 * An {@link Exception} which represents the system with low memory.
 */
@NoArgsConstructor
public class LowMemoryException extends Exception {

    /**
     * The Serial UUID.
     */
    private static final long serialVersionUID = -7934346360661057805L;

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

}
