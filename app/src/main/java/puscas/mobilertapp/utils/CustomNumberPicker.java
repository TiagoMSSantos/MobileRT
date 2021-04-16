package puscas.mobilertapp.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.logging.Logger;
import puscas.mobilertapp.exceptions.FailureException;

/**
 * Custom {@link NumberPicker} for the User Interface with smaller text size
 * and black color for all the number pickers used.
 */
public final class CustomNumberPicker extends NumberPicker {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(CustomNumberPicker.class.getName());

    /**
     * The constructor for this class.
     *
     * @param context The {@link Context} of the Android system.
     * @param attrs   The {@link AttributeSet} of the Android system.
     */
    public CustomNumberPicker(@NonNull final Context context, @NonNull final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void addView(@NonNull final View child, @NonNull final ViewGroup.LayoutParams params) {
        super.addView(child, params);
        LOGGER.info("addView");

        if (!(child instanceof TextView)) {
            throw new FailureException("View cannot be cast to TextView.");
        }

        final TextView textView = (TextView) child;
        final int color = Color.parseColor(ConstantsUI.COLOR_NUMBER_PICKER);
        textView.setTextSize(ConstantsUI.TEXT_SIZE);
        textView.setTextColor(color);
    }

}
