package puscas.mobilertapp.constants;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.logging.Logger;

import puscas.mobilertapp.exceptions.FailureException;

/**
 * Custom {@link NumberPicker} for the User Interface with smaller text size
 * and black color for all the number pickers used.
 */
public final class CustomNumberPicker extends NumberPicker {

    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(CustomNumberPicker.class.getSimpleName());

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
        logger.info("addView");

        if (child instanceof TextView) {
            final int color = Color.parseColor(ConstantsUI.COLOR_NUMBER_PICKER);
            final TextView textView = (TextView) child;
            textView.setTextSize(ConstantsUI.TEXT_SIZE);
            textView.setTextColor(color);
            return;
        }

        if (child instanceof ImageButton) {
            // To be compatible with Android API 15 or older.
            return;
        }

        throw new FailureException("View cannot be cast to TextView.");
    }

}
