package com.mikehelland.omgtechnogauntlet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.mikehelland.omgtechnogauntlet.jam.Jam;
import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.Note;
import com.mikehelland.omgtechnogauntlet.jam.NoteList;

import java.util.ArrayList;

/**
 * User: m
 * Date: 11/15/13
 * Time: 11:01 PM
 */
@SuppressWarnings("FieldCanBeLocal")
public class VerticalView extends View {

    private Paint paint;
    private Paint paintOff;
    private Paint paintRed;
    private Paint paintGreen;
    private Paint paintYellow;

    private int width = -1;
    private int height = -1;

    private float boxWidth;
    private float boxHeight;
    private float boxHeightHalf;

    private Jam mJam;
    private JamPart mPart;

    private Paint topPanelPaint;
    private Paint paintText;

    private Paint paintBeat;

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
    private float draw_boxwidth;

    private Bitmap images[][];

    private Paint paintCurrentBeat;
    private Paint paintCurrentBeatRest;

    private int lowNote;

    private int rootFret = 0;

    private float draw_leftOffset = 20;
    private float draw_debugBeatWidth;
    private float draw_beatWidth;

    private float zoomboxHeight = -1;
    private float zoomTop = -1;
    private float zoomBottom = -1;
    private boolean zooming = false;
    private int zoomingSkipBottom = 0;
    private int zoomingSkipTop = 0;
    private int showingFrets = 0;
    private int skipBottom = 0;
    private int skipTop = 0;

    private boolean mZoomMode = false;

    private ArrayList<Touch> touches = new ArrayList<>();

    private OnGestureListener onGestureListener;

    private int touchingColumn = -1;

    public VerticalView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setARGB(255, 255, 255, 255);
        paint.setShadowLayer(10, 0, 0, 0xFFFFFFFF);

        paintText = new Paint();
        paintText.setARGB(255, 255, 255, 255);
        paintText.setTextSize(24);

        paintBeat = new Paint();
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
        topPanelPaint.setARGB(96, 192, 192, 255);

        setBackgroundColor(Color.BLACK);

        images = ((Main) context).getImages().getNoteImages();

