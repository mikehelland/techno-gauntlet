package com.mikehelland.omgtechnogauntlet.jam;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

class Player {

    final static int STATE_STOPPED = 0;
    final static int STATE_STARTING = 1;
    final static int STATE_PLAYING = 2;
    final static int STATE_STOPPING = 3;
    final static int STATE_FINISHED = 4;

    private volatile int state = 0;

    CopyOnWriteArrayList<OnSubbeatListener> onSubbeatListeners = new CopyOnWriteArrayList<>();

    private ArrayList<PlaySoundCommand> playingCommands = new ArrayList<>();

    //private CopyOnWriteArrayList<PartPlayer> partPlayers = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<JamPart> jamParts = new CopyOnWriteArrayList<>();

    private Section section;

    int isubbeat;
    long timeOfLastBeatPlayed;

    int totalSubbeats;

    private SoundManager soundManager;

    private PlaybackThread playbackThread;

    private int progressionI = -1;

    int currentChord = 0;

    private long mSyncTime = 0L;

    private ArrayList<PlaySoundCommand> commands = new ArrayList<>();

    Player(SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    void play(Section section, CopyOnWriteArrayList<JamPart> jamParts) {

        this.section = section;
        this.jamParts = jamParts;

        if (state != STATE_PLAYING) {
            state = STATE_PLAYING;
            playbackThread = new PlaybackThread();
            playbackThread.start();
        }

        //todo call onStart()
    }

    private void playBeatSampler(int subbeat) {

        //this was a class member to avoid allocating every beat, but throws concurrent modification errors
        //todo is that because multiple threads tried to access it? shouldn't be happening
        ArrayList<PlaySoundCommand> commands = new ArrayList<>();

        for (JamPart jamPart : jamParts) {
            jamPart.partPlayer.getSoundsToPlayForPartAtSubbeat(commands, subbeat, currentChord);
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
                playingCommands.add(command);
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
    }

    void stop() {
        Log.d("MGH stop1 playback", "" + Thread.currentThread().getId());
        state = STATE_STOPPING;

        if (playbackThread != null) {
            playbackThread.cancel = true;
            Log.d("MGH stop2 playback", "" + playbackThread.getId());
        }

        if (section != null) {
            for (Part part : section.parts) {
                if (part.soundSet.isOscillator()) {
                    part.soundSet.getOscillator().mute();
                }
            }
        }

        for (OnSubbeatListener subbeatListener : onSubbeatListeners) {
            subbeatListener.onSubbeat(-1);
        }

        state = STATE_STOPPED;
    }

    void finish() {
        if (section != null && section.parts != null) {
            for (Part part : section.parts) {
                //part.finish();
            }
        }
        if (soundManager != null) {
            soundManager.cleanUp();
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
        if (soundManager == null) {
            return;
        }
        for (Note note : notes) {
            if (note.playingHandle == -1) {
                playPartLiveNote(part, note);
            }
        }
    }

    void stopPartLiveNote(Part part, Note note) {
        if (part.soundSet.isOscillator()) {
            part.soundSet.getOscillator().mute();
        }
        if (note.playingHandle > -1) {
            soundManager.stopSound(note.playingHandle);
        }
        note.playingHandle = -1;
        if (isPlaying() && !part.audioParameters.mute) {
            //note.setBeats(Math.max(1, 1 + isubbeat - note.startedPlayingAtSubbeat) /
            //        (double)section.beatParameters.subbeats);
            //part.notes.overwrite(note);
        }
    }

    private class PlaybackThread extends Thread {
        boolean cancel = false;
        public void run() {

            startPlayback();
            while (!cancel) {
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
            pollFinishedNotes();
        }
        return false;
    }

    private void doTheThing() {
        totalSubbeats = BeatParameters.getTotalSubbeats(section.beatParameters);
        //Log.d("MGH playback", "doTheThing " + isubbeat + "/" + totalSubbeats);
        if (isubbeat < totalSubbeats) {
            timeOfLastBeatPlayed += section.beatParameters.subbeatLength;
            if (soundManager != null) {
                playBeatSampler(isubbeat);
            }
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

    void pollFinishedNotes() {
        //try {
        PlaySoundCommand command;
        //Log.d("MGH pollfinishnotes", "commands size = " + playingCommands.size());
        for (int i = 0; i < playingCommands.size(); i++) {
            command = playingCommands.get(i);
            if (command != null && command.note != null) {
                if (command.note.startedPlayingAtSubbeat + section.beatParameters.subbeats * command.note.getBeats() <= isubbeat || isubbeat == 0) {
                    //Log.d("MGH finishing note", "command.note.startedPlayingAtSubbeat: " + command.note.startedPlayingAtSubbeat);
                    //Log.d("MGH finishing note", "command.note.getBeats(): " + command.note.getBeats());
                    //Log.d("MGH finishing note", command.note.startedPlayingAtSubbeat + section.beatParameters.subbeats * command.note.getBeats()+" <= " + isubbeat);
                    soundManager.stopSound(command);
                    playingCommands.remove(i);
                    i--;
                }
            }
        }
        //} catch (Exception ignore) {}
    }
}
