package com.nimasystems.lightcast.ui.misc;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ButtonHighlighterOnTouchListener implements OnTouchListener {

    private static final int TRANSPARENT_GREY = Color.argb(0, 185, 185, 185);
    private static final int FILTERED_GREY = Color.argb(155, 185, 185, 185);

    ImageView imageView;
    TextView textView;

    public ButtonHighlighterOnTouchListener(final ImageView imageView) {
        super();
        this.imageView = imageView;
    }

    public ButtonHighlighterOnTouchListener(final TextView textView) {
        super();
        this.textView = textView;
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        if (imageView != null) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                imageView.setColorFilter(FILTERED_GREY);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                imageView.setColorFilter(TRANSPARENT_GREY); // or null
            }
        } else {
            for (final Drawable compoundDrawable : textView
                    .getCompoundDrawables()) {
                if (compoundDrawable != null) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        // we use PorterDuff.Mode. SRC_ATOP as our filter color
                        // is already transparent
                        // we should have use PorterDuff.Mode.LIGHTEN with a non
                        // transparent color
                        compoundDrawable.setColorFilter(FILTERED_GREY,
                                PorterDuff.Mode.SRC_ATOP);
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        compoundDrawable.setColorFilter(TRANSPARENT_GREY,
                                PorterDuff.Mode.SRC_ATOP); // or null
                    }
                }
            }
        }
        return false;
    }

}