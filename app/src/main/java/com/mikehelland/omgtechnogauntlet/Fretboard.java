package com.mikehelland.omgtechnogauntlet;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * User: m
 * Date: 2/20/13
 * Time: 9:23 PM
 */
public class Fretboard {


    int frets;
    int strings;
    int key;
    //String scale;
    Paint paint;

    ArrayList<Touch> touches = new ArrayList<Touch>();

//    AudioDevice audioDevice = null;

    private Channel mChannel;

    boolean isSetup = false;

    float fretHeight;
    float stringWidth;


    private String instrument;


    private boolean isTouching = false;

    private Paint recordingPaint;

    private FretMap fretMap = new FretMap();

    private Jam mJam;

    public Fretboard(Channel channel, Jam jam, String fretboardJson) {

        mChannel = channel;
        mJam = jam;

        paint = new Paint();
        paint.setARGB(255, 255, 255, 255);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(4);
        paint.setShadowLayer(6, 0, 0, 0xFFFFFFFF);

        recordingPaint = new Paint();
        recordingPaint.setARGB(255, 255, 0, 0);
        recordingPaint.setStyle(Paint.Style.FILL);

        key = mJam.getKey();

        if (parseJSONToArray(fretboardJson, mJam.getScaleString())) {
            frets = fretMap.get(0).size();
            strings = fretMap.size();
            isSetup = false;
        }


    }

    private boolean parseJSONToArray(String json, String scale) {

        fretMap.clear();

        try {
            JSONArray catchAllMap = null;
            JSONArray maps = new JSONObject(json).getJSONArray("maps");
            JSONObject map;
            JSONArray columns = null;
            JSONArray rows;
            for (int im = 0; im < maps.length(); im++) {
                map = maps.getJSONObject(im);
                if (!map.has("scale")) {
                    catchAllMap = map.getJSONArray("frets");
                }
                else if (map.get("scale").equals(scale)) {
                    columns = map.getJSONArray("frets");

                    break;
                }
            }

            if (columns == null && catchAllMap != null)
                columns = catchAllMap;

            if (columns == null)
                return false;

            ArrayList<FretMapElement> mapColumn;
            FretMapElement mapElement;

            for (int ic = 0; ic < columns.length(); ic++) {
                mapColumn = new ArrayList<FretMapElement>();
                fretMap.add(mapColumn);

                rows = columns.getJSONArray(ic);
                for (int ir = 0; ir < rows.length(); ir++) {

                    mapElement = new FretMapElement();
                    mapElement.basicNote = rows.getInt(ir);

                    mapColumn.add(mapElement);
                }
            }

        }
        catch (JSONException exp) {
            Log.d("MGH Fretboard bad json", exp.getMessage());
            exp.printStackTrace();
            return false;
        }

        for (ArrayList<FretMapElement> mapColumn : fretMap) {
            for (FretMapElement mapElement : mapColumn) {

                mapElement.scaledNote = mJam.getScaledNoteNumber(mapElement.basicNote);
                mapElement.instrumentNote = mChannel.getInstrumentNoteNumber(mapElement.scaledNote);

                Log.d("MGH mapped basic note", Integer.toString(mapElement.basicNote));
                Log.d("MGH mapped scaled note", Integer.toString(mapElement.scaledNote));
                Log.d("MGH mapped inst note", Integer.toString(mapElement.instrumentNote));
            }
        }


        return true;
    }



    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {

                isTouching = true;

                //TODO channel.cancelPlayback();

                Touch touch = makeTouch(event, -1);
                touch.onFret = (int) (touch.y / fretHeight);
                touch.onString = (int) (touch.x / stringWidth);
                touches.add(touch);
                Log.d("MGH touchfret", Integer.toString(touch.onFret));
                Log.d("MGH touchstring", Integer.toString(touch.onString));

                //touch.channelId = mChannel.startChannel(base + touch.fretMapping(fretMap));

                Note playNote = new Note();

                FretMapElement fret = touch.fretMapping(fretMap);
                playNote.setBasicNote(fret.basicNote);
                playNote.setScaledNote(fret.scaledNote);
                playNote.setInstrumentNote(fret.instrumentNote);


                touch.playingHandle = mChannel.playLiveNote(playNote, true);

                break;
            }

