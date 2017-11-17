package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Jam {

    private String mTags = "";
    private Random rand = new Random();

    private int subbeats = 4;
    private int beats = 8;
    private int totalsubbeats = subbeats * beats;
    private int subbeatLength = 125; //70 + rand.nextInt(125); // 125;

    private float shuffle = 0;

    private ArrayList<Channel> mChannels = new ArrayList<>();

    private OMGSoundPool pool;

    private Context mContext;

    private PlaybackThread playbackThread;

    private boolean cancelPlaybackThread = false;

    private MelodyMaker mm;

    private boolean playing = false;

    private int progressionI = -1;

    private ArrayList<View> viewsToInvalidateOnBeat = new ArrayList<>();
    private ArrayList<View> viewsToInvalidateOnNewMeasure = new ArrayList<>();

    private int currentChord = 0;

    private List<StateChangeCallback> mStateChangeListeners = new ArrayList<>();

    Jam(Context context, OMGSoundPool pool) {

        this.pool = pool;

        mContext = context;
        mm = new MelodyMaker(mContext);
        mm.makeMotif();
        mm.makeMelodyFromMotif(beats);
    }

    void addStateChangeListener(StateChangeCallback listener) {
        mStateChangeListeners.add(listener);
    }
    void removeStateChangeListener(StateChangeCallback listener) {
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

        for (StateChangeCallback callback : mStateChangeListeners) {
            callback.onPlay();
        }
    }



    int getCurrentSubbeat() {
        int i = playbackThread.ibeat;
        if (i == 0) i = beats * subbeats;
        return i - 1;

    }

    int getClosestSubbeat(DebugTouch debugTouch) {
        if (playbackThread == null)
            return 0;

        int i = playbackThread.lastI;

        debugTouch.iclosestsubbeat = i;
        debugTouch.dbeat = (i + playbackThread.timeSinceLast / (double)subbeatLength) / subbeats;

        // don't use 16th notes
        //if (i % 2 > 0)
        //    i = (i - 1) % (totalsubbeats);

        //if (i < 0) i = totalsubbeats - 1;

        if (playbackThread.timeSinceLast > subbeatLength / 2) {
            i = i + 1;
            if (i == totalsubbeats)
                i = 0;

            //if (i == -1) i = beats * subbeats - 1;
        }

        debugTouch.isubbeatgiven = i;


        return i;

    }

    int getSubbeatLength() {
        return subbeatLength;
    }

    boolean load(String json) {

        boolean good = false;
        try {

            JSONObject jsonData = new JSONObject(json);

            JSONArray parts;
            parts = jsonData.getJSONArray("parts");

            if (jsonData.has("subbeatMillis")) {
                setSubbeatLength(jsonData.getInt("subbeatMillis"));
            }

            if (jsonData.has("shuffle")) {
                setShuffle((float)jsonData.getDouble("shuffle"));
            }

            if (jsonData.has("rootNote")) {
                setKey(jsonData.getInt("rootNote") % 12);
            }

            if (jsonData.has("scale")) {
                setScale(jsonData.getString("scale"));
            }

            if (jsonData.has("chordProgression")) {
                JSONArray chordsData = jsonData.getJSONArray("chordProgression");
                int[] newChords = new int[chordsData.length()];
                for (int ic = 0; ic < chordsData.length(); ic++) {
                    newChords[ic] = chordsData.getInt(ic);
                }
                setChordProgression(newChords);
            }

            if (jsonData.has("tags")) {
                mTags = jsonData.getString("tags");
            }

            Channel channel;

            for (int ip = 0; ip < parts.length(); ip++) {
                JSONObject part = parts.getJSONObject(ip);
                String type = part.getString("type");

                //todo get rid of this, only good for old saved songs, prelaunch
                if ("CHORDPROGRESSION".equals(type)) {
                    Log.d("MGH", "loading chord progression");
                    Log.d("MGH", part.toString());
                    JSONArray chordsData = part.getJSONArray("data");
                    int[] newChords = new int[chordsData.length()];
                    for (int ic = 0; ic < chordsData.length(); ic++) {
                        newChords[ic] = chordsData.getInt(ic);
                    }
                    setChordProgression(newChords);
                    continue;
                }

                channel = new Channel(mContext, this, pool);
                loadPart(channel, part);
                mChannels.add(channel);
            }

            onNewLoop();

            good = true;

        } catch (JSONException e) {
            Log.d("MGH loaddata exception", e.getMessage());
            Toast.makeText(mContext,
                    "Could not load data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return good;

    }

    ArrayList<Channel> getChannels() {
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

    private class PlaybackThread extends Thread {

        int ibeat;
        int lastI;
        long timeSinceLast;

        public void run() {

            playing = true;
            progressionI = -1; // gets incremented by onNewLoop
            onNewLoop();

            long lastBeatPlayed = System.currentTimeMillis() - subbeatLength;
            long now;

            ibeat = 0;


            while (!cancelPlaybackThread) {

                now = System.currentTimeMillis();
                timeSinceLast = now - lastBeatPlayed;

                if (ibeat % subbeats == 0) {
                    if (timeSinceLast < subbeatLength) {
                        pollFinishedNotes(now);
                        continue;
                    }
                }
                else {
                    if (timeSinceLast < subbeatLength + (int)(subbeatLength * shuffle)) {
                        pollFinishedNotes(now);
                        continue;
                    }
                }

                //lastBeatPlayed = now;
                lastBeatPlayed += subbeatLength;
                playBeatSampler(ibeat);

                lastI = ibeat++;

                if (ibeat == beats * subbeats) {
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

            playing = false;

        }

        void pollFinishedNotes(long now) {
            long finishAt;
            Channel channel;
            for (int i = 0; i < mChannels.size(); i++) { // Channel channel : mChannels) {
                channel = mChannels.get(i);
                finishAt = channel.getFinishAt();
                if (finishAt > 0 && now >= finishAt) {
                    channel.mute();
                    channel.finishCurrentNoteAt(0);
                }
            }
        }

    }

    private int[] progression = {0};

    private void onNewLoop() {

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

    void finish() {
        cancelPlaybackThread = true;

        for (Channel channel : mChannels) {
            channel.mute();
            channel.finish();
        }

        for (StateChangeCallback callback : mStateChangeListeners) {
            callback.onStop();
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
        sb.append("\", \"scale\": \"");
        sb.append(mm.getScale());
        sb.append("\", \"ascale\": [");
        sb.append(mm.getScale());
        sb.append("], \"rootNote\": ");
        sb.append(mm.getKey());
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

        DrumMonkey monkey = new DrumMonkey(this);

        if (rand.nextBoolean()) {
            monkey.makePercussionFill();
        }
        else {
            //todo makeDrumBeatFromMelody is a possibility

            monkey.makeDrumBeats();
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

    int getBeats() {
        return beats;
    }

    int getSubbeats() {
        return subbeats;
    }

    int getTotalSubbeats() {
        return totalsubbeats;
    }

    Random getRand() {
        return rand;
    }

    int getScaledNoteNumber(int basicNote) {
        return mm.scaleNote(basicNote, 0);
    }

    private void loadMelody(Channel channel, JSONObject part) throws JSONException {

        NoteList notes = channel.getNotes();
        notes.clear();

        if (part.has("volume")) {
            channel.volume = (float)part.getDouble("volume");
        }
        if (part.has("mute") && part.getBoolean("mute"))
            channel.disable();
        else
            channel.enable();

        JSONArray notesData = part.getJSONArray("notes");

        Note newNote;
        JSONObject noteData;

        for (int i = 0; i < notesData.length(); i++) {
            noteData = notesData.getJSONObject(i);

            newNote = new Note();
            newNote.setBeats(noteData.getDouble("beats"));

            newNote.setRest(noteData.getBoolean("rest"));

            if (!newNote.isRest()) {
                newNote.setBasicNote(noteData.getInt("note"));
                if (!channel.getSoundSet().isChromatic()) {
                    newNote.setScaledNote(newNote.getBasicNote());
                    newNote.setInstrumentNote(newNote.getBasicNote());
                }
            }
            notes.add(newNote);
        }

    }

    private void loadPart(Channel jamChannel, JSONObject part) throws  JSONException {
        String soundsetURL = part.getString("soundsetURL");

        jamChannel.prepareSoundSetFromURL(soundsetURL);

        if (part.has("surfaceURL")) {
            String surfaceURL = part.getString("surfaceURL");
            jamChannel.setSurface(surfaceURL);
        }

        if (part.has("volume")) {
            jamChannel.volume = (float)part.getDouble("volume");
        }
        //todo pan

        if (jamChannel.getSurfaceURL().equals("PRESET_SEQUENCER")) {
            loadDrums(jamChannel, part);
        }
        else {
            loadMelody(jamChannel, part);
        }
    }

    private void loadDrums(Channel jamChannel, JSONObject part) throws JSONException {


        JSONArray tracks = part.getJSONArray("tracks");

        JSONObject track;
        JSONArray trackData;

        boolean[][] pattern = jamChannel.pattern;

        if (part.has("volume")) {
            jamChannel.volume = (float)part.getDouble("volume");
        }
        if (part.has("mute") && part.getBoolean("mute"))
            jamChannel.disable();
        else
            jamChannel.enable();

        //underrun overrun?
        //match the right channels?
        // this assumes things are in the right order

        for (int i = 0; i < tracks.length(); i++) {
            track = tracks.getJSONObject(i);

            trackData = track.getJSONArray("data");

            for (int j = 0; j < trackData.length(); j++) {
                pattern[i][j] = trackData.getInt(j) == 1;
            }

        }

    }

    abstract static class StateChangeCallback {
        abstract void onPlay();
        abstract void onStop();
        abstract void onSubbeatLengthChange(int length, String source);
        abstract void onKeyChange(int key, String source);
        abstract void onScaleChange(String scale, String source);
        abstract void onChordProgressionChange(int[] chords);
        abstract void onNewChannel(Channel channel);
    }

    Channel newChannel(long soundsetId) {
        final Channel channel = new Channel(mContext, this, pool);

        SoundSetDataOpenHelper helper = new SoundSetDataOpenHelper(mContext);
        channel.prepareSoundSet(helper.getSoundSetById(soundsetId));

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
}
