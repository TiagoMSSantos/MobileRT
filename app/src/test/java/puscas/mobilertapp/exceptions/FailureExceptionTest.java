package puscas.mobilertapp.exceptions;

import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The test suite for {@link FailureException} class.
 */
public final class FailureExceptionTest {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(FailureExceptionTest.class.getName());

    /**
     * Setup method called before each test.
     */
    @Before
    public void setUp() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Tests the constructor.
     */
    @Test(expected = RuntimeException.class)
    public void testConstructorWithoutArguments() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        throw new FailureException();
    }

    /**
     * Tests the constructor.
     */
    @Test(expected = RuntimeException.class)
    public void testConstructorWithThrowableArgument() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final IndexOutOfBoundsException indexOutOfBoundsException =
            new IndexOutOfBoundsException("Test");
        throw new FailureException(indexOutOfBoundsException);
    }

    /**
     * Tests the constructor.
     */
    @Test(expected = RuntimeException.class)
    public void testConstructorWithStringArgument() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);

        final String message = "Test";
        throw new FailureException(message);
    }
}
