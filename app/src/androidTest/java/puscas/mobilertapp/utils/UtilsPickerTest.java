package puscas.mobilertapp.utils;

import android.widget.NumberPicker;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import puscas.mobilertapp.ViewActionNumberPicker;

/**
 * Helper class which contains helper methods about the {@link NumberPicker}s for the tests.
 */
public final class UtilsPickerTest {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(UtilsPickerTest.class.getName());

    /**
     * Private method to avoid instantiating this helper class.
     */
    private UtilsPickerTest() {
        final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        LOGGER.info(methodName);
    }

    /**
     * Helper method which changes the {@code value} of a {@link NumberPicker}.
     *
     * @param pickerName    The name of the {@link NumberPicker}.
     * @param pickerId      The identifier of the {@link NumberPicker}.
     * @param expectedValue The new expectedValue for the {@link NumberPicker}.
     */
    public static void changePickerValue(final String pickerName,
                                         final int pickerId,
                                         final int expectedValue) {
        LOGGER.info("changePickerValue");
        Espresso.onView(ViewMatchers.withId(pickerId))
            .perform(new ViewActionNumberPicker(expectedValue))
            .check((view, exception) -> {
                final NumberPicker numberPicker = view.findViewById(pickerId);
                Assertions.assertEquals(expectedValue, numberPicker.getValue(),
                    "Number picker '" + pickerName + "' with wrong value"
                );
            });
    }

}
