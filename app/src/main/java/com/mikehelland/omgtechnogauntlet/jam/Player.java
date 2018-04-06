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

    private ArrayList<Note> playingNotes = new ArrayList<>();

    private Section section;

    int isubbeat;
    long timeOfLastBeatPlayed;

    int totalSubbeats;

    private SoundManager soundManager;

    private PlaybackThread playbackThread;

    private boolean cancelPlaybackThread = false;

    private int progressionI = -1;

    int currentChord = 0;

    private long mSyncTime = 0L;

    private ArrayList<PlaySoundCommand> commands = new ArrayList<>();

    Player(SoundManager soundManager) {
        this.soundManager = soundManager;
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


    private void playBeatSampler(int subbeat) {

        //this was a class member to avoid allocating every beat, but throws concurrent modification errors
        //todo is that because multiple threads tried to access it? shouldn't be happening
        ArrayList<PlaySoundCommand> commands = new ArrayList<>();
        for (Part part : section.parts) {
            PartPlayer.getSoundsToPlayForPartAtSubbeat(commands, section, part,
                    subbeat, currentChord);
        }

        for (PlaySoundCommand command : commands) {
            if (command.note != null) {
                command.note.playingHandle = soundManager.playSound(command);
            } else {
                soundManager.playSound(command);
            }
        }
        for (PlaySoundCommand command : commands) {
            if (command.note != null) {
                command.note.startedPlayingAtSubbeat = isubbeat;
                playingNotes.add(command.note);
            }
        }
        //could be first, but might be faster down here?
        commands.clear();
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

    int getClosestSubbeat() {
        if (playbackThread == null)
            return 0;

        if (System.currentTimeMillis() - timeOfLastBeatPlayed < section.beatParameters.subbeatLength / 2) {
            return isubbeat - 1;
        }

        return isubbeat;

        //todo don't use 16th notes
        //if (i % 2 > 0)
        //    i = (i - 1) % (totalsubbeats);
    }

    private void onNewLoop() {
        progressionI++;

        if (progressionI >= section.progression.length || progressionI < 0) {
            progressionI = 0;
        }
        currentChord = section.progression[progressionI];

        //updateChord(chord);

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

        if (section != null) {
            for (Part part : section.parts) {
                //todo part.mute();
            }
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

    void playPartLiveNote(Part part, Note note) {
        note.playingHandle = soundManager.playSound(PartPlayer.getCommandForNote(part, note));
        note.startedPlayingAtSubbeat = getClosestSubbeat(); //todo note.createdAt
        //playingNotes.add(note);
    }

    void playPartLiveNotes(Part part, Note[] notes) {
        for (Note note : notes) {
            if (note.playingHandle == -1) {
                playPartLiveNote(part, note);
            }
        }
    }

    void stopPartLiveNote(Part part, Note note) {
        if (note.playingHandle > -1) {
            soundManager.stopSound(note.playingHandle);
        }
        if (isPlaying() && !part.getMute()) {
            note.setBeats(Math.max(1, 1 + isubbeat - note.startedPlayingAtSubbeat) /
                    (double)section.beatParameters.subbeats);
            part.notes.overwrite(note);
        }
    }

    private class PlaybackThread extends Thread {

        public void run() {

            startPlayback();
            while (!cancelPlaybackThread) {
                if (isTime()) {
                    doTheThing();
                }
            }
        }
    }

    private void startPlayback() {
        Log.d("MGH start playback", "" + Thread.currentThread().getId());
        progressionI = -1; // gets incremented by onNewLoop
        onNewLoop();

        isubbeat = 0;
        timeOfLastBeatPlayed = System.currentTimeMillis() - section.beatParameters.subbeatLength;
    }

    private boolean isTime() {
        //Log.d("MGH playback", "isTime?");
        long timeUntilNext;
        long now;

        now = System.currentTimeMillis();
        timeUntilNext = timeOfLastBeatPlayed + section.beatParameters.subbeatLength;
        if (isubbeat % section.beatParameters.subbeats != 0 && section.beatParameters.shuffle > 0) {
            timeUntilNext += (int) (section.beatParameters.subbeatLength * section.beatParameters.shuffle);
        }

        if (now >= timeUntilNext) {
            return true;
        }
        //Log.d("MGH playback", "isubbeat:" + isubbeat + ", timeUntilNext-now:" + (timeUntilNext - now));

        if (timeUntilNext - now > 50) {
            try {
                Thread.sleep(section.beatParameters.subbeatLength - 50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //todo better spot for this?
            pollFinishedNotes(now);
        }
        return false;
    }

    private void doTheThing() {
        totalSubbeats = BeatParameters.getTotalSubbeats(section.beatParameters);
        //Log.d("MGH playback", "doTheThing " + isubbeat + "/" + totalSubbeats);
        if (isubbeat < totalSubbeats) {
            timeOfLastBeatPlayed += section.beatParameters.subbeatLength;
            playBeatSampler(isubbeat);
        }

        for (OnSubbeatListener subbeatListener : onSubbeatListeners) {
            subbeatListener.onSubbeat(isubbeat);
        }

        isubbeat++;

        if (mSyncTime > 0) {
            isubbeat = 1;
            timeOfLastBeatPlayed = mSyncTime + section.beatParameters.subbeatLength;
            mSyncTime = 0;
        }
        if (isubbeat >= totalSubbeats) {
            isubbeat = 0;
            onNewLoop();
        }
    }

    void pollFinishedNotes(long now) {
        long finishAt;
        //try {
        for (int i = 0; i < playingNotes.size(); i++) {
            Note note = playingNotes.get(i);
            if (note.startedPlayingAtSubbeat + section.beatParameters.subbeats * note.getBeats() <= isubbeat) {
                soundManager.stopSound(note.playingHandle);
                playingNotes.remove(note);
                i--;
            }
            //todo
                /*finishAt = part.getFinishAt();
                if (finishAt > 0 && now >= finishAt) {
                    part.mute();
                    part.finishCurrentNoteAt(0);
                }*/
        }
        //} catch (Exception ignore) {}
    }
}
