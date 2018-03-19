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
public class MelodyView extends View {

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

    private NoteList mList;

    private Bitmap images[][];

    private String editCaption;
    private float editCaptionLength = 0f;
    private float editCaptionLeft = 0f;

    private boolean editMode = false;

    private boolean pressingEditButton = false;

    private boolean isTouching = false;

    private ArrayList<MelodyTouch> touches = new ArrayList<MelodyTouch>();

    private Paint drawPaint;

    public MelodyView(Context context, AttributeSet attrs) {
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


        images = new Bitmap[8][2];
        images[0][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.note_half);
        images[0][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.note_rest_half);
        images[1][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.note_dotted_quarter);
        images[1][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.note_rest_dotted_quarter);
        images[2][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.note_quarter);
        images[2][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.note_rest_quarter);
        images[3][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.note_dotted_eighth);
        images[3][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.note_rest_dotted_eighth);
        images[4][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.note_eighth);
        images[4][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.note_rest_eighth);
        images[5][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.note_dotted_sixteenth);
        images[5][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.note_rest_dotted_sixteenth);
        images[6][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.note_sixteenth);
        images[6][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.note_rest_sixteenth);
        images[7][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.note_thirtysecond);
        images[7][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.note_rest_thirtysecond);

        setBackgroundColor(Color.WHITE);

        editCaption = context.getString(R.string.edit_to_draw);
        editCaptionLength = paint.measureText(editCaption);

    }

    public void onDraw(Canvas canvas) {

        if (width == -1) {
            width = getWidth();
            height = getHeight();
            boxHeight = images[1][0].getHeight();
            boxWidth = images[1][0].getWidth();
            marginX = width / 64;
            marginY = height / 128;
            editCaptionLeft = width / 2 - editCaptionLength / 2;
        }

        if (mList == null)
            return ;

        double beatsUsed = 0.0d;

        int spaceUsed = 0;
        int spaceUsedAtLineEnd = 0;
        int extraSpace = 0;

        Note note;

        NoteList list = mList;

        int lineStarts = 0;
        int lineEnds = -1;

        int i = 0;

        draw_lastDrawnX = 0;
        draw_y = 0;
        draw_lastDrawnI = 0;

        boolean hitEndOfAMeasure = false;

        while (draw_lastDrawnI < list.size() - 1) {

            if (beatsUsed >= 4.0d) {

                lineEnds = i - 1;
                spaceUsedAtLineEnd = spaceUsed ;
                hitEndOfAMeasure = true;
                beatsUsed = 0;

            }


            spaceUsed += boxWidth;

            if (hitEndOfAMeasure && spaceUsed >= width) {

                drawNotes(canvas, list, lineStarts, lineEnds, spaceUsedAtLineEnd);

                i = lineEnds + 1;
                lineStarts = i;

                draw_y += boxHeight + marginY;

//                canvas.drawLine(0, draw_y, width, draw_y, paint);

                spaceUsed = 0;
                beatsUsed = 0;

                hitEndOfAMeasure = false;

                continue;

            }

            note = list.get(i);
            beatsUsed += note.getBeats();
            i++;



            if (i >= list.size()) {
                drawNotes(canvas, list, lineStarts, list.size() - 1, spaceUsed);
                break;
            }


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


    private int draw_lastDrawnX = 0;
    private Note draw_note;
    private float draw_x;
    private float draw_y;
    private Bitmap draw_noteImage;
    private int draw_lastDrawnI;
    private int draw_boxwidth;


    public void drawNotes(Canvas canvas, NoteList list, int startI, int finishI, int spaceUsed) {

        if (spaceUsed > width) {
            draw_boxwidth = (int)(boxWidth * 0.75d);
            draw_lastDrawnX = (width - (int)(spaceUsed * 0.75d)) / 2;
        }
        else {
            draw_boxwidth = boxWidth;
            draw_lastDrawnX = (width - spaceUsed) / 2;
        }

        double beatPostion = 0.0d;

        for (int j = startI; j <= finishI; j++) {

            draw_note = list.get(j);

            if (beatPostion % 4 == 0)
                canvas.drawLine(draw_lastDrawnX, draw_y,
                        draw_lastDrawnX, draw_y + boxHeight, paint);

            draw_x = (float)(draw_lastDrawnX);
            draw_lastDrawnX += draw_boxwidth;

            if (draw_note.isPlaying()) {
                canvas.drawRect(draw_x, draw_y,
                        draw_lastDrawnX,
                        draw_y + boxHeight,
                        paintCurrentBeat);
            }

            draw_noteImage = null;
            if (draw_note.getBeats() == 2.0d) {
                draw_noteImage = images[0][draw_note.isRest() ? 1 : 0];
            }
            if (draw_note.getBeats() == 1.5d) {
                draw_noteImage = images[1][draw_note.isRest() ? 1 : 0];
            }
            if (draw_note.getBeats() == 1.0d) {
                draw_noteImage = images[2][draw_note.isRest() ? 1 : 0];
            }
            if (draw_note.getBeats() == 0.75d) {
                draw_noteImage = images[3][draw_note.isRest() ? 1 : 0];
            }
            if (draw_note.getBeats() == 0.5d) {
                draw_noteImage = images[4][draw_note.isRest() ? 1 : 0];
            }
            if (draw_note.getBeats() == 0.375d) {
                draw_noteImage = images[5][draw_note.isRest() ? 1 : 0];
            }
            if (draw_note.getBeats() == 0.25d) {
                draw_noteImage = images[6][draw_note.isRest() ? 1 : 0];
            }
            if (draw_note.getBeats() == 0.125d) {
                draw_noteImage = images[7][draw_note.isRest() ? 1 : 0];
            }

            if (draw_noteImage != null) {
                canvas.drawBitmap(draw_noteImage, draw_x , draw_y, null);
            }
            else {
                canvas.drawText(Double.toString(draw_note.getBeats()),
                        draw_x, draw_y + 50,
                        paint);
            }

            beatPostion += draw_note.getBeats();

        }

        canvas.drawLine(draw_lastDrawnX, draw_y,
                draw_lastDrawnX, draw_y + boxHeight, paint);

        draw_lastDrawnI = finishI;

        //lastDrawnX = lastDrawnX + extraSpace;

    }

    public void setMelody(NoteList notes) {
        mList = notes;
        invalidate();
    }

    public void setJam(Main activity, Jam jam) {
        mJam = jam;
        mActivity = activity;
    }

    public boolean onTouchEvent(MotionEvent event) {


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
}
