package puscas.mobilertapp;

/**
 * An {@link Exception} which represents the system with low memory.
 */
class LowMemoryException extends Exception {
    private static final long serialVersionUID = 7599504698852771799L;

    LowMemoryException() {
        super();
    }

    LowMemoryException(final String errorMessage) {
        super(errorMessage);
    }

    LowMemoryException (final Throwable throwable) {
        super (throwable);

    }

    LowMemoryException (final String errorMessage, final Throwable throwable) {
        super (errorMessage, throwable);
    }
}
