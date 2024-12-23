package puscas.mobilertapp;

import androidx.test.filters.FlakyTest;

import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;

/**
* The {@link BlockJUnit4ClassRunner JUnit 4 Class Runner} which sorts the tests
* execution order.
* The goal of this class is to allow the user to set the most flaky tests to be executed
* first and thus make the pipeline as fast as possible if one test already failed.
*/
public class OrderRunner extends BlockJUnit4ClassRunner {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(OrderRunner.class.getSimpleName());

    /**
     * Whether the test has the {@link FlakyTest} annotation or not.
     */
    private boolean hasFlakyTestAnnotation = false;

    /**
    * The constructor.
    *
    * @param clazz The class which should be scanned for annotations.
    * @throws InitializationError If the test class is malformed.
    */
    public OrderRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    /**
    * Sort test methods by the defined {@link Order} annotation.
    * <p>
    * Also, it gives priority to tests annotated with {@link FlakyTest} so they are
    * executed first.
    * <p>
    * {@inheritDoc}
    */
    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        logger.info("Sorting tests execution order.");
        final List<FrameworkMethod> originalTestsOrder = super.computeTestMethods();
        final List<FrameworkMethod> orderedTests = new ArrayList<>(originalTestsOrder);
        Collections.sort(orderedTests, (f1, f2) -> {
            final Order orderTest1 = f1.getAnnotation(Order.class);
            final Order orderTest2 = f2.getAnnotation(Order.class);
            final FlakyTest flakyTest1 = f1.getAnnotation(FlakyTest.class);
            final FlakyTest flakyTest2 = f2.getAnnotation(FlakyTest.class);

            if (orderTest1 == null && orderTest2 == null) {
                return compareFlakyTests(flakyTest1, flakyTest2);
            }
            if (orderTest1 != null && orderTest2 == null) {
                return -1;
            }
            if (orderTest1 == null) {
                return 1;
            }

            final int order = orderTest1.order() - orderTest2.order();
            if (order == 0) {
                return compareFlakyTests(flakyTest1, flakyTest2);
            }

            return order;
        });
        logger.info("Original test execution order: " + Arrays.toString(originalTestsOrder.toArray()));
        logger.info("Sorted test execution order: " + Arrays.toString(orderedTests.toArray()));

        return orderedTests;
    }

    /**
    * This implementation ignores errors from tests that are marked as {@link FlakyTest flaky} with the annotation.
    * <p>
    * {@inheritDoc}
    *
    * @param method The {@link FrameworkMethod method} of the test.
    * @param test   The {@link Test} object.
    */
    @Override
    protected Statement methodInvoker(final FrameworkMethod method, final Object test) {
        this.hasFlakyTestAnnotation = method.getAnnotation(FlakyTest.class) != null;
        if (this.hasFlakyTestAnnotation) {
            return new InvokeMethodIgnoringErrors(method, test);
        }
        return super.methodInvoker(method, test);
    }

    @Override
    protected List<TestRule> getTestRules(final Object testTarget) {
        final List<TestRule> testRules = super.getTestRules(testTarget);
        if (this.hasFlakyTestAnnotation) {
            final String testClassName = testTarget.getClass().getSimpleName();
            final String testTimeoutRuleName = Timeout.class.getSimpleName();
            logger.warning("Ignoring '" + testTimeoutRuleName + "' test rule for the test '" + testClassName + "' because it is marked as a flaky one.");
            for (final TestRule testRule : testRules) {
                if (testRule.getClass().getSimpleName().equals(testTimeoutRuleName)) {
                    testRules.remove(testRule);
                }
            }
        }
        return testRules;
    }

    /**
     * Helper method which compares 2 {@link Test tests} based if they have the {@link FlakyTest} annotation.
     *
     * @param test1 The {@link FlakyTest} of the 1st test.
     * @param test2 The {@link FlakyTest} of the 2nd test.
     * @return {@code 0} if they are equal, {@code -1} if the 1st test should be executed first, or {@code 1} if the 2nd test should be executed first.
     */
    private int compareFlakyTests(@Nullable final FlakyTest test1, @Nullable final FlakyTest test2) {
        if (test1 == null && test2 == null) {
            return 0;
        }
        if (test1 != null && test2 != null) {
            return 0;
        }
        if (test1 != null) {
            return -1;
        }
        return 1;
    }
}