            case MotionEvent.ACTION_UP: {

                if (isTouching) {
                    mChannel.stopWithHandle(touches.get(0).playingHandle);
                    //channel.stopChannel(touches.get(0).channelId);
                    touches.clear();
                    isTouching = false;

                    Note restNote = new Note();
                    restNote.setRest(true);
                    mChannel.playLiveNote(restNote, true);

                }

                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

                Touch touch = makeTouch(event, index);
                touch.onFret = (int) (touch.y / fretHeight);
                touch.onString = (int) (touch.x / stringWidth);

                touches.add(touch);

                //touch.channelId = channel.startChannel(base + touch.fretMapping(fretMap));
                Note playNote = new Note();

                FretMapElement fret = touch.fretMapping(fretMap);
                playNote.setBasicNote(fret.basicNote);
                playNote.setScaledNote(fret.scaledNote);
                playNote.setInstrumentNote(fret.instrumentNote);


                touch.playingHandle = mChannel.playLiveNote(playNote, true);

                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {

                final int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int id = event.getPointerId(index);
                for (Touch touch : touches) {
                    if (id == touch.id) {
                        touches.remove(touch);

                        mChannel.stopWithHandle(touch.playingHandle);

                        break;
                    }
                }

                //I don't think this would run, since it would action_up, but hey
                if (touches.size() == 0) {
                    Note restNote = new Note();
                    restNote.setRest(true);
                    mChannel.playLiveNote(restNote, true);
                }


                break;
            }

            case MotionEvent.ACTION_MOVE: {

                if (!isTouching)
                    break;



                  int id;
                int lastFret;
                int lastString;
                for (int ip = 0; ip < event.getPointerCount(); ip++) {
                    id = event.getPointerId(ip);
                    for (Touch touch : touches) {
                        if (id == touch.id) {

                            lastFret = touch.onFret;
                            lastString = touch.onString;

                            touch.x = event.getX(ip);
                            touch.y = event.getY(ip);
                            touch.onFret = (int) (touch.y / fretHeight);
                            touch.onString = (int) (touch.x / stringWidth);

                            if (lastFret != touch.onFret || lastString != touch.onString) {

                                Note playNote = new Note();

                                FretMapElement fret = touch.fretMapping(fretMap);
                                playNote.setBasicNote(fret.basicNote);
                                playNote.setScaledNote(fret.scaledNote);
                                playNote.setInstrumentNote(fret.instrumentNote);

                                mChannel.stopWithHandle(touch.playingHandle);
                                touch.playingHandle = mChannel.playLiveNote(playNote, true);

                                //channel.setChannel(touch.channelId,
                                //        base + touch.fretMapping(fretMap));

                            }

                            break;
                        }
                    }
                }
                break;
            }
        }

        return true;
    }


    protected void onDraw(Canvas canvas, final float width, final float height) {

        if (mChannel == null)
            return;

        if (!isSetup) {
            fretHeight = height / frets;
            stringWidth = width / strings;

            isSetup = true;
        }


        float fstrings = (float) strings;
        float ffrets = (float) frets;
        for (int i = 1; i < strings; i++) {
            canvas.drawLine(i / fstrings * width, 0,
                    i / fstrings * width, height, paint);
        }

        for (int i = 1; i < frets; i++) {
            canvas.drawLine(0, i / ffrets * height,
                    width, i / ffrets * height, paint);
        }

        for (Touch touch : touches) {
            canvas.drawRect(stringWidth * touch.onString, fretHeight * touch.onFret,
                    stringWidth * (touch.onString + 1), fretHeight * (touch.onFret + 1), paint);
        }

        canvas.drawCircle(width * 0.25f, height * 0.1f, 20, paint);
        canvas.drawCircle(width * 0.75f, height * 0.9f, 20, paint);

    }



    Touch makeTouch(MotionEvent event, int index) {
        Touch t;
        if (index == -1) {
            t = new Touch(event.getX(), event.getY(), event.getPointerId(0));
        } else {
            t = new Touch(event.getX(index), event.getY(index), event.getPointerId(index));
        }
        return t;
    }

    void makeFretMap() {

        /*
        if (instrument.equals("HHDRUMS")) {
            fretMap = new int[][]{
                    new int[]{0, 1, 2, 3}
            };
        } else if (instrument.equals("AGUITAR CHORDS")) {
            fretMap = new int[][]{
                    new int[]{4, 0, 5},
                    new int[]{1, 2, 3}
            };
        } else if (instrument.equals("EBASS") ||
                instrument.equals("EGUITAR CHORDS")) {
            fretMap = new int[][]{
                    new int[]{8, 9, 10, 11, 12},
                    new int[]{0, 3, 5, 6, 7}
            };

        } else if (instrument.equals("SYNTH") || instrument.equals("EGUITAR")) {

            scale = PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getString("quantizer", "0,2,4,5,7,9,11");

            if (scale.equals("0,2,4,5,7,9,11")) {
                fretMap = new int[][]{
                        new int[]{-1, 0, 5, 7, 9},
                        new int[]{4, 9, 11, 12, 14}
                };
            } else if (scale.equals("0,3,5,6,7,10")) {
                fretMap = new int[][]{
                        new int[]{-2, 0, 3, 5, 6},
                        new int[]{5, 7, 10, 12, 15}
                };
            } else {
                fretMap = new int[][]{
                        new int[]{-1, 0, 5, 7, 9},
                        new int[]{4, 9, 11, 12, 14}
                };
            }
        }

        if (fretMap == null) {
            fretMap = new int[][]{
                    new int[]{0}
            };
        }
    */
    }


}

