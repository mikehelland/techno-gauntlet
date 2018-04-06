package com.mikehelland.omgtechnogauntlet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.mikehelland.omgtechnogauntlet.jam.Jam;
import com.mikehelland.omgtechnogauntlet.jam.Part;
import com.mikehelland.omgtechnogauntlet.jam.SoundSet;

import java.util.ArrayList;

/**
 * User: m
 * Date: 11/15/13
 * Time: 11:01 PM
 */
public class DrumView extends View {

    private Paint paint;
    private Paint paintOff;
    private Paint paintMute;
    private Paint paintCurrentTrack;

    private int height = -1;

    private int marginX;
    private int marginY;

    private int boxWidth;
    private int boxHeight;

    private int wide = 4;
    private int tall = 8;

    //private boolean[] trackData;
    private boolean[][] data;
    private Jam mJam;

    private Paint topPanelPaint;
    private Paint paintText;

    private int firstRowButton = -1;

    private String[][] captions;
    private float[][] captionWidths;
    private int captionHeight;

    private Paint paintBeat;

    private Paint blackPaint;

    private int adjustUp = 12;
    private int adjustDown = 18;

    private boolean isLive = false;
    private int lastX = -1;
    private int lastY = -1;

    private Part part;

    private long mLastClickTime = 0;

    public DrumView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setARGB(255, 255, 255, 255);
        paint.setShadowLayer(10, 0, 0, 0xFFFFFFFF);

        paintText = new Paint();
        paintText.setARGB(255, 255, 255, 255);
        paintText.setTextSize(24);

        paintBeat =  new Paint();
        paintBeat.setARGB(255, 255, 0, 0);

        paintCurrentTrack =  new Paint();
        paintCurrentTrack.setARGB(128, 255, 255, 255);

        paintMute =  new Paint();
        paintMute.setARGB(128, 255, 0, 0);
        paintMute.setShadowLayer(10, 0, 0, 0xFFFFFFFF);

        paintOff = new Paint();
        paintOff.setARGB(255, 128, 128, 128);
        //paintOff.setShadowLayer(10, 0, 0, 0xFFFFFFFF);
        paintOff.setStyle(Paint.Style.STROKE);

        blackPaint = new Paint();
        blackPaint.setARGB(255, 0, 0, 0);
        blackPaint.setStyle(Paint.Style.STROKE);

        paintOff.setTextSize(paintText.getTextSize());
        blackPaint.setTextSize(22);

