package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class BeatView extends View {

    private Jam mJam;

    private Paint paintRed;
    private Paint paintGreen;
    private Paint paintYellow;

    public BeatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paintRed = new Paint();
        paintRed.setARGB(128, 255, 0, 0);
        paintRed.setStyle(Paint.Style.FILL_AND_STROKE);

        paintGreen = new Paint();
        paintGreen.setARGB(128, 0, 255, 0);
        paintGreen.setStyle(Paint.Style.FILL_AND_STROKE);

        paintYellow = new Paint();
        paintYellow.setARGB(128, 255, 255, 0);
        paintYellow.setStyle(Paint.Style.FILL_AND_STROKE);

    }

    public void onDraw(Canvas canvas) {
        if (mJam == null)
            return;

        float boxWidth = getWidth() / mJam.getBeats();
        for (int i = 1; i < mJam.getBeats(); i++) {
            canvas.drawLine(i * boxWidth, 0, i * boxWidth, getHeight(), paintYellow);
        }

        if (!mJam.isPaused()) {
            float beatBoxWidth = getWidth() / mJam.getTotalBeats();
            float beatBoxStart = (float) Math.floor(mJam.getCurrentSubbeat() / mJam.getSubbeats()) * beatBoxWidth;

            canvas.drawRect(beatBoxStart, 0, beatBoxStart + beatBoxWidth, getHeight(), paintGreen);
        }
        else {
            canvas.drawRect(0, 0, getWidth(), getHeight(), paintRed);
        }

    }

    void setJam(Jam jam) {
        mJam = jam;
        invalidate();
    }
}
