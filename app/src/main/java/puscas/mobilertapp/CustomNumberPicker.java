package puscas.mobilertapp;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

public final class CustomNumberPicker extends NumberPicker {
    public CustomNumberPicker(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void addView(final View child, final ViewGroup.LayoutParams params) {
        super.addView(child, params);
        final float textSize = 15.0f;//15.0f
        final int color = Color.parseColor("#000000");
        ((TextView) child).setTextSize(textSize);
        ((TextView) child).setTextColor(color);
    }
}
