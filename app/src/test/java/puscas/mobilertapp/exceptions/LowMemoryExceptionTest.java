package puscas.mobilertapp.exceptions;

import lombok.extern.java.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The test suite for {@link LowMemoryException} class.
 */
@Log
public final class LowMemoryExceptionTest {

    /**
     * Tests the constructor without arguments.
     */
    @Test
    public void testConstructorWithoutArguments() {
        Assertions.assertThrows(LowMemoryException.class, () -> {
            throw new LowMemoryException();
        }, "Expected an exception.");
    }

    /**
     * Tests the constructor that receives a {@link Throwable}.
     */
    @Test
    public void testConstructorWithThrowableArgument() {
        final IndexOutOfBoundsException indexOutOfBoundsException =
            new IndexOutOfBoundsException("Test");
        Assertions.assertThrows(LowMemoryException.class, () -> {
            throw new LowMemoryException(indexOutOfBoundsException);
        }, "Expected an exception.");
    }

    /**
     * Tests the constructor that receives a {@link String} with the message of the cause.
     */
    @Test
    public void testConstructorWithStringArgument() {
        final String message = "Test";
        Assertions.assertThrows(LowMemoryException.class, () -> {
            throw new LowMemoryException(message);
        }, "Expected an exception.");
    }

}
