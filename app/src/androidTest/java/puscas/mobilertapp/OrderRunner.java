package puscas.mobilertapp;

import androidx.test.filters.FlakyTest;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

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
    * The constructor.
    *
    * @param clazz The class which should be scanned for annotations.
    * @throws InitializationError If the test class is malformed.
    */
    public OrderRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        logger.info("Sorting tests execution order.");
        final List<FrameworkMethod> originalTestsOrder = super.computeTestMethods();
        final List<FrameworkMethod> orderedTests = new ArrayList<>(originalTestsOrder);
        Collections.sort(orderedTests, (f1, f2) -> {
            final FlakyTest o1 = f1.getAnnotation(FlakyTest.class);
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

            return 0;
        });
        logger.info("Original test execution order: " + Arrays.toString(originalTestsOrder.toArray()));
        logger.info("Sorted test execution order: " + Arrays.toString(orderedTests.toArray()));

        return orderedTests;
    }
}
