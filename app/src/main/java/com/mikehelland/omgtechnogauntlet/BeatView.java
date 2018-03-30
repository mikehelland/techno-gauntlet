package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.mikehelland.omgtechnogauntlet.jam.Jam;

public class BeatView extends View {

    private Jam jam;

    private Paint paintRed;
    private Paint paintGreen;
    private Paint paintWhite;
    private Paint paintGrey;
    private Paint paintBlue;

    private int beats = 4;

    private boolean mShowingLoadProgress = false;
    private boolean mInitialized = false;
    private int mProgressMax = 1;
    private int mProgressI = 0;

    private String mText = "";
    private boolean mIsHeightSet = false;

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

        paintWhite.setTextAlign(Paint.Align.CENTER);

        paintGrey = new Paint();
        paintGrey.setARGB(100, 255, 255, 255);
        paintGrey.setStyle(Paint.Style.FILL_AND_STROKE);

        paintBlue = new Paint();
        paintBlue.setARGB(100, 128, 128, 255);
        paintBlue.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public void onDraw(Canvas canvas) {
        if (jam == null || !jam.isReady())
            return;

        if (!mIsHeightSet) {
            paintWhite.setTextSize(getHeight() * 0.80f);
            mIsHeightSet = true;
        }

        beats = jam.getBeats();
        float boxWidth = ((float)getWidth()) / jam.getTotalBeats();
        for (int i = 1; i < jam.getTotalBeats(); i++) {
            canvas.drawLine(i * boxWidth, 0, i * boxWidth, getHeight(),
                    i % beats == 0 ? paintWhite : paintGrey);
        }
        canvas.drawLine(0, getHeight() - 2, getWidth(), getHeight() - 2, paintWhite);

        if (!mInitialized || mShowingLoadProgress) {
            canvas.drawRect(0, 0, getWidth() * ((float)mProgressI / mProgressMax), getHeight(), paintBlue);
            mText = "Loading Sounds...";
        }
        else if (!jam.isPlaying()){
            canvas.drawRect(0, 0, getWidth(), getHeight(), paintRed);
            mText = "Play";
        }
        if (jam.isPlaying()) {
            float beatBoxWidth = ((float)getWidth()) / jam.getTotalBeats();
            float beatBoxStart = jam.getCurrentSubbeat() / jam.getSubbeats() * beatBoxWidth;

            canvas.drawRect(beatBoxStart, 0, beatBoxStart + beatBoxWidth, getHeight(), paintGreen);
            if (!mShowingLoadProgress) {
                mText = "Stop";
            }
        }

        canvas.drawText(mText, getWidth() / 2, getHeight() - getHeight() * 0.2f, paintWhite);
    }

    void setJam(Jam jam) {
        this.jam = jam;
        postInvalidate();
    }

    boolean isShowingLoadProgress() {return mShowingLoadProgress;}

    void showLoadProgress(int max) {
        mProgressMax = max;
        mProgressI = 0;
        mShowingLoadProgress = true;
        mInitialized = true;
    }

    void incrementProgress() {
        mProgressI++;
        if (mProgressI >= mProgressMax) {
            mProgressI = 0;
            mShowingLoadProgress = false;
        }
        postInvalidate();
    }
}