        paintCurrentBeat = new Paint();
        paintCurrentBeat.setARGB(128, 0, 255, 0);
        paintCurrentBeat.setShadowLayer(4, 0, 0, 0xFFFFFFFF);
        paintCurrentBeat.setStyle(Paint.Style.FILL_AND_STROKE);
        paintCurrentBeatRest = new Paint();
        paintCurrentBeatRest.setARGB(128, 255, 0, 0);
        paintCurrentBeatRest.setShadowLayer(4, 0, 0, 0xFFFFFFFF);
        paintCurrentBeatRest.setStyle(Paint.Style.FILL_AND_STROKE);

    }

    public void setJam(Jam jam, JamPart channel, OnGestureListener onGestureListener) {
        mJam = jam;
        mPart = channel;
        this.onGestureListener = onGestureListener;

        setScaleInfo();

        int[] skipBottomAndTop = mPart.getSurface().getSkipBottomAndTop();
        skipBottom = skipBottomAndTop[0];
        skipTop = skipBottomAndTop[1];
        showingFrets = Math.max(1, frets - skipTop - skipBottom);

    }

    public void setScaleInfo() {

        int rootNote;

        key = mJam.getKey();
        scale = mJam.getScale();
        frets = 0;

        useScale = mPart.getSoundSet().isChromatic();

        int highNote;
        if (!useScale) {
            key = 0;
            rootNote = 0;
            lowNote = 0;
            highNote = mPart.getSoundSet().getSounds().size() - 1;

            soundsetCaptions = mPart.getSoundSet().getSoundNames();

        } else {
            lowNote = mPart.getSoundSet().getLowNote();
            highNote = mPart.getSoundSet().getHighNote();
            rootNote = key + mPart.getOctave() * 12;
            while (rootNote < lowNote) {
                rootNote += 12;
            }
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

        showingFrets = frets;
    }


    public void onDraw(Canvas canvas) {

        if (frets == 0 || mJam == null || mPart == null || fretMapping == null || fretMapping.length == 0) {
            return;
        }

        if (mJam.isPlaying()) {
            float beatBoxWidth = ((float) getWidth()) / mJam.getTotalBeats();
            float beatBoxStart = mJam.getCurrentSubbeat() / mJam.getSubbeats() * beatBoxWidth;

            canvas.drawRect(beatBoxStart, 0, beatBoxStart + beatBoxWidth, getHeight(), paintYellow);
        }

        //if (height != getHeight()) {
        width = getWidth();
        height = getHeight();
        draw_debugBeatWidth = width; //getWidth() - draw_leftOffset;
        draw_beatWidth = draw_debugBeatWidth / (float) (mJam.getSubbeats() * mJam.getTotalBeats());
        boxHeight = (float) height / showingFrets;
        boxHeightHalf = boxHeight / 2;
        boxWidth = (float)width / strings;
        paint.setTextSize(boxHeightHalf);
        //}

        for (Touch touch : touches) {
            canvas.drawRect(0, height - (touch.onFret - skipBottom + 1) * boxHeight,
                    width, height - (touch.onFret - skipBottom) * boxHeight,
                    topPanelPaint);
        }

        if (touchingColumn > -1) {
            canvas.drawRect(touchingColumn * boxWidth, 0,
                    (touchingColumn + 1) * boxWidth, height,
                    topPanelPaint);
        }

        int noteNumber;
        int index;
        for (int fret = 1; fret <= showingFrets; fret++) {
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

        drawColumns(canvas);
        drawNotes(canvas, mPart.getNotes());

    }

    public void drawNotes(Canvas canvas, NoteList list) {

        //float middle = getHeight() / 2.0f;
        float draw_y;
        draw_boxwidth = Math.min(images[0][0].getWidth(), (float)getWidth() / (list.size() + 1));

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
                if (draw_note.getInstrumentNote() >= 0 && draw_note.getInstrumentNote() < noteMapping.length) {
                    draw_y = (frets - 1 - noteMapping[draw_note.getInstrumentNote()]) * boxHeight;
                }
            }

            draw_x = draw_beatWidth * (float) beatsUsed * 4.0f;

            if (draw_note.isPlaying()) {
                canvas.drawRect(draw_x, draw_y,
                        draw_x + draw_boxwidth,
                        draw_y + boxHeight,
                        draw_note.isRest() ? paintCurrentBeatRest : paintCurrentBeat);
            }

            if (draw_noteImage != null) {
                canvas.drawBitmap(draw_noteImage, draw_x,
                        draw_y + boxHeight - draw_noteImage.getHeight(), null);
            } else {
                canvas.drawText(Double.toString(draw_note.getBeats()),
                        draw_x, draw_y + boxHeightHalf,
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mJam == null || mPart == null || fretMapping == null || fretMapping.length == 0) {
            return true;
        }

        if (mZoomMode) {
            onMultiTouchEventForZoom(event);
        } else {
            onMultiTouchEvent(event);
        }

        invalidate();
        return true;
    }

    private int getTouchingString(float x) {
        return (int) Math.floor(x / boxWidth);
    }

    private int getTouchingFret(float y) {
        int fret = (int) Math.floor(y / boxHeight);
        fret = Math.max(0, Math.min(showingFrets - 1, fret));
        fret = showingFrets - fret - 1;
        fret = fret + skipBottom;
        return fret;
    }

    private void onMultiTouchEventForZoom(MotionEvent event) {

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

                if (onGestureListener != null) {
                    onGestureListener.onEnd();
                }

                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (zooming)
                    zoom(event);
                break;
            }
        }
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

        zoomingSkipTop = Math.max(skipTop * -1, (int) Math.floor(topDiff / zoomboxHeight));
        zoomingSkipBottom = Math.max(skipBottom * -1, (int) Math.floor(bottomDiff / zoomboxHeight));

        showingFrets = Math.max(1, frets - skipTop - skipBottom - zoomingSkipBottom - zoomingSkipTop);

        invalidate();
    }

    void setZoomModeOn() {
        mZoomMode = true;
    }

    private void onMultiTouchEvent(MotionEvent event) {

        int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                onDown(event);
                break;
            }
            case MotionEvent.ACTION_UP: {
                onUp(event);
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                onPointerDown(event, index);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                onPointerUp(event, index);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                onMove(event);
                break;
            }
        }
    }

    private Touch makeTouch(MotionEvent event, int index) {

        Touch t = new Touch(event.getX(index), event.getY(index), event.getPointerId(index));
        t.onString = getTouchingString(event.getX(index));
        t.onFret = getTouchingFret(event.getY(index));
        makeNote(t);

        return t;
    }

    private void onDown(MotionEvent event) {
        Touch touch = makeTouch(event, 0);
        touches.add(touch);

        if (onGestureListener != null)
            onGestureListener.onStart(touch.note, touch.onString);
    }

    private void onUp(MotionEvent event) {
        touches.clear();
        touchingColumn = -1;

        if (onGestureListener != null)
            onGestureListener.onEnd();
    }

    private void onPointerDown(MotionEvent event, int index) {
        Touch touch = makeTouch(event, index);
        touches.add(touch);

        if (onGestureListener != null)
            onGestureListener.onUpdate(getNoteArrayFromTouches(), touch.onString);
    }

    private void onPointerUp(MotionEvent event, int index) {

        final int id = event.getPointerId(index);
        for (final Touch touch : touches) {
            if (id == touch.id) {
                touches.remove(touch);

                if (onGestureListener != null)
                    onGestureListener.onRemove(touch.note, getNoteArrayFromTouches());

                break;
            }
        }
    }

    private void onMove(MotionEvent event) {
        boolean update = false;
        int id;
        touchingColumn = 0;
        for (int ip = 0; ip < event.getPointerCount(); ip++) {
            id = event.getPointerId(ip);
            for (Touch touch : touches) {
                if (id == touch.id) {


                    touch.lastFret = touch.onFret;
                    touch.lastString = touch.onString;

                    touch.x = event.getX(ip);
                    touch.y = event.getY(ip);
                    touch.onFret = getTouchingFret(touch.y);
                    touch.onString = getTouchingString(touch.x);

                    if (touchingColumn < touch.onString)
                        touchingColumn = touch.onString;

                    if (touch.lastFret != touch.onFret || touch.lastString != touch.onString) {
                        onGestureListener.onRemove(touch.note, getNoteArrayFromTouches());
                        makeNote(touch);
                        update = true;
                    }
                    break;
                }
            }
        }
        if (update) {
            onGestureListener.onUpdate(getNoteArrayFromTouches(), touchingColumn);
        }
    }

    private void makeNote(Touch touch) {

        touch.note = new Note(false,
                touch.onFret -rootFret,
                fretMapping[touch.onFret],
                fretMapping[touch.onFret]-lowNote,
                -1
        );

        //todo auto touch, auto beat needs a more accurate name I think
        // mPart.setArpeggiator(touch.onString);
    }

    private Note[] getNoteArrayFromTouches() {
        Note[] notes = new Note[touches.size()];
        for (int i = 0; i < notes.length; i++) {
            notes[i] = touches.get(i).note;
        }
        return notes;
    }

    int getSkipTop() {
        return skipTop;
    }

    int getSkipBottom() {
        return skipBottom;
    }

    abstract static class OnGestureListener {
        abstract void onStart(Note note, int autoBeat);
        abstract void onUpdate(Note[] notes, int autoBeat);
        abstract void onRemove(Note note, Note[] notes);
        abstract void onEnd();
    }
}