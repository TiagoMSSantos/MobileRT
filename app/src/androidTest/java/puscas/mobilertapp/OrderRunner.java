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
    * {@inheritDoc}
    */
    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        logger.info("Sorting tests execution order.");
        final List<FrameworkMethod> originalTestsOrder = super.computeTestMethods();
        final List<FrameworkMethod> orderedTests = new ArrayList<>(originalTestsOrder);
        Collections.sort(orderedTests, (f1, f2) -> {
            final Order o1 = f1.getAnnotation(Order.class);
            final Order o2 = f2.getAnnotation(Order.class);

            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 != null && o2 == null) {
                return -1;
            }
            if (o1 == null) {
                return 1;
            }

            return o1.order() - o2.order();
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
}
