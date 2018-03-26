package com.mikehelland.omgtechnogauntlet.jam;

import android.view.View;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class Player {

    private Section section;

    int ibeat;
    long timeSinceLast;
    long lastBeatPlayed;

    int subbeatLength;
    int totalSubbeats;

    private SoundManager soundManager;

    private PlaybackThread playbackThread;

    private boolean cancelPlaybackThread = false;

    private boolean playing = false;
    private boolean mIsPaused = true;

    private int progressionI = -1;

    private List<View> viewsToInvalidateOnBeat = new CopyOnWriteArrayList<>();
    private List<View> viewsToInvalidateOnNewMeasure = new CopyOnWriteArrayList<>();

    private int currentChord = 0;

    private long mSyncTime = 0L;

    Player(Section section) {
        this.section = section;
    }

    private void playBeatSampler(int subbeat) {

        for (Part part : section.parts) {
            if (subbeat == 0) {
                //todo part.resetI();
            }
            //todo part.getSoundsToPlayForBeat(subbeat);
        }
    }

    void kickIt() {

        if (!playing) {
            cancelPlaybackThread = false;
            playbackThread = new PlaybackThread();
            playbackThread.start();
        }
        mIsPaused = false;
    }



    int getCurrentSubbeat() {
        return ibeat;
        //todo there's probably a good reason for this code below
        //todo but it made the beat counter not right (because I added measures)
        //todo so.... why was it here? mistake? or part of the recording features?
        //int i = playbackThread.ibeat;
        //if (i == 0) i = beats * subbeats;
        //return i - 1;

    }

    /*int getClosestSubbeat(DebugTouch debugTouch) {
        if (playbackThread == null)
            return 0;

        int i = ibeat;

        debugTouch.iclosestsubbeat = i;
        debugTouch.dbeat = (i + playbackThread.timeSinceLast / (double)subbeatLength) / subbeats;

        // don't use 16th notes
        //if (i % 2 > 0)
        //    i = (i - 1) % (totalsubbeats);

        //if (i < 0) i = totalsubbeats - 1;

        if (playbackThread.timeSinceLast > subbeatLength / 2) {
            i = i + 1;
            if (i == getTotalSubbeats())
                i = 0;

            //if (i == -1) i = beats * subbeats - 1;
        }

        debugTouch.isubbeatgiven = i;


        return i;

    }*/

    private void onNewLoop() {
        progressionI++;

        if (progressionI >= section.progression.length || progressionI < 0) {
            progressionI = 0;
        }
        int chord = section.progression[progressionI];

        updateChord(chord);

    }

    private void updateChord(int chord) {
        for (Part part : section.parts) {
            if (part.soundSet.isChromatic()) {
                //todo mm.applyScale(part, chord);
            }
        }
    }

    void pause() {
        mIsPaused = true;
        cancelPlaybackThread = true;

        for (Part part : section.parts) {
            //todo part.mute();
        }
    }

    void finish() {
        mIsPaused = true;
        for (Part part : section.parts) {
            //todo part.finish();
        }
    }

    boolean isPlaying() {
        return playing;
    }


    int getChordInProgression() {
        return progressionI;
    }

    void addInvalidateOnBeatListener(View view) {
        viewsToInvalidateOnBeat.add(view);
    }

    void addInvalidateOnNewMeasureListener(View view) {
        viewsToInvalidateOnNewMeasure.add(view);
    }

    void removeInvalidateOnBeatListener(View view)  {
        viewsToInvalidateOnBeat.remove(view);
    }

    boolean isPaused() {return mIsPaused;}


    private class PlaybackThread extends Thread {

        public void run() {

            startTheThing();
            while (!cancelPlaybackThread) {
                if (isTime()) {
                    doTheThing();
                }
            }
        }
    }

    private void startTheThing() {
        playing = true;
        progressionI = -1; // gets incremented by onNewLoop
        onNewLoop();

        ibeat = 0;
        lastBeatPlayed = System.currentTimeMillis() - section.beatParameters.subbeatLength;
    }

    private boolean isTime() {

        long timeUntilNext;
        long now;

        now = System.currentTimeMillis();
        timeUntilNext = lastBeatPlayed + section.beatParameters.subbeatLength;
        if (ibeat % section.beatParameters.subbeats != 0 && section.beatParameters.shuffle > 0) {
            timeUntilNext += (int) (section.beatParameters.subbeatLength * section.beatParameters.shuffle);
        }

        if (now >= timeUntilNext) {
            return true;
        }

        //todo better spot for this?
        pollFinishedNotes(now);

        if (timeUntilNext - now > 50) {
            try {
                Thread.sleep(subbeatLength - 50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void doTheThing() {
        totalSubbeats = BeatParameters.getTotalSubbeats(section.beatParameters);
        if (ibeat < totalSubbeats) {
            lastBeatPlayed += subbeatLength;
            playBeatSampler(ibeat);
        }

        ibeat++;

        if (mSyncTime > 0) {
            ibeat = 1;
            lastBeatPlayed = mSyncTime + subbeatLength;
            mSyncTime = 0;
        }
        if (ibeat >= totalSubbeats) {
            ibeat = 0;
            onNewLoop();

            for (View iv : viewsToInvalidateOnNewMeasure) {
                iv.postInvalidate();
            }
        }

        for (View iv : viewsToInvalidateOnBeat) {
            iv.postInvalidate();
        }
    }

    void pollFinishedNotes(long now) {
        long finishAt;
        try {
            for (Part part : section.parts) {
                //todo
                /*finishAt = part.getFinishAt();
                if (finishAt > 0 && now >= finishAt) {
                    part.mute();
                    part.finishCurrentNoteAt(0);
                }*/
            }
        }
        catch  (Exception ignore) {}
    }

}
