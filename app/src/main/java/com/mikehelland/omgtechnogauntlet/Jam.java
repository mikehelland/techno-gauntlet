package com.mikehelland.omgtechnogauntlet;

import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

class Jam {

    private String mTags = "";
    private Random rand = new Random();

    private int subbeats = 4;
    private int beats = 4;
    private int measures = 2;

    private int subbeatLength = 125; //70 + rand.nextInt(125); // 125;

    private float shuffle = 0;

    private List<Channel> mChannels = new CopyOnWriteArrayList<>();

    private OMGSoundPool pool;

    private PlaybackThread playbackThread;

    private boolean cancelPlaybackThread = false;

    private MelodyMaker mm;

    private boolean playing = false;
    private boolean mIsPaused = true;

    private int progressionI = -1;

    private List<View> viewsToInvalidateOnBeat = new CopyOnWriteArrayList<>();
    private List<View> viewsToInvalidateOnNewMeasure = new CopyOnWriteArrayList<>();

    private int currentChord = 0;

    private List<StateChangeCallback> mStateChangeListeners = new CopyOnWriteArrayList<>();

    private long mSyncTime = 0L;

    private String appName = "";

    Jam(MelodyMaker melodyMaker, OMGSoundPool pool, String appName) {

        this.pool = pool;

        mm = melodyMaker;
        mm.makeMelodyFromMotif(beats);

        this.appName = appName;
    }

    void addStateChangeListener(StateChangeCallback listener) {
        mStateChangeListeners.add(listener);
    }
    void removeStateChangeListener(StateChangeCallback listener) {
        //listener.remove = true;
        mStateChangeListeners.remove(listener);
    }

    void loadSoundSets() {
        for (Channel channel : mChannels) {
            if (!channel.loadSoundSetIds())
                return;
        }
    }

    private void playBeatSampler(int subbeat) {

        if (subbeat == 0) {
            for (Channel channel : mChannels) {
                channel.resetI();
            }
        }

        for (int i = 0; i < mChannels.size(); i++) {
            mChannels.get(i).playBeat(subbeat);
        }

    }

    private void makeChannelNotes(Channel channel) {

        if (rand.nextInt(5) == 0) {
            mm.makeMotif();
            mm.makeMelodyFromMotif(beats);
        }
        else if (rand.nextInt(3) == 0) {
            // keep the motif, but change melody
            mm.makeMelodyFromMotif(beats);
        }

        mm.cloneCurrentMelodyTo(channel.getNotes());

        mm.applyScale(channel, currentChord);
        channel.resetI();


    }


    void kickIt() {

        if (!playing) {
            cancelPlaybackThread = false;
            playbackThread = new PlaybackThread();
            playbackThread.start();
        }
        mIsPaused = false;
        runCallbacks("PLAY");
    }



    int getCurrentSubbeat() {
        return playbackThread.ibeat;
        //todo there's probably a good reason for this code below
        //todo but it made the beat counter not right (because I added measures)
        //todo so.... why was it here? mistake? or part of the recording features?
        //int i = playbackThread.ibeat;
        //if (i == 0) i = beats * subbeats;
        //return i - 1;

    }

    int getClosestSubbeat(DebugTouch debugTouch) {
        if (playbackThread == null)
            return 0;

        int i = playbackThread.ibeat;

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

    }

    int getSubbeatLength() {
        return subbeatLength;
    }

    List<Channel> getChannels() {
        return mChannels;
    }

    void addChannel(Channel channel) {
        mChannels.add(channel);
    }

    void setTags(String s) {
        mTags = s;
    }

    String getTags() {
        return mTags;
    }

    float getShuffle() {
        return shuffle;
    }

    void setBeats(int beats) {
        this.beats = beats;
    }
    void setSubbeats(int subbeats) {
        this.subbeats = subbeats;
    }

    void copyChannel(Channel channel) {
        final Channel newChannel = new Channel(this, pool);
        newChannel.prepareSoundSet(new SoundSet(channel.getSoundSet()));
        newChannel.loadSoundSetIds();
        newChannel.setSurface(channel.getSurface().copy());
        if (newChannel.useSequencer()) {
            newChannel.setPattern(channel.pattern.clone());
        }
        else {
            NoteList newNotes = new NoteList();
            for (Note note : channel.getNotes()) {
                newNotes.add(note.cloneNote());
            }
            newChannel.setNotes(newNotes);
        }
        addChannel(newChannel);
    }

    private class PlaybackThread extends Thread {

        int ibeat;
        long timeSinceLast;

