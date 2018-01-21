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
    private Paint paintWhite;
    private Paint paintGrey;

    private int beats = 4;

    public BeatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paintRed = new Paint();
        paintRed.setARGB(128, 255, 0, 0);
        paintRed.setStyle(Paint.Style.FILL_AND_STROKE);

        paintGreen = new Paint();
        paintGreen.setARGB(128, 0, 255, 0);
        paintGreen.setStyle(Paint.Style.FILL_AND_STROKE);

        paintWhite = new Paint();
        paintWhite.setARGB(200, 255, 255, 255);
        paintWhite.setStyle(Paint.Style.FILL_AND_STROKE);

        paintGrey = new Paint();
        paintGrey.setARGB(100, 255, 255, 255);
        paintGrey.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public void onDraw(Canvas canvas) {
        if (mJam == null)
            return;

        beats = mJam.getBeats();
        float boxWidth = ((float)getWidth()) / mJam.getTotalBeats();
        for (int i = 1; i < mJam.getTotalBeats(); i++) {
            canvas.drawLine(i * boxWidth, 0, i * boxWidth, getHeight(),
                    i % beats == 0 ? paintWhite : paintGrey);
        }
        canvas.drawLine(0, getHeight() - 2, getWidth(), getHeight() - 2, paintWhite);

        if (!mJam.isPaused()) {
            float beatBoxWidth = ((float)getWidth()) / mJam.getTotalBeats();
            float beatBoxStart = mJam.getCurrentSubbeat() / mJam.getSubbeats() * beatBoxWidth;

            canvas.drawRect(beatBoxStart, 0, beatBoxStart + beatBoxWidth, getHeight(), paintGreen);
        }
        else {
            canvas.drawRect(0, 0, getWidth(), getHeight(), paintRed);
        }

    }

    void setJam(Jam jam) {
        mJam = jam;
        postInvalidate();
    }
}
