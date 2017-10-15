package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * User: m
 * Date: 11/15/13
 * Time: 11:01 PM
 */
public class ChordsView extends View {

    private Paint paint;
    private Paint paintOff;
    private Paint paintCurrentBeat;

    private int width = -1;
    private int height;

    private int marginX;
    private int marginY;

    private int boxWidth;
    private int boxHeight;

    private Jam mJam;
    private Main mActivity;

    private NoteList mList;;

    private Bitmap images[];

    private String editCaption;
    private float editCaptionLength = 0f;
    private float editCaptionLeft = 0f;

    private boolean editMode = false;

    private boolean pressingEditButton = false;

    private boolean isTouching = false;

    private ArrayList<MelodyTouch> touches = new ArrayList<MelodyTouch>();

    private Paint drawPaint;

    private int[] chords;
    private int[] scale;

    public ChordsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setARGB(255, 0, 0, 0);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(16.0f);
        //paint.setShadowLayer(10, 0, 0, 0xFFFFFFFF);

        paintOff = new Paint();
        paintOff.setARGB(255, 255, 0, 0);
        //paintOff.setShadowLayer(10, 0, 0, 0xFFFFFFFF);
        paintOff.setStyle(Paint.Style.STROKE);

        paintCurrentBeat = new Paint();
        paintCurrentBeat.setARGB(128, 255, 0, 0);
        paintCurrentBeat.setShadowLayer(4, 0, 0, 0xFFFFFFFF);
        paintCurrentBeat.setStyle(Paint.Style.FILL_AND_STROKE);

        drawPaint = new Paint();
        drawPaint.setARGB(255, 0, 0, 0);
        drawPaint.setStrokeWidth(5.0f);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setShadowLayer(10.0f, 10, 10, Color.GRAY);


        images = new Bitmap[8];
        images[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_one);
        images[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_two);
        images[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_three);
        images[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_four);
        images[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_flatfive);
        images[5] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_five);
        images[6] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_six);
        images[7] = BitmapFactory.decodeResource(context.getResources(), R.drawable.chord_seven);

        setBackgroundColor(Color.WHITE);

        editCaption = context.getString(R.string.edit_to_draw);
        editCaptionLength = paint.measureText(editCaption);

    }

    public void onDraw(Canvas canvas) {

        if (width == -1) {
            width = getWidth();
            height = getHeight();
            boxHeight = images[1].getHeight();
            boxWidth = images[1].getWidth();
            marginX = width / 64;
            marginY = (height - boxHeight) / 2;
            editCaptionLeft = width / 2 - editCaptionLength / 2;
        }

        int chordInProgression = -1;

        if (mJam != null) {
            chords = mJam.getProgression();
            scale = mJam.getScale();
            chordInProgression = mJam.getChordInProgression();
        }

        if (chords == null)
            return ;

        int lineEnds = -1;

        int numberOfChords = chords.length;
        int startAt = (width - numberOfChords * boxWidth) / 2;
        int at;
        int scalei;
        for (int i = 0; i < chords.length; i++) {

            at = startAt + boxWidth * i;

            scalei = chords[i] % scale.length;
            if (scalei < 0)
                scalei += scale.length;
            drawChord(canvas, scale[scalei], at);

            if (chordInProgression == i) {
                canvas.drawRect(at, marginY,
                        at + boxWidth,
                        marginY + boxHeight,
                        paintCurrentBeat);

            }

            //canvas.drawText(Integer.toString(chords[ibeat]), at, boxHeight * 2, paint);

        }

        if (!editMode) {
            canvas.drawText(editCaption, editCaptionLeft, boxHeight * 2.95f, paint);
            canvas.drawLine(editCaptionLeft - 10, boxHeight * 2.95f - 16f,
                    editCaptionLeft - 10, boxHeight * 2.95f,
                    paint);
            canvas.drawLine(editCaptionLeft + editCaptionLength + 10, boxHeight * 2.95f - 16f,
                    editCaptionLeft + editCaptionLength + 10, boxHeight * 2.95f,
                    paint);
        }
        else {
            drawEditMode(canvas);
        }

    }


    private Bitmap draw_image;


    public void drawChord(Canvas canvas, int chord, int at) {


        draw_image = null;
        if (chord == 0) {
            draw_image = images[0];
        }
        if (chord == 2) {
            draw_image = images[1];
        }
        if (chord == 3 || chord == 4) {
            draw_image = images[2];
        }
        if (chord == 5) {
            draw_image = images[3];
        }
        if (chord == 6) {
            draw_image = images[4];
        }
        if (chord == 7) {
            draw_image = images[5];
        }
        if (chord == 8 || chord == 9) {
            draw_image = images[6];
        }
        if (chord == 10 || chord == 11) {
            draw_image = images[7];
        }

        if (draw_image != null) {
            canvas.drawBitmap(draw_image, at, marginY, null);
        }
        else {
            canvas.drawText(Integer.toString(chord),
                    at, marginY+ 50,
                    paint);
        }

    }


    public void setJam(Main activity, Jam jam) {
        mJam = jam;
        mActivity = activity;
    }

/*    public boolean onTouchEvent(MotionEvent event) {

        postInvalidate();

        if (editMode) {
            onTouchInEditMode(event);
            return true;
        }

        float y = event.getY();
        float x = event.getX();

        if (x > editCaptionLeft - 10 && x < editCaptionLeft + editCaptionLength + 10
                && y > boxHeight * 2.95f - 20 && y < boxHeight * 2.95f + 10) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                pressingEditButton = true;
            }

            if (event.getAction() == MotionEvent.ACTION_UP && pressingEditButton) {
                //mActivity.editMelody(this);
                editMode = true;
                pressingEditButton = false;
            }
        }

        return true;
    }
*/
    private void onTouchInEditMode(MotionEvent event) {


        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {

            isTouching = true;

        }
        if (action == MotionEvent.ACTION_MOVE) {

            if (isTouching) {

                touches.add(new MelodyTouch(event.getX(), event.getY()));

            }


        }
        if (action == MotionEvent.ACTION_UP) {

            isTouching = false;


        }

        invalidate();
    }

    public void finishEditMode() {
        editMode = false;
    }


    class MelodyTouch {

        float x;
        float y;

        MelodyTouch(float x, float y) {
            this.x = x;
            this.y = y;
        }

    }


   private void drawEditMode(Canvas canvas) {

       for (int i = 1; i < touches.size(); i++) {
           canvas.drawLine(touches.get(i -1).x, touches.get(i -1).y,
                           touches.get(i ).x,   touches.get(i).y,
                           drawPaint);
       }

   }

   public void setChords(int[] chords, int[] scale) {
       this.chords = chords;
       this.scale = scale;
       invalidate();
   }

   public int[] getChords() {
       return chords;
   }
}
