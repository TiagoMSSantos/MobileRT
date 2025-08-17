package puscas.mobilertapp.exceptions;

import org.junit.Test;

/**
 * The test suite for {@link LowMemoryException} class.
 */
public final class LowMemoryExceptionTest {

    /**
     * Tests the constructor that receives a {@link Throwable}.
     */
    @Test(expected = LowMemoryException.class)
    public void testConstructorWithThrowableArgument() throws LowMemoryException {
        final IndexOutOfBoundsException indexOutOfBoundsException =
            new IndexOutOfBoundsException("Test");
        throw new LowMemoryException(indexOutOfBoundsException);
    }

    /**
     * Tests the constructor that receives a {@link String} with the message of the cause.
     */
    @Test(expected = LowMemoryException.class)
    public void testConstructorWithStringArgument() throws LowMemoryException {
        final String message = "Test";
        throw new LowMemoryException(message);
    }

    /**
     * Tests the constructor that receives both parameters.
     */
    @Test(expected = LowMemoryException.class)
    public void testConstructorWithBothParameters() throws LowMemoryException {
        final String message = "Test";
        final IndexOutOfBoundsException indexOutOfBoundsException = new IndexOutOfBoundsException("Test");
        throw new LowMemoryException(message, indexOutOfBoundsException);
    }

}
