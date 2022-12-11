package puscas.mobilertapp.exceptions;

import org.junit.Test;

import lombok.extern.java.Log;

/**
 * The test suite for {@link FailureException} class.
 */
@Log
public final class FailureExceptionTest {

    /**
     * Tests the constructor without arguments.
     */
    @Test(expected = RuntimeException.class)
    public void testConstructorWithoutArguments() {
        throw new FailureException();
    }

    /**
     * Tests the constructor that receives a {@link Throwable}.
     */
    @Test(expected = RuntimeException.class)
    public void testConstructorWithThrowableArgument() {
        final IndexOutOfBoundsException indexOutOfBoundsException =
            new IndexOutOfBoundsException("Test");
        throw new FailureException(indexOutOfBoundsException);
    }

    /**
     * Tests the constructor that receives a {@link String} with the message of the cause.
     */
    @Test(expected = RuntimeException.class)
    public void testConstructorWithStringArgument() {
        final String message = "Test";
        throw new FailureException(message);
    }

}
