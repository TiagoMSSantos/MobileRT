package puscas.mobilertapp.constants;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;

import lombok.extern.java.Log;
import puscas.mobilertapp.exceptions.FailureException;

/**
 * Custom {@link NumberPicker} for the User Interface with smaller text size
 * and black color for all the number pickers used.
 */
@Log
public final class CustomNumberPicker extends NumberPicker {

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
        log.info("addView");

        if (!(child instanceof TextView)) {
            throw new FailureException("View cannot be cast to TextView.");
        }

        final TextView textView = (TextView) child;
        final int color = Color.parseColor(ConstantsUI.COLOR_NUMBER_PICKER);
        textView.setTextSize(ConstantsUI.TEXT_SIZE);
        textView.setTextColor(color);
    }

}
