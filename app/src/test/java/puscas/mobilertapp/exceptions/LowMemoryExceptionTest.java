package puscas.mobilertapp.exceptions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import puscas.mobilertapp.utils.Constants;

/**
 * The test suite for {@link LowMemoryException} class.
 */
public final class LowMemoryExceptionTest {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(LowMemoryExceptionTest.class.getName());

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
    @Test(expected = LowMemoryException.class)
    public void testConstructorWithoutArguments() throws LowMemoryException {
        LOGGER.info("testConstructorWithoutArguments");
        throw new LowMemoryException();
    }

    /**
     * Tests the constructor.
     */
    @Test(expected = LowMemoryException.class)
    public void testConstructorWithThrowableArgument() throws LowMemoryException {
        LOGGER.info("testConstructorWithThrowableArgument");
        final IndexOutOfBoundsException indexOutOfBoundsException = new IndexOutOfBoundsException("Test");
        throw new LowMemoryException(indexOutOfBoundsException);
    }

    /**
     * Tests the constructor.
     */
    @Test(expected = LowMemoryException.class)
    public void testConstructorWithStringArgument() throws LowMemoryException {
        LOGGER.info("testConstructorWithStringArgument");
        final String message = "Test";
        throw new LowMemoryException(message);
    }
}
