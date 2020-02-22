package puscas.mobilertapp.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.logging.Logger;

import static puscas.mobilertapp.utils.ConstantsUI.COLOR_NUMBER_PICKER;
import static puscas.mobilertapp.utils.ConstantsUI.TEXT_SIZE;

/**
 * Custom {@link NumberPicker} for the User Interface with smaller text size and black color for all the number pickers
 * used.
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

        final int color = Color.parseColor(COLOR_NUMBER_PICKER);
        ((TextView) child).setTextSize(TEXT_SIZE);
        ((TextView) child).setTextColor(color);
    }
}
