package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * User: m
 * Date: 11/15/13
 * Time: 11:01 PM
 */
public class SampleSpeedView extends View {

    private Paint paint;
    private Paint paintOrange;

    private int width = -1;
    private int width2 = -1;
    private int height = -1;

    private Jam mJam;
    private Channel mChannel;

    private Paint topPanelPaint;
    private Paint paintText;

    private static final int TOUCHING_AREA_RESET = 1;
    private static final int TOUCHING_AREA_SPEED = 2;
    private static final int TOUCHING_AREA_NONE = 0;

    private int touchingArea = TOUCHING_AREA_NONE;

    private String channelName = "";
    private float channelNameWidth2;

    private int labelTextSize = 48;

    private float resetButtonWidth = -1;
    private float speedStart = -1;
    private float controlMargin = 5;
    private float speedWidth = -1;

    private int channelNumber = 0;

    public SampleSpeedView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setARGB(255, 255, 255, 255);
        paint.setShadowLayer(10, 0, 0, 0xFFFFFFFF);

        paintText = new Paint();
        paintText.setARGB(255, 255, 255, 255);
        paintText.setTextSize(labelTextSize);

        paintOrange = new Paint();
        paintOrange.setARGB(255, 255, 165, 0);
        paintOrange.setStyle(Paint.Style.FILL_AND_STROKE);

        topPanelPaint = new Paint();
        topPanelPaint.setARGB(255, 192, 192, 255);

        setBackgroundColor(Color.BLACK);

        setChannelName("Test Trackname");

    }

    public void setChannelName(String newChannelName) {
        channelName = newChannelName;
        channelNameWidth2 = paintText.measureText(channelName) / 2;

    }

    public void onDraw(Canvas canvas) {

        if (height == -1) {
            width = getWidth();
            height = getHeight();
            width2 = width / 2;

            resetButtonWidth = paintText.measureText(" Mute ");
            speedStart = resetButtonWidth + controlMargin;
            speedWidth = (width - speedStart) - 2 * controlMargin;
        }

        float speed = mChannel.getSampleSpeed();

        float height2 = height / 2;

        canvas.drawRect(0, 0, resetButtonWidth, height, paintOrange);
        canvas.drawText(" Reset ", 0, height2, paintText);


        canvas.drawLine(speedStart + controlMargin, height2, speedStart + speedWidth - controlMargin,
                height2, paint);

        canvas.drawRect(speedStart + speedWidth * speed / 2 - controlMargin, 0,
                speedStart + speedWidth * speed / 2 + controlMargin, height,
                topPanelPaint);

        canvas.drawRect(speedStart, 0, speedStart + speedWidth * speed / 2, height, paintOrange);


        canvas.drawText(channelName, speedStart + controlMargin, height2, paintText);
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();

        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {

            if (x <= resetButtonWidth) {
                mChannel.setSampleSpeed(1);
                touchingArea = TOUCHING_AREA_RESET;
            }
            else if (x <= speedStart + speedWidth) {
                touchingArea = TOUCHING_AREA_SPEED;
            }

            performTouch(x);
        }

        if (action == MotionEvent.ACTION_MOVE) {

            performTouch(x);
        }

        if (action == MotionEvent.ACTION_UP) {

            touchingArea = TOUCHING_AREA_NONE;
        }

        invalidate();
        return true;
    }

    public void setJam(Jam jam, Channel channel, String name, int channelNumber) {
        mJam = jam;
        mChannel = channel;
        this.channelNumber = channelNumber;

        setChannelName(name);

    }

    private void performTouch(float x) {
        if (touchingArea == TOUCHING_AREA_SPEED) {
            float volume = Math.max(0, Math.min(1, (x - speedStart) / speedWidth));

            float speed = Math.max(0, Math.min(2.0f, ((x - speedStart) / speedWidth) * 2));

            //the reason this should be done through the jam is for forwarding bluetooth messages
            //mJam.setChannelVolume(channelNumber, volume, null);

            mChannel.setSampleSpeed(speed);
        }
    }
}