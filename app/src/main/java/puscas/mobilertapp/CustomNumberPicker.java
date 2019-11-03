package puscas.mobilertapp;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom number picker for the User Interface with smaller text size and black color for all the number pickers used.
 */
public final class CustomNumberPicker extends NumberPicker {
    private static final Logger LOGGER = Logger.getLogger(CustomNumberPicker.class.getName());

    public CustomNumberPicker(@NonNull final Context context, @NonNull final AttributeSet attrs) {
        super(context, attrs);

        LOGGER.log(Level.INFO, "CustomNumberPicker");
    }

    @Override
    public void addView(@NonNull final View child, @NonNull final ViewGroup.LayoutParams params) {
        super.addView(child, params);

        LOGGER.log(Level.INFO, "addView");
        final float textSize = 15.0f;
        final int color = Color.parseColor("#000000");
        ((TextView) child).setTextSize(textSize);
        ((TextView) child).setTextColor(color);
    }
}