        topPanelPaint = new Paint();
        topPanelPaint.setARGB(255, 192, 192, 255);
    }

    public void onDraw(Canvas canvas) {
        if (mJam == null || part == null || tall == 0 || wide == -1) {
            return;
        }

        if (height != getHeight()) {
            int width = getWidth();
            height = getHeight();
            marginX = width / 64;
            marginY = height / 128;
            boxWidth = width / (wide + 1);
            boxHeight = height / tall;

            if (boxHeight < 90) {
                blackPaint.setTextSize(12);
                adjustUp = 6;
                adjustDown = 8;
            }


            setCaptions();
        }

        canvas.drawRect(0, 0,
                boxWidth, height,
                topPanelPaint);

        //todo um, is this effecient? Why not create a paintRed and paintGreen
        if (!part.getMute())
            paintBeat.setARGB(255, 0, 255, 0);
        else
            paintBeat.setARGB(255, 255, 0, 0);


        boolean on;

        if (mJam != null && mJam.isPlaying()) {
            if (firstRowButton > -1) {
                int i = 1 + (mJam.getCurrentSubbeat() % wide);
                int j = mJam.getCurrentSubbeat() / wide ;
                canvas.drawRect(boxWidth * i,  j * boxHeight,
                        boxWidth * i + boxWidth, j * boxHeight + boxHeight,
                        paintBeat);
            }
            else {
                int i = mJam.getCurrentSubbeat() / mJam.getSubbeats();
                canvas.drawRect(boxWidth  + boxWidth * i,  0,
                        boxWidth + boxWidth * i + boxWidth, height,
                        paintBeat);
            }
        }

        if (captions != null && captions.length > 0 && captionWidths != null && captionWidths.length > 0) {
            captionHeight = height / captions.length;
            for (int j = 0; j < captions.length; j++) {
                if (j < captions.length) {

                    if (part.getSequencerPattern().getTrack(j).isMuted()) {
                        canvas.drawRect(marginX, j * captionHeight + marginY,
                                boxWidth - marginX, j * captionHeight + captionHeight - marginY,
                                paintMute);
                    }

                    if (firstRowButton == j) {
                        canvas.drawRect(marginX, j * captionHeight + marginY,
                                boxWidth - marginX, j * captionHeight + captionHeight - marginY,
                                paintCurrentTrack);
                    }

                    if (captionWidths[j].length == 1) {
                        canvas.drawText(captions[j][0], boxWidth / 2 - captionWidths[j][0] / 2,
                                j * captionHeight + captionHeight / 2 + 6, blackPaint);
                    } else {
                        canvas.drawText(captions[j][0], boxWidth / 2 - captionWidths[j][0] / 2,
                                j * captionHeight + captionHeight / 2 - adjustUp, blackPaint);
                        canvas.drawText(captions[j][1], boxWidth / 2 - captionWidths[j][1] / 2,
                                j * captionHeight + captionHeight / 2 + adjustDown, blackPaint);
                    }
                }
            }
        }

        for (int j = 0; j < tall; j++) {
            for (int i = 0; i < wide; i++) {
                if (firstRowButton == -1) {
                    if (j >= data.length || j < 0 || i * mJam.getSubbeats() >= data[j].length) {
                        break;
                    }
                }
                else if (firstRowButton > -1) {
                    if (firstRowButton >= data.length ||
                            (i + j * wide) >= data[firstRowButton].length) {
                        break;
                    }
                }

                on = (firstRowButton == -1) ? data[j][i * mJam.getSubbeats()] : data[firstRowButton][i + j * wide];

                canvas.drawRect(boxWidth + boxWidth * i + marginX,  j * boxHeight + marginY,
                        boxWidth + boxWidth * i + boxWidth - marginX, j * boxHeight + boxHeight - marginY,
                        on? paint:paintOff);

                if (firstRowButton == -1) {
                    canvas.drawText(Integer.toString(i + 1), boxWidth + i * boxWidth + boxWidth / 2 - 6,
                            boxHeight * j + boxHeight / 2 + 6, on? paintOff:paintText);
                }
                else {
                    canvas.drawText(i==0 ? Integer.toString(j+1) : i==1 ? "e" : i== 2 ? "+":"a",
                            boxWidth + i * boxWidth + boxWidth / 2 - 6,
                            boxHeight * j + boxHeight / 2 + 6, on? paintOff:paintText);
                }
            }
        }

    }


    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {

        if (mJam == null || part == null || boxHeight == 0 || boxWidth == 0 || captionHeight == 0) {
            return true;
        }

        int boxX = (int)Math.floor(event.getX() / boxWidth);
        int boxY = (int)Math.floor(event.getY() / boxHeight);

        boxX = Math.min(wide, Math.max(0, boxX));
        boxY = Math.min(tall - 1, Math.max(0, boxY));

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            if (boxX == 0) {
                handleFirstColumn((int)Math.floor(event.getY() / captionHeight));
            }
            else {
                handleTouch(boxX - 1, boxY);
                isLive = true;
            }
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (isLive && boxX > 0) {

                if (boxX != lastX || boxY != lastY) {
                    handleTouch(boxX - 1, boxY);
                }
            }
        }

        lastX = boxX;
        lastY = boxY;

        if (event.getAction() == MotionEvent.ACTION_UP) {
            isLive = false;
            lastX = -1;
            lastY = -1;
        }

        invalidate();
        return true;
    }

    private void handleTouch(int x, int y) {

        //todo this hits the data directly. Should be going through jam
        if (firstRowButton == -1) {
            x = x * mJam.getSubbeats();

            if (y > -1 && y < data.length && x > -1 && x < data[y].length) {
                data[y][x] = !data[y][x];
            }
        }
        else {
            int i = x % wide + y * wide;

            if (i >= 0 && firstRowButton < data.length && i < data[firstRowButton].length) {
                data[firstRowButton][i] = !data[firstRowButton][i];
            }
        }
    }

    public void setJam(Jam jam, Part channel) {
        mJam = jam;
        part = channel;

        //todo what if there are more sounds in the soundset than the track listing, hmm?
        tall = part.getSoundSet().getSounds().size();
        wide = mJam.getTotalBeats();

        data = part.getPattern();
    }


    void handleFirstColumn(int y)  {
        if (part == null || data == null || y < 0 || y >= captions.length) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - mLastClickTime < 200) {
            mJam.setPartTrackMute(part, part.getSequencerPattern().getTrack(y),
                    !part.getSequencerPattern().getTrack(y).isMuted());
        }

        mLastClickTime = now;

        if (firstRowButton == y) {
            firstRowButton = -1;
            wide = mJam.getTotalBeats();
            tall = data.length;
        }
        else {
            firstRowButton = y;
            wide = mJam.getSubbeats();
            tall = mJam.getTotalBeats();
        }

        height = -1;
        postInvalidate();
    }

    public void setCaptions() {

        ArrayList<SoundSet.Sound> sounds = part.getSoundSet().getSounds();
        captionWidths = new float[sounds.size()][];
        captions = new String[sounds.size()][];

        for (int i = 0; i < sounds.size(); i++) {

            captions[i] = sounds.get(i).getName().split(" ");
            captionWidths[i] = new float[captions[i].length];
            for (int j = 0; j < captions[i].length; j++) {
                captionWidths[i][j] = blackPaint.measureText(captions[i][j]) ;
            }

        }

    }


}