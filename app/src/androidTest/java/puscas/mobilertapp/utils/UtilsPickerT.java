package puscas.mobilertapp.utils;

import android.widget.NumberPicker;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Assertions;
import puscas.mobilertapp.ViewActionNumberPicker;

/**
 * Helper class which contains helper methods about the {@link NumberPicker}s for the tests.
 */
@UtilityClass
@Log
public final class UtilsPickerT {

    /**
     * Helper method which changes the {@code value} of a {@link NumberPicker}.
     *
     * @param pickerName    The name of the {@link NumberPicker}.
     * @param pickerId      The identifier of the {@link NumberPicker}.
     * @param expectedValue The new expectedValue for the {@link NumberPicker}.
     */
    public static void changePickerValue(@NonNull final String pickerName,
                                         final int pickerId,
                                         final int expectedValue) {
        log.info("changePickerValue");
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
