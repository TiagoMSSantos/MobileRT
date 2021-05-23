package puscas.mobilertapp.exceptions;

import lombok.extern.java.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The test suite for {@link FailureException} class.
 */
@Log
public final class FailureExceptionTest {

    /**
     * Tests the constructor without arguments.
     */
    @Test
    public void testConstructorWithoutArguments() {
        Assertions.assertThrows(FailureException.class, () -> {
            throw new FailureException();
        }, "Expected an exception.");
    }

    /**
     * Tests the constructor that receives a {@link Throwable}.
     */
    @Test
    public void testConstructorWithThrowableArgument() {
        final IndexOutOfBoundsException indexOutOfBoundsException =
            new IndexOutOfBoundsException("Test");
        Assertions.assertThrows(FailureException.class, () -> {
            throw new FailureException(indexOutOfBoundsException);
        }, "Expected an exception.");
    }

    /**
     * Tests the constructor that receives a {@link String} with the message of the cause.
     */
    @Test
    public void testConstructorWithStringArgument() {
        final String message = "Test";
        Assertions.assertThrows(FailureException.class, () -> {
            throw new FailureException(message);
        }, "Expected an exception.");
    }

}
