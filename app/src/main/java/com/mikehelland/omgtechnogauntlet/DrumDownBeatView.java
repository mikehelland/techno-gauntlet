package com.mikehelland.omgtechnogauntlet;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * User: m
 * Date: 11/15/13
 * Time: 11:01 PM
 */
public class DrumDownBeatView extends View {

    private boolean setThisUp = true;

    private Paint paint;
    private Paint paintOff;

    private int height = -1;

    private int marginX;
    private int marginY;

    private int boxWidth;
    private int boxHeight;

    private int wide = -1;
    private int tall = -1;

    private boolean[][] data;
    private Jam mJam;

    private Paint topPanelPaint;
    private Paint paintText;

    private int firstRowButton = 0;

    private String[][] captions;
    private float[][] captionWidths;

    private Paint paintBeat;

    private Paint blackPaint;

    private int adjustUp = 12;
    private int adjustDown = 18;

    private boolean isLive = false;
    private int lastX = -1;
    private int lastY = -1;

    //private DrumChannel mChannel;
    private Channel mChannel;

    public DrumDownBeatView(Context context, AttributeSet attrs) {
        super(context, attrs);

        makePaints();
    }

    private void makePaints() {

        paint = new Paint();
        paint.setARGB(255, 255, 255, 255);
        paint.setShadowLayer(10, 0, 0, 0xFFFFFFFF);

        paintText = new Paint();
        paintText.setARGB(255, 255, 255, 255);
        paintText.setTextSize(24);

        paintBeat =  new Paint();
        paintBeat.setARGB(255, 255, 0, 0);

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

//        setBackgroundColor(Color.BLACK);
    }

    public void onDraw(Canvas canvas) {

        if (mChannel == null || tall == -1) {
            //maybe a loading sign?
            Log.d("MGH drum beat view", "null or -1");
            return;
        }

        if (height != getHeight()) {
            setThisUp = true;
        }

        if (setThisUp) {

            tall = mChannel.getSoundSet().getSounds().size();

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
        //todo does this do anything?
        canvas.drawRect(0, 0,
                boxWidth, height,
                topPanelPaint);
        //this is cool, green for playing, red for not?
        if (mChannel.isEnabled())
            paintBeat.setARGB(255, 0, 255, 0);
        else
            paintBeat.setARGB(255, 255, 0, 0);


        boolean on;

        if (mJam != null && mJam.isPlaying()) {
            int i = 1 + (mJam.getCurrentSubbeat() % (wide - 1));
            int j = mJam.getCurrentSubbeat() / (wide - 1);

            i = mJam.getCurrentSubbeat() / mJam.getSubbeats();
            j = 1;

            canvas.drawRect(boxWidth * i,  j * boxHeight,
                    boxWidth * i + boxWidth, j * boxHeight + boxHeight,
                    paintBeat);
        }

        for (int j = 0; j < tall; j++) {
            //todo pretty sure can do better then this huge nested function
            if (j < captions.length) {
                if (captionWidths[j].length == 1) {
                    canvas.drawText(captions[j][0], boxWidth / 2 - captionWidths[j][0] / 2,
                            j * boxHeight + boxHeight / 2 + 6, blackPaint);
                }
                else {
                    canvas.drawText(captions[j][0], boxWidth / 2 - captionWidths[j][0] / 2,
                            j * boxHeight + boxHeight / 2 - adjustUp, blackPaint);
                    canvas.drawText(captions[j][1], boxWidth / 2 - captionWidths[j][1] / 2,
                            j * boxHeight + boxHeight / 2 + adjustDown, blackPaint);
                }
            }


            for (int i = 0; i < wide; i++) {

                on = data[j][i]; //(i - 1) + j * (wide - 1)]) || (i==0 && j==firstRowButton);
                //on = (i > 0 && data[(i - 1) + j * (wide - 1)]) || (i==0 && j==firstRowButton);
                canvas.drawRect(boxWidth + boxWidth * i + marginX,  j * boxHeight + marginY,
                        boxWidth + boxWidth * i + boxWidth - marginX, j * boxHeight + boxHeight - marginY,
                        on? paint:paintOff);

                //if (i == 0) {

                //}
                //else {
                    canvas.drawText(Integer.toString(i + 1), boxWidth + i * boxWidth + boxWidth / 2 - 6,
                            boxHeight * j + boxHeight / 2 + 6, on? paintOff:paintText);
                //}
            }
        }

    }


    public boolean onTouchEvent(MotionEvent event) {

        int boxX = (int)Math.floor(event.getX() / boxWidth);
        int boxY = (int)Math.floor(event.getY() / boxHeight);

        //boxX = Math.min(wide - 1, Math.max(0, boxX));
        boxX = Math.min(wide, Math.max(0, boxX));
        boxY = Math.min(tall - 1, Math.max(0, boxY));

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            if (boxX == 0) {
                handleFirstColumn(boxY);
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
        //todo
        //((Main)getContext()).onModify();

        int beatColumns = wide - 1;
        //int i = x % beatColumns + y * beatColumns;

        //if (i >= 0 && data.length > i) {
        data[y][x] = !data[y][x];
            mChannel.setPattern(y, x * mJam.getSubbeats(), data[y][x]);
        //}

    }

    public void setJam(Jam jam, Channel channel) {
        mJam = jam;
        wide = mJam.getBeats();
        setChannel(channel);

        mJam.addInvalidateOnBeatListener(this);
        //handleFirstColumn(0);
        setThisUp = true;
    }

    void setChannel(Channel channel) {
        mChannel = channel;
        tall = mChannel.getSoundSet().getSounds().size();

        data = new boolean[tall][wide];
        int subbeats = mJam.getSubbeats();

        for (int i = 0; i < tall; i++) {
            for (int j = 0; j < wide; j++) {
                data[i][j] = mChannel.pattern[i][j * subbeats];
            }
        }
    }

    void handleFirstColumn(int y)  {
        //todo play the sample live
    }

    public void setCaptions() {

        ArrayList<SoundSet.Sound> sounds = mChannel.getSoundSet().getSounds();
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