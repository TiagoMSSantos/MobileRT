package puscas.mobilertapp;

import androidx.test.filters.FlakyTest;

import com.google.common.base.Preconditions;

import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.util.logging.Logger;

import puscas.mobilertapp.utils.UtilsLogging;

/**
 * Implementation of {@link Statement} that executes the {@link Test} methods and
 * ignore errors.
 * <p>
 * This should be used by flaky tests only.
 */
public class InvokeMethodIgnoringErrors extends Statement {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(InvokeMethodIgnoringErrors.class.getSimpleName());

    /**
     * The {@link Test method}.
     */
    private final FrameworkMethod testMethod;

    /**
     * The fixture to run a particular test {@code method} against.
     */
    private final Object testTarget;

    /**
     * Constructor.
     */
    public InvokeMethodIgnoringErrors(final FrameworkMethod testMethod, final Object testTarget) {
        final FlakyTest annotation = testMethod.getAnnotation(FlakyTest.class);
        Preconditions.checkNotNull(annotation, "The test '" + testMethod.getName() + "' should be marked with @FlakyTest annotation in order to use '" + getClass().getSimpleName() + "'.");

        this.testMethod = testMethod;
        this.testTarget = testTarget;
    }

    @Override
    public void evaluate() throws Throwable {
        try {
            this.testMethod.invokeExplosively(this.testTarget);
        } catch (final Exception ex) {
            logger.severe("The test '" + this.testMethod.getName() + "' failed, but the errors were ignored because it was marked as a flaky test.");
            UtilsLogging.logThrowable(ex, this.testMethod.getName());
        }
    }
}
