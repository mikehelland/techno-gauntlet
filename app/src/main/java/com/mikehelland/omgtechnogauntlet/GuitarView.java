package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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
public class GuitarView extends View {

    private Paint paint;
    private Paint paintOff;
    private Paint paintRed;
    private Paint paintGreen;
    private Paint paintYellow;

    private int width = -1;
    private int height = -1;

    private int boxWidth;
    private int boxHeight;
    private int boxHeightHalf;

    private Jam mJam;
    private Channel mChannel;

    private Paint topPanelPaint;
    private Paint paintText;

    private Paint paintBeat;

    private int touchingString = -1;
    private int touchingFret = -1;

    private Note restNote;

    private int lastFret = -1;

    private int key;
    private int[] scale;

    private int frets = 0;
    private int strings = 4;

    private int[] fretMapping;
    private int[] noteMapping;

    private String[] keyCaptions = {"C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B"};
    private String[] soundsetCaptions;

    private boolean useScale = true;

    private Note draw_note;
    private float draw_x;
    private Bitmap draw_noteImage;
    private int draw_boxwidth;

    private Bitmap images[][];

    private Paint paintCurrentBeat;


    private int lowNote;

    private boolean modified = false;

    private int rootFret = 0;

    private float draw_leftOffset = 20;
    private float draw_debugBeatWidth;
    private float draw_beatWidth;

    private Fretboard mFretboard = null;

    public GuitarView(Context context, AttributeSet attrs) {
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

        restNote = new Note();
        restNote.setRest(true);


        images = new Bitmap[8][2];
        images[0][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_half);
        images[0][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_rest_half);
        images[1][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_dotted_quarter);
        images[1][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_rest_dotted_quarter);
        images[2][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_quarter);
        images[2][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_rest_quarter);
        images[3][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_dotted_eighth);
        images[3][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_rest_dotted_eighth);
        images[4][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_eighth);
        images[4][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_rest_eighth);
        images[5][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_dotted_sixteenth);
        images[5][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_rest_dotted_sixteenth);
        images[6][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_sixteenth);
        images[6][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_rest_sixteenth);
        images[7][0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_thirtysecond);
        images[7][1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.w_note_rest_thirtysecond);

        paintCurrentBeat = new Paint();
        paintCurrentBeat.setARGB(128, 255, 0, 0);
        paintCurrentBeat.setShadowLayer(4, 0, 0, 0xFFFFFFFF);
        paintCurrentBeat.setStyle(Paint.Style.FILL_AND_STROKE);

    }

    public void onDraw(Canvas canvas) {

        if (mJam.isPlaying()) {
            float beatBoxWidth = getWidth() / mJam.getBeats();
            float beatBoxStart = (float)Math.floor(mJam.getCurrentSubbeat() / mJam.getSubbeats()) * beatBoxWidth;

            canvas.drawRect(beatBoxStart, 0, beatBoxStart + beatBoxWidth, getHeight(), paintYellow);
        }

        //if (height != getHeight()) {
        draw_debugBeatWidth = getWidth() - draw_leftOffset;
        draw_beatWidth = draw_debugBeatWidth / (float)mJam.getTotalSubbeats();
        width = getWidth();
        height = getHeight();
        boxHeight = height / frets;
        boxHeightHalf = boxHeight / 2;
        boxWidth = width / strings;
        //}

        if (mFretboard != null) {
            mFretboard.onDraw(canvas, width, height);

            drawNotes(canvas, mChannel.getNotes());
            return;
        }

        int noteNumber;

        for (int fret = 1; fret <= frets; fret++) {

            noteNumber = fretMapping[fret - 1];

            if (noteNumber % 12 == key) {
                canvas.drawRect(width / 4, height - fret * boxHeight,
                        width / 4 * 3, height - (fret - 1) * boxHeight, paintOff);
            }

            canvas.drawLine(0, height - fret * boxHeight, width,
                    height - fret * boxHeight, paint);

            canvas.drawText(useScale ? keyCaptions[noteNumber % 12] : soundsetCaptions[noteNumber],
                    0, height - (fret - 1) * boxHeight - boxHeightHalf, paint);

        }

        canvas.drawLine(0, height - 1, width,
                height - 1, paint);

        if (touchingFret > -1 ) {
            canvas.drawRect(0, height - (touchingFret + 1) * boxHeight,
                    width, height - touchingFret  * boxHeight,
                    topPanelPaint);
        }

        drawColumns(canvas);
        drawNotes(canvas, mChannel.getNotes());

        if (mChannel.debugTouchData.size() > 0) {
            for (int isubbeat = 0; isubbeat <= mJam.getTotalSubbeats(); isubbeat++) {
                canvas.drawLine(draw_leftOffset + isubbeat * draw_beatWidth, height - 50,
                        draw_leftOffset + isubbeat * draw_beatWidth, height, paint);
            }

            DebugTouch debugTouch;
            for (int idebug = 0; idebug < mChannel.debugTouchData.size(); idebug++) {
                debugTouch = mChannel.debugTouchData.get(idebug);

                canvas.drawCircle(draw_leftOffset + draw_debugBeatWidth *
                                (float)debugTouch.dbeat / 8.0f, height - 40, 5,
                        debugTouch.mode.equals("START") ? paintGreen : paintRed);

                canvas.drawCircle(draw_leftOffset + draw_debugBeatWidth *
                                (float)debugTouch.isubbeatgiven / 32, height - 20, 5,
                        debugTouch.mode.equals("START") ? paintGreen : paintRed);

            }
        }
    }



