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
public class MixerView extends View {

    private Paint paint;
    private Paint paintOff;
    private Paint paintRed;
    private Paint paintGreen;

    private int width = -1;
    private int width2 = -1;
    private int height = -1;

    private Jam mJam;
    private Channel mChannel;

    private Paint topPanelPaint;
    private Paint paintText;

    private static final int TOUCHING_AREA_VOLUME = 1;
    private static final int TOUCHING_AREA_PAN = 2;
    private static final int TOUCHING_AREA_NONE = 0;

    private int touchingArea = TOUCHING_AREA_NONE;

    private String channelName = "";
    private float channelNameWidth2;

    private int labelTextSize = 24;

    private float volume = 0.5f;
    private float pan = 0.0f;


    public MixerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setARGB(255, 255, 255, 255);
        paint.setShadowLayer(10, 0, 0, 0xFFFFFFFF);

        paintText = new Paint();
        paintText.setARGB(255, 255, 255, 255);
        paintText.setTextSize(labelTextSize);

        paintOff = new Paint();
        paintOff.setARGB(128, 128, 128, 128);
        paintOff.setStyle(Paint.Style.FILL);
        paintOff.setTextSize(paintText.getTextSize());

        paintRed = new Paint();
        paintRed.setARGB(128, 255, 0, 0);
        paintRed.setStyle(Paint.Style.FILL_AND_STROKE);

        paintGreen = new Paint();
        paintGreen.setARGB(128, 0, 255, 0);
        paintGreen.setStyle(Paint.Style.FILL_AND_STROKE);

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

        }

        float height2 = height / 2;

        canvas.drawLine(2, height2, width2 - 2,
                height2, paint);

        canvas.drawLine(2 + width2, height2, width - 2,
                height2, paint);

        canvas.drawText("L", width2 + 15,
                height - 5, paintText);

        canvas.drawText("R", width - 25,
                height - 5, paintText);

        canvas.drawText("Volume", 10,
                height - 5, paintText);

        canvas.drawText(channelName, width2 - channelNameWidth2, labelTextSize, paintText);

        canvas.drawRect(width2 * volume - 5, height / 3,
                        width2 * volume + 5, height / 3 * 2,
                topPanelPaint);

        canvas.drawRect(width2 * pan / 2 + width / 4 * 3 - 5, height / 4,
                        width2 * pan / 2 + width / 4 * 3 + 5, height / 4 * 3,
                topPanelPaint);

    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();

        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {

            if (x <= width2) {
                touchingArea = TOUCHING_AREA_VOLUME;
            }
            else {
                touchingArea = TOUCHING_AREA_PAN;
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

    public void setJam(Jam jam, Channel channel, String name) {
        mJam = jam;
        mChannel = channel;

        volume = channel.volume;

        setChannelName(name);

    }

    private void performTouch(float x) {
        if (touchingArea == TOUCHING_AREA_VOLUME) {
            volume = Math.max(0, Math.min(1, x / (width/2)));
            mChannel.volume = volume;
            invalidate();
        }
        else if (touchingArea == TOUCHING_AREA_PAN) {
            pan = Math.max(-1.0f, Math.min(1.0f, ((x - width2) / width2 - 0.5f) * 2));
            invalidate();
        }
    }

}
