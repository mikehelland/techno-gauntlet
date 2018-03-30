package com.mikehelland.omgtechnogauntlet.jam;

import android.util.Log;

import java.util.ArrayList;

class Player {

    final static int STATE_STOPPED = 0;
    final static int STATE_STARTING = 1;
    final static int STATE_PLAYING = 2;
    final static int STATE_STOPPING = 3;
    final static int STATE_FINISHED = 4;

    private int state = 0;

    ArrayList<OnSubbeatListener> onSubbeatListeners = new ArrayList<>();

    private Section section;

    int isubbeat;
    long timeSinceLast;
    long lastBeatPlayed;

    int totalSubbeats;

    private SoundManager soundManager;

    private PlaybackThread playbackThread;

    private boolean cancelPlaybackThread = false;

    private int progressionI = -1;

    private int currentChord = 0;

    private long mSyncTime = 0L;

    Player() {
    }

    private void playBeatSampler(int subbeat) {

        for (Part part : section.parts) {
            if (subbeat == 0) {
                //todo part.resetI();
            }
            //todo part.getSoundsToPlayForBeat(subbeat);
        }
    }

    void play(Section section) {

        this.section = section;

        if (state != STATE_PLAYING) {
            cancelPlaybackThread = false;
            playbackThread = new PlaybackThread();
            playbackThread.start();
        }

        state = STATE_PLAYING;

        //todo call onStart()
    }



    int getCurrentSubbeat() {
        return isubbeat;
        //todo there's probably a good reason for this code below
        //todo but it made the beat counter not right (because I added measures)
        //todo so.... why was it here? mistake? or part of the recording features?
        //int i = playbackThread.isubbeat;
        //if (i == 0) i = beats * subbeats;
        //return i - 1;

    }

    /*int getClosestSubbeat(DebugTouch debugTouch) {
        if (playbackThread == null)
            return 0;

        int i = isubbeat;

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

    void stop() {
        state = STATE_STOPPING;
        cancelPlaybackThread = true;

        for (Part part : section.parts) {
            //todo part.mute();
        }
        state = STATE_STOPPED;
    }

    void finish() {
        for (Part part : section.parts) {
            //todo part.finish();
        }
        state = STATE_FINISHED;
    }

    boolean isPlaying() {
        return state == STATE_PLAYING;
    }


    int getChordInProgression() {
        return progressionI;
    }

    private class PlaybackThread extends Thread {

        public void run() {

            startPlayback();
            while (!cancelPlaybackThread) {
                if (isTime()) {
                    doTheThing();
                }
            }
            Log.d("MGH playback", "looks like we're done");
        }
    }

    private void startPlayback() {
        Log.d("MGH playback", "startPlayback");
        progressionI = -1; // gets incremented by onNewLoop
        onNewLoop();

        isubbeat = 0;
        lastBeatPlayed = System.currentTimeMillis() - section.beatParameters.subbeatLength;
    }

    private boolean isTime() {
        //Log.d("MGH playback", "isTime?");
        long timeUntilNext;
        long now;

        now = System.currentTimeMillis();
        timeUntilNext = lastBeatPlayed + section.beatParameters.subbeatLength;
        if (isubbeat % section.beatParameters.subbeats != 0 && section.beatParameters.shuffle > 0) {
            timeUntilNext += (int) (section.beatParameters.subbeatLength * section.beatParameters.shuffle);
        }

        if (now >= timeUntilNext) {
            return true;
        }
        //Log.d("MGH playback", "isubbeat:" + isubbeat + ", timeUntilNext-now:" + (timeUntilNext - now));
        //todo better spot for this?
        pollFinishedNotes(now);

        if (timeUntilNext - now > 50) {
            try {
                Thread.sleep(section.beatParameters.subbeatLength - 50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void doTheThing() {
        totalSubbeats = BeatParameters.getTotalSubbeats(section.beatParameters);
        Log.d("MGH playback", "doTheThing " + isubbeat + "/" + totalSubbeats);
        if (isubbeat < totalSubbeats) {
            lastBeatPlayed += section.beatParameters.subbeatLength;
            playBeatSampler(isubbeat);
        }

        for (OnSubbeatListener subbeatListener : onSubbeatListeners) {
            subbeatListener.onSubbeat(isubbeat);
        }

        isubbeat++;

        if (mSyncTime > 0) {
            isubbeat = 1;
            lastBeatPlayed = mSyncTime + section.beatParameters.subbeatLength;
            mSyncTime = 0;
        }
        if (isubbeat >= totalSubbeats) {
            isubbeat = 0;
            onNewLoop();
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