    public boolean onTouchEvent(MotionEvent event) {

        if (mFretboard != null) {
            boolean result = mFretboard.onTouchEvent(event);
            invalidate();
            return result;
        }


        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {

            if (mChannel.isEnabled() && !modified) {
                modified = true;

                mChannel.clearNotes();
            }

            touchingString = getTouchingString(event.getX());

            touchingFret = (int)Math.floor(event.getY() / boxHeight);
            touchingFret = Math.max(0, Math.min(fretMapping.length - 1, touchingFret));

            touchingFret = fretMapping.length - touchingFret - 1;

            lastFret = touchingFret;
            Note note = new Note();
            note.setBasicNote(touchingFret - rootFret);
            note.setScaledNote(fretMapping[touchingFret]);
            note.setInstrumentNote(fretMapping[touchingFret] - lowNote);
            mChannel.playLiveNote(note);
            mChannel.setArpeggiator(touchingString);

        }

        if (action == MotionEvent.ACTION_MOVE) {
            if (lastFret > -1) {
                touchingString = getTouchingString(event.getX());

                touchingFret = (int)Math.floor(event.getY() / boxHeight);
                touchingFret = Math.max(0, Math.min(fretMapping.length - 1, touchingFret));

                touchingFret = fretMapping.length - touchingFret - 1;

                if (touchingFret != lastFret) {

                    lastFret = touchingFret;
                    Note note = new Note();
                    note.setBasicNote(touchingFret - rootFret);
                    note.setScaledNote(fretMapping[touchingFret]);
                    note.setInstrumentNote(fretMapping[touchingFret] - lowNote);
                    if (touchingString == 0)
                        mChannel.playLiveNote(note);
                    else
                        mChannel.updateLiveNote(note);
                }
                mChannel.setArpeggiator(touchingString);

            }

        }

        if (action == MotionEvent.ACTION_UP) {
            mChannel.playLiveNote(restNote);
            touchingString = -1;
            touchingFret = -1;
            lastFret = -1;

        }

        invalidate();
        return true;
    }

    public void setJam(Jam jam, Channel channel, Fretboard fretboard) {
        mJam = jam;
        mChannel = channel;

        mFretboard = fretboard;

        setScaleInfo();

        mJam.addInvalidateOnBeatListener(this);

    }

    public void setScaleInfo() {

        mChannel.debugTouchData.clear();

        int rootNote;

        key = mJam.getKey();
        scale = mJam.getScale();
        frets = 0;

        useScale = mChannel.isAScale();

        int highNote;
        if (!useScale) {
            key = 0;
            rootNote = 0;
            lowNote = 0;
            highNote = mChannel.getSoundSet().getSounds().size() - 1;

            soundsetCaptions = mChannel.getSoundSet().getSoundNames();

        }
        else {
            rootNote = key + mChannel.getOctave() * 12;
            Log.d("MGH guitarview rootnote", Integer.toString(mChannel.getOctave()));
            lowNote = mChannel.getLowNote();
            highNote = mChannel.getHighNote();
        }

        int[] allFrets = new int[highNote - lowNote + 1];
        noteMapping = new int[highNote - lowNote + 1];

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
                if (i == rootNote) {
                    rootFret = frets;
                }

                noteMapping[i - lowNote] = frets;
                allFrets[frets++] = i;

            }

        }

        fretMapping = new int[frets];
        System.arraycopy(allFrets, 0, fretMapping, 0, frets);


    }

    public void drawNotes(Canvas canvas, ArrayList<Note> list) {

        //float middle = getHeight() / 2.0f;
        float draw_y;
        draw_boxwidth = Math.min(images[0][0].getWidth(), getWidth() / (list.size() + 1));

        double beatsUsed = 0.0d;

        for (int j = 0; j < list.size(); j++) {

            draw_note = list.get(j);

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

            draw_y = getHeight() / 2;
            if (!draw_note.isRest()) {
                draw_y = (frets - 1 - noteMapping[draw_note.getInstrumentNote()]) * boxHeight;
            }

            draw_x = draw_beatWidth * (float)beatsUsed * 4.0f;

            if (draw_note.isPlaying()) {
                canvas.drawRect(draw_x, draw_y,
                        draw_x + draw_boxwidth,
                        draw_y + boxHeight,
                        paintCurrentBeat);
            }

            if (draw_noteImage != null) {
                canvas.drawBitmap(draw_noteImage, draw_x , draw_y, null);
            }
            else {
                canvas.drawText(Double.toString(draw_note.getBeats()),
                        draw_x, draw_y + 50,
                        paint);
            }

            beatsUsed += draw_note.getBeats();
        }

    }

    void drawColumns(Canvas canvas) {
        for (int i = 1; i < strings; i++) {
            canvas.drawLine(i * boxWidth, 0, i * boxWidth, height, paintYellow);
        }
    }

    int getTouchingString(float x) {
        int touchingString = (int)Math.floor(x / boxWidth);
        if (touchingString == 3) {
            return 1;
        }
        if (touchingString == 1) {
            return 4;
        }

        return touchingString;
    }
}
