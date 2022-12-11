package puscas.mobilertapp.exceptions;

import org.junit.Test;

import lombok.extern.java.Log;

/**
 * The test suite for {@link LowMemoryException} class.
 */
@Log
public final class LowMemoryExceptionTest {

    /**
     * Tests the constructor without arguments.
     */
    @Test(expected = LowMemoryException.class)
    public void testConstructorWithoutArguments() throws LowMemoryException {
        throw new LowMemoryException();
    }

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

}
