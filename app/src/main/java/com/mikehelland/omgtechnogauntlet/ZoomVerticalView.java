package com.mikehelland.omgtechnogauntlet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * User: m
 * Date: 11/15/13
 * Time: 11:01 PM
 */
@SuppressWarnings("FieldCanBeLocal")
public class ZoomVerticalView extends View {

    private Paint paint;
    private Paint paintOff;
    private Paint paintRed;
    private Paint paintGreen;
    private Paint paintYellow;

    private int width = -1;
    private int height = -1;

    private float boxHeight;
    private float boxHeightHalf;

    private Channel mChannel;

    private Paint topPanelPaint;
    private Paint paintText;

    private Paint paintBeat;

    private int key;
    private int[] scale;

    private int frets = 0;
    private int[] fretMapping;

    private String[] keyCaptions = {"C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B"};
    private String[] soundsetCaptions;

    private boolean useScale = true;

    private Paint paintCurrentBeat;
    private Paint paintCurrentBeatRest;

    private int lowNote;

    private float zoomboxHeight = -1;
    private float zoomTop = -1;
    private float zoomBottom = -1;
    private boolean zooming = false;
    private int zoomingSkipBottom = 0;
    private int zoomingSkipTop = 0;
    private int showingFrets = 0;
    private int skipBottom = 0;
    private int skipTop = 0;

    public ZoomVerticalView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setARGB(255, 255, 255, 255);
        paint.setShadowLayer(10, 0, 0, 0xFFFFFFFF);

        paintText = new Paint();
        paintText.setARGB(255, 255, 255, 255);
        paintText.setTextSize(24);

        paintBeat =  new Paint();
        paintBeat.setARGB(255, 255, 0, 0);

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

        paintYellow = new Paint();
        paintYellow.setARGB(128, 255, 255, 0);
        paintYellow.setStyle(Paint.Style.FILL_AND_STROKE);


        topPanelPaint = new Paint();
        topPanelPaint.setARGB(255, 192, 192, 255);

        setBackgroundColor(Color.BLACK);

        paintCurrentBeat = new Paint();
        paintCurrentBeat.setARGB(128, 0, 255, 0);
        paintCurrentBeat.setShadowLayer(4, 0, 0, 0xFFFFFFFF);
        paintCurrentBeat.setStyle(Paint.Style.FILL_AND_STROKE);
        paintCurrentBeatRest = new Paint();
        paintCurrentBeatRest.setARGB(128, 255, 0, 0);
        paintCurrentBeatRest.setShadowLayer(4, 0, 0, 0xFFFFFFFF);
        paintCurrentBeatRest.setStyle(Paint.Style.FILL_AND_STROKE);

    }

    public void onDraw(Canvas canvas) {

        if (frets == 0 || mChannel == null || fretMapping == null || fretMapping.length == 0) {
            return;
        }

        //if (height != getHeight()) {
        width = getWidth();
        height = getHeight();
        boxHeight = (float)height / showingFrets;
        boxHeightHalf = boxHeight / 2;
        paint.setTextSize(boxHeightHalf);
        //}

        int noteNumber;
        int index;
        for (int fret = 1 ; fret <= showingFrets; fret++) {
            index = fret - 1 + skipBottom + zoomingSkipBottom;
            if (index < 0 || index >= fretMapping.length) {
                Log.e("MGH GuitarView onDraw", "Invalid note Index: " +
                        "fret: " + fret + ", skipBottom: " + skipBottom + ", zoomingSkipBottom: " + zoomingSkipBottom);
                continue;
            }
            noteNumber = fretMapping[index];

            if (noteNumber % 12 == key) {
                canvas.drawRect(width / 4, height - fret * boxHeight,
                        width / 4 * 3, height - (fret - 1) * boxHeight, paintOff);
            }

            canvas.drawLine(0, height - fret * boxHeight, width,
                    height - fret * boxHeight, paint);

            if (useScale || noteNumber < soundsetCaptions.length) {
                canvas.drawText(useScale ? keyCaptions[noteNumber % 12] : soundsetCaptions[noteNumber],
                        0, height - (fret - 1) * boxHeight - boxHeightHalf, paint);
            }

        }

        canvas.drawLine(0, height - 1, width,
                height - 1, paint);

    }

    public void setJam(Jam jam, Channel channel) {
        key = jam.getKey();
        scale = jam.getScale();
        mChannel = channel;

        setScaleInfo();

        int[] skipBottomAndTop = mChannel.getSurface().getSkipBottomAndTop();
        skipBottom = skipBottomAndTop[0];
        skipTop = skipBottomAndTop[1];
        showingFrets = Math.max(1, frets - skipTop - skipBottom);
    }

    public void setScaleInfo() {

        mChannel.debugTouchData.clear();

        frets = 0;

        useScale = mChannel.isAScale();

        int highNote;
        if (!useScale) {
            key = 0;
            lowNote = 0;
            highNote = mChannel.getSoundSet().getSounds().size() - 1;

            soundsetCaptions = mChannel.getSoundSet().getSoundNames();

        }
        else {
            lowNote = mChannel.getLowNote();
            highNote = mChannel.getHighNote();
        }

        int[] allFrets = new int[highNote - lowNote + 1];

        int s;
        boolean isInScale;
        for (int i = lowNote; i <= highNote; i++) {
            isInScale = false;

            for (s = 0; s < scale.length; s++) {
                if (!useScale || scale[s] == ((i - key) % 12)) {
                    isInScale = true;
                    break;
                }

            }

            if (isInScale) {
                allFrets[frets++] = i;
            }

        }

        fretMapping = new int[frets];
        System.arraycopy(allFrets, 0, fretMapping, 0, frets);

        showingFrets = frets;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mChannel == null || fretMapping == null || fretMapping.length == 0) {
            return true;
        }

        int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                float y;
                zoomTop = -1;
                zoomBottom = -1;
                for (int i = 0; i < event.getPointerCount(); i++) {
                    y = event.getY(i);
                    if (zoomTop == -1 || y < zoomTop) {
                        zoomTop = y;
                    }
                    if (y > zoomBottom) {
                        zoomBottom = y;
                    }
                }
                zoomboxHeight = boxHeight;
                zooming = true;
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                zooming = false;
                zoomBottom = -1;
                zoomTop = -1;
                skipBottom += zoomingSkipBottom;
                skipTop += zoomingSkipTop;
                zoomingSkipTop = 0;
                zoomingSkipBottom = 0;

                mChannel.getSurface().setSkipBottomAndTop(skipBottom, skipTop);

                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (zooming)
                    zoom(event);
                break;
            }
        }
        invalidate();
        return true;
    }

    private void zoom(MotionEvent event) {

        float top = -1;
        float bottom = -1;

        float y;
        for (int i = 0; i < event.getPointerCount(); i++) {
            y = event.getY(i);
            if (top == -1 || y < top) {
                top = y;
            }
            if (y > bottom) {
                bottom = y;
            }
        }

        float topDiff = zoomTop - top;
        float bottomDiff = bottom - zoomBottom;

        zoomingSkipTop = Math.max(skipTop * -1, (int)Math.floor(topDiff / zoomboxHeight));
        zoomingSkipBottom = Math.max(skipBottom * -1, (int)Math.floor(bottomDiff / zoomboxHeight));

        showingFrets = Math.max(1, frets - skipTop - skipBottom - zoomingSkipBottom - zoomingSkipTop);

        invalidate();
    }
}
