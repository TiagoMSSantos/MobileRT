package puscas.mobilertapp.exceptions;

import lombok.extern.java.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The test suite for {@link LowMemoryException} class.
 */
@Log
public final class LowMemoryExceptionTest {

    /**
     * Setup method called before each test.
     */
    @Before
    public void setUp() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.info(methodName);
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.info(methodName);
    }

    /**
     * Tests the constructor without arguments.
     */
    @Test(expected = LowMemoryException.class)
    public void testConstructorWithoutArguments() throws LowMemoryException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.info(methodName);

        throw new LowMemoryException();
    }

    /**
     * Tests the constructor that receives a {@link Throwable}.
     */
    @Test(expected = LowMemoryException.class)
    public void testConstructorWithThrowableArgument() throws LowMemoryException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.info(methodName);

        final IndexOutOfBoundsException indexOutOfBoundsException =
            new IndexOutOfBoundsException("Test");
        throw new LowMemoryException(indexOutOfBoundsException);
    }

    /**
     * Tests the constructor that receives a {@link String} with the message of the cause.
     */
    @Test(expected = LowMemoryException.class)
    public void testConstructorWithStringArgument() throws LowMemoryException {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.info(methodName);

        final String message = "Test";
        throw new LowMemoryException(message);
    }

}