        public void run() {

            playing = true;
            progressionI = -1; // gets incremented by onNewLoop
            onNewLoop(System.currentTimeMillis());

            long lastBeatPlayed = System.currentTimeMillis() - subbeatLength;
            long now;

            //long ticks = 0;

            boolean hasSlept = false;
            ibeat = 0;
            long timeUntilNext;
            while (!cancelPlaybackThread) {

                now = System.currentTimeMillis();

                timeSinceLast = now - lastBeatPlayed;

                timeUntilNext = lastBeatPlayed + subbeatLength;
                if (ibeat % subbeats != 0 && shuffle > 0) {
                    timeUntilNext += (int)(subbeatLength * shuffle);
                }

                if (mSyncTime == 0 && now < timeUntilNext) {
                    pollFinishedNotes(now);
                    //ticks++;

                    if (!hasSlept) {
                        hasSlept = true;
                        try {
                            sleep(subbeatLength - 50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    continue;
                }

                hasSlept = false;

                //Log.d("MGH ticks since", "" + mghTicks);
                //Log.d("MGH off by", now - lastBeatPlayed + "");
                //ticks = 0;

                if (ibeat < beats * subbeats * measures) {
                    lastBeatPlayed += subbeatLength;
                    playBeatSampler(ibeat);
                }

                ibeat++;

                if (mSyncTime > 0) {
                    ibeat = 1;
                    lastBeatPlayed = mSyncTime + subbeatLength;
                    mSyncTime = 0;
                }
                if (ibeat >= beats * subbeats * measures) {
                    ibeat = 0;
                    onNewLoop(now);

                    for (View iv : viewsToInvalidateOnNewMeasure) {
                        iv.postInvalidate();
                    }
                }

                for (View iv : viewsToInvalidateOnBeat) {
                    iv.postInvalidate();
                }
            }

            playing = false;

            for (View iv : viewsToInvalidateOnBeat) {
                iv.postInvalidate();
            }

        }

        void pollFinishedNotes(long now) {
            long finishAt;
            Channel channel;
            for (int i = 0; i < mChannels.size(); i++) { // Channel channel : mChannels) {
                try {
                    channel = mChannels.get(i);
                    finishAt = channel.getFinishAt();
                    if (finishAt > 0 && now >= finishAt) {
                        channel.mute();
                        channel.finishCurrentNoteAt(0);
                    }
                }
                catch  (Exception ignore) {}
            }
        }

    }

    private int[] progression = {0};

    private void onNewLoop(Long now) {

        runCallbacks("ON_NEW_LOOP");

        progressionI++;

        if (progressionI >= progression.length || progressionI < 0) {
            progressionI = 0;
        }
        int chord = progression[progressionI];

        updateChord(chord);

    }

    private void updateChord(int chord) {
        for (Channel channel : mChannels) {
            if (channel.getSoundSet().isChromatic())
                mm.applyScale(channel, chord);
        }
    }

    void pause() {
        mIsPaused = true;
        cancelPlaybackThread = true;

        for (Channel channel : mChannels) {
            channel.mute();
        }

        runCallbacks("STOP");
    }

    void finish() {
        mIsPaused = true;
        for (Channel channel : mChannels) {
            channel.finish();
        }
    }

    public void randomRuleChange(long now) {

        int change = rand.nextInt(7);

        switch (change) {
            case 0:
                //subbeatLength += rand.nextBoolean() ? 10 : -10;
                setSubbeatLength(70 + rand.nextInt(125)); // 125
                break;
            case 1:
                mm.pickRandomKey();
                break;
            case 2:
                mm.pickRandomScale();
                break;
            case 3:
                makeChordProgression();
                //progressionI = 0;

                break;
            case 4:
                //drumChannel.makeDrumBeatsFromMelody(basslineChannel.getNotes());
                break;
            case 5:
                //makeChannelNotes(basslineChannel);

                break;
            case 6:
                //makeChannelNotes(keyboardChannel);

                break;

        }

    }

    String getKeyName() {
        return mm.getKeyName();
    }

    private void makeChordProgression() {
        progressionI = 0;

        int pattern = rand.nextInt(10);
        int scaleLength = mm.getScaleLength();

        if (pattern < 2) {
            int rc =  2 + rand.nextInt(scaleLength - 2);
            progression = new int[] {0, 0, rc, rc};
        }
        else if (pattern < 4)
            progression = new int[] {0,
                                    rand.nextInt(scaleLength),
                                    rand.nextInt(scaleLength),
                                    rand.nextInt(scaleLength)};

        else if (pattern < 6) {
            if (scaleLength == 6)
                progression = new int[] {0, 4, 5, 2};
            else if (scaleLength == 5) {
                progression = new int[] {0, 3, 4, 2};
            }
            else {
                progression = new int[] {0, 4, 5, 3};
            }
        }

        else if (pattern < 8) {
            if (scaleLength == 6)
                progression = new int[] {0, 0, 2, 0, 4, 2, 0, 4};
            else if (scaleLength == 5) {
                progression = new int[] {3, 4, 2, 0, };
            }
            else {
                progression = new int[] {0, 0, 3, 0, 4, 3, 0, 4};
            }
        }

        else if (pattern == 8)
            progression = new int[] {rand.nextInt(scaleLength), rand.nextInt(scaleLength),
                    rand.nextInt(scaleLength)};

        else {
            int changes = 1 + rand.nextInt(8);
            progression = new int[changes];
            for (int i = 0; i < changes; i++) {
                progression[i] = rand.nextInt(scaleLength);
            }
        }

        currentChord = progression[0];

    }

    int getBPM() {
        return 60000 / (subbeatLength * subbeats);
    }

    boolean isPlaying() {
        return playing;
    }

    String getData() {

        StringBuilder sb = new StringBuilder();

        sb.append("{\"type\": \"SECTION\", \"tags\": \"");
        sb.append(mTags);
        sb.append("\", \"madeWith\": \"");
        sb.append(appName);
        sb.append("\", \"scale\": \"");
        sb.append(mm.getScale());
        sb.append("\", \"ascale\": [");
        sb.append(mm.getScale());
        sb.append("], \"rootNote\": ");
        sb.append(mm.getKey());
        sb.append(", \"measures\" :");
        sb.append(measures);
        sb.append(", \"beats\" :");
        sb.append(beats);
        sb.append(", \"subbeats\" :");
        sb.append(subbeats);
        sb.append(", \"subbeatMillis\" :");
        sb.append(subbeatLength);
        sb.append(", \"shuffle\" :");
        sb.append(shuffle);
        sb.append(", ");
        getChordsData(sb);

        sb.append(", \"parts\" : [");

        for (Channel channel : mChannels) {
            channel.getData(sb);
            sb.append(",");
        }

        sb.delete(sb.length() - 1, sb.length());
        sb.append("]}");

        Log.d("MGH getData", sb.toString());
        return sb.toString();

    }


    private void getChordsData(StringBuilder sb) {

        sb.append("\"chordProgression\" : [");

        boolean first = true;

        for (int chord : progression) {
            if (first)
                first = false;
            else
                sb.append(", ");

            sb.append(chord);
        }
        sb.append("]");

    }

    void monkeyWithEverything() {

        mTags = "";

        setSubbeatLength(70 + rand.nextInt(125));

        setKey(mm.getRandomKey());
        setScale(mm.getRandomScale());
        makeChordProgression();

        mm.makeMotif();
        mm.makeMelodyFromMotif(beats);

        for (Channel channel : mChannels) {
            monkeyWithChannel(channel);
        }

        playbackThread.ibeat = 0;

    }

    void monkeyWithChannel(Channel channel) {

        if (channel.getSoundSet().isChromatic()) {
            makeChannelNotes(channel);
        }
        else {
            monkeyWithDrums(channel);
        }
    }

    private void monkeyWithDrums(Channel channel) {

        DrumMonkey monkey = new DrumMonkey(this, channel);

        if (channel.getSoundSetName().toLowerCase().contains("kit")
                || rand.nextBoolean()) {
            //todo makeDrumBeatFromMelody is a possibility
            monkey.makeDrumBeats();
        }
        else {
            monkey.makePercussionFill();
        }
        channel.setPattern(monkey.getPattern());

    }

    int[] getProgression() {
        return progression;
    }

    int getChordInProgression() {
        return progressionI;
    }

    int[] getScale() {
        return mm.getScaleArray();
    }

    int getScaleIndex() {
        return mm.getScaleIndex();
    }

    String getScaleString() {
        return mm.getScale();
    }

    int getKey() {
        return mm.getKey() % 12;
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

    void setScale(String scale) {
        setScale(scale, null);
    }
    void setScale(int scaleI) {
        mm.setScale(scaleI);
        afterScaleChange(null);
    }
    void setScale(String scale, String source) {
        mm.setScale(scale);
        afterScaleChange(source);
    }
    private void afterScaleChange(String source) {
        for (StateChangeCallback callback : mStateChangeListeners) {
            callback.onScaleChange(mm.getScale(), source);
        }
    }

    void setKey(int keyI) {
        setKey(keyI, null);
    }
    void setKey(int keyI, String source) {
        mm.setKey(keyI);

        for (StateChangeCallback callback : mStateChangeListeners) {
            callback.onKeyChange(keyI, source);
        }
    }

    void setBPM(float bpm) {
        setSubbeatLength((int)((60000 / bpm) / subbeats));
        //bpm = 60000 / (subbeatLength * subbeats);
    }

    void setShuffle(float value) {
        shuffle = value;
    }

    void setSubbeatLength(int length) {
        setSubbeatLength(length, null);
    }
    void setSubbeatLength(int length, String source) {
        subbeatLength = length;

        for (StateChangeCallback callback : mStateChangeListeners) {
            callback.onSubbeatLengthChange(length, source);
        }
    }

    void setChordProgression(int[] chordProgression) {
        // ifeel like this should be negative one?
        progressionI = 0;
        progression = chordProgression;
        currentChord = progression[0];

        if (chordProgression.length == 1) {
            updateChord(currentChord);
        }

        for (StateChangeCallback callback : mStateChangeListeners) {
            callback.onChordProgressionChange(chordProgression);
        }
    }

    int[] getChordProgression() {
        return progression;
    }

    int getTotalBeats() {
        return beats * measures;
    }
    int getBeats() {
        return beats;
    }
    void setMeasures(int i) {
        measures = i;
    }
    int getMeasures() {
        return measures;
    }

    int getSubbeats() {
        return subbeats;
    }

    int getTotalSubbeats() {
        return subbeats * beats * measures;
    }

    Random getRand() {
        return rand;
    }

    int getScaledNoteNumber(int basicNote) {
        return mm.scaleNote(basicNote, 0);
    }

    abstract static class StateChangeCallback {
        abstract void onSubbeatLengthChange(int length, String source);
        abstract void onKeyChange(int key, String source);
        abstract void onScaleChange(String scale, String source);
        abstract void onChordProgressionChange(int[] chords);
        abstract void onNewChannel(Channel channel);
        abstract void onChannelEnabledChanged(Channel channel, boolean enabled, String source);
        abstract void onChannelVolumeChanged(Channel channel, float volume, String source);
        abstract void onChannelPanChanged(Channel channel, float pan, String source);

        abstract void newState(String stateChange, Object... args);
    }

    Channel newChannel(SoundSet soundSet) {

        final Channel channel = new Channel(this, pool);
        channel.prepareSoundSet(soundSet);

        new Thread(new Runnable() {
            @Override
            public void run() {
                pool.loadSounds();
                channel.loadSoundSetIds();
                addChannel(channel);

                for (StateChangeCallback callback : mStateChangeListeners) {
                    callback.onNewChannel(channel);
                }

            }
        }).start();

        return channel;
    }

    Channel getChannelByID(String channelID) {
        for (Channel channel : mChannels) {
            if (channel.getID().equals(channelID)) {
                return channel;
            }
        }
        return null;
    }

    void setChannelVolume(String channelID, float v, String device) {
        Channel channel = getChannelByID(channelID);
        if (channel != null) {
            setChannelVolume(channel, v, device);
        }
    }
    void setChannelPan(String channelID, float p, String device) {
        Channel channel = getChannelByID(channelID);
        if (channel != null) {
            setChannelPan(channel, p, device);
        }
    }
    void setChannelEnabled(String channelID, boolean on, String device) {
        Channel channel = getChannelByID(channelID);
        if (channel != null) {
            setChannelEnabled(channel, on, device);
        }
    }
    void setChannelVolume(Channel channel, float v, String device) {
        channel.setVolume(v);

        for (StateChangeCallback callback : mStateChangeListeners) {
            callback.onChannelVolumeChanged(channel, v, device);
        }
    }
    void setChannelPan(Channel channel, float p, String device) {
        channel.setPan(p);

        for (StateChangeCallback callback : mStateChangeListeners) {
            callback.onChannelPanChanged(channel, p, device);
        }
    }
    void setChannelEnabled(Channel channel, boolean l, String device) {
        if (l)
            channel.enable();
        else
            channel.disable();

        for (StateChangeCallback callback : mStateChangeListeners) {
            callback.onChannelEnabledChanged(channel, l, device);
        }
    }
    boolean toggleChannelEnabled(Channel channel) {
        return toggleChannelEnabled(channel, null);
    }
    boolean toggleChannelEnabled(Channel channel, String device) {
        boolean enabled = channel.toggleEnabled();

        for (StateChangeCallback callback : mStateChangeListeners) {
            callback.onChannelEnabledChanged(channel, enabled, device);
        }

        return enabled;
    }

    private void runCallbacks(String state) {
        for (StateChangeCallback callback : mStateChangeListeners) {
            if (callback != null) {
                callback.newState(state);
            }
        }
        /*StateChangeCallback callback;
        for (int i = mStateChangeListeners.size() - 1; i >= 0; i--) {
            callback = mStateChangeListeners.get(i);
            if (callback != null) {
                if (callback.remove) {
                    mStateChangeListeners.remove(i);
                } else {
                    callback.newState(state);
                }
            }
        }*/
    }

    void syncNow() {
        mSyncTime = System.currentTimeMillis();
    }

    boolean isPaused() {return mIsPaused;}
}
