package puscas.mobilertapp.exceptions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import puscas.mobilertapp.utils.Constants;

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
        LOGGER.info(Constants.SET_UP);
    }

    /**
     * Tear down method called after each test.
     */
    @After
    public void tearDown() {
        LOGGER.info(Constants.TEAR_DOWN);
    }

    /**
     * Tests the constructor.
     */
    @Test(expected = RuntimeException.class)
    public void testConstructorWithoutArguments() {
        LOGGER.info("testConstructorWithoutArguments");
        throw new FailureException();
    }

    /**
     * Tests the constructor.
     */
    @Test(expected = RuntimeException.class)
    public void testConstructorWithThrowableArgument() {
        LOGGER.info("testConstructorWithThrowableArgument");
        final IndexOutOfBoundsException indexOutOfBoundsException = new IndexOutOfBoundsException("Test");
        throw new FailureException(indexOutOfBoundsException);
    }

    /**
     * Tests the constructor.
     */
    @Test(expected = RuntimeException.class)
    public void testConstructorWithStringArgument() {
        LOGGER.info("testConstructorWithStringArgument");
        final String message = "Test";
        throw new FailureException(message);
    }
}
