package puscas.mobilertapp.exceptions;

import lombok.extern.java.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The test suite for {@link FailureException} class.
 */
@Log
public final class FailureExceptionTest {

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
    @Test(expected = RuntimeException.class)
    public void testConstructorWithoutArguments() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.info(methodName);

        throw new FailureException();
    }

    /**
     * Tests the constructor that receives a {@link Throwable}.
     */
    @Test(expected = RuntimeException.class)
    public void testConstructorWithThrowableArgument() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.info(methodName);

        final IndexOutOfBoundsException indexOutOfBoundsException =
            new IndexOutOfBoundsException("Test");
        throw new FailureException(indexOutOfBoundsException);
    }

    /**
     * Tests the constructor that receives a {@link String} with the message of the cause.
     */
    @Test(expected = RuntimeException.class)
    public void testConstructorWithStringArgument() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        log.info(methodName);

        final String message = "Test";
        throw new FailureException(message);
    }

}
