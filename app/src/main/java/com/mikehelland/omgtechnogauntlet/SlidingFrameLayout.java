package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:52 PM
 */

public class SlidingFrameLayout extends RelativeLayout {

    public SlidingFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidingFrameLayout(Context context) {
        super(context);
    }

    public float getXFraction() {
        return getX() / getWidth(); // TODO: guard divide-by-zero
    }
    public float getYFraction() {
        return getY() / getHeight(); // TODO: guard divide-by-zero
    }

    public void setXFraction(float xFraction) {
        // TODO: cache width
        final int width = getWidth();
        setX((width > 0) ? (xFraction * width) : -9999);
    }

    public void setYFraction(float xFraction) {
        // TODO: cache width
        final int height = getHeight();
        setY((height > 0) ? (xFraction * height) : -9999);
    }
}

