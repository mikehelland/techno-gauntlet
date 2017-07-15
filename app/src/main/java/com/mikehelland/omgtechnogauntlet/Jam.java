package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class Jam {

    private Random rand = new Random();

    private int subbeats = 4;
    private int beats = 8;
    private int totalsubbeats = subbeats * beats;
    private int subbeatLength = 125; //70 + rand.nextInt(125); // 125;

    private ArrayList<Channel> mChannels = new ArrayList<>();

    private DialpadChannel dialpadChannel;

    private OMGSoundPool pool;

    private Context mContext;

    PlaybackThread playbackThread;

    boolean cancel = false;

    private MelodyMaker mm;

    private boolean chordsEnabled = true;

    private boolean playing = false;

    private int progressionI = -1;

    private ArrayList<View> viewsToInvalidateOnBeat = new ArrayList<View>();
    private ArrayList<View> viewsToInvalidateOnNewMeasure = new ArrayList<View>();

    private int currentChord = 0;

    private int soundsToLoad = 0;

    private boolean soundPoolInitialized = false;

    public Jam(Context context, OMGSoundPool pool) {

        this.pool = pool;

        mContext = context;
        mm = new MelodyMaker(mContext);
        mm.makeMotif();
        mm.makeMelodyFromMotif(beats);

        setDrumset(0);

    }

    public void makeChannels(final ProgressBar progressBar, final Runnable callback) {

        pool.allowLoading();
        if (progressBar != null) {
            progressBar.setProgress(0);
        }

        boolean usingListener = false;
        boolean updatePB = false;

        dialpadChannel = new DialpadChannel(mContext, this, pool, "MELODY", new DialpadChannelSettings());

        soundsToLoad = 0;

        String[] channelConfig = PreferenceHelper.getLastChannelConfiguration(mContext).split(",");
        for (String sId : channelConfig) {
            Channel channel = new Channel(mContext, this, pool);
            soundsToLoad += channel.prepareSoundSet(sId);
            mChannels.add(channel);
        }

        if (Build.VERSION.SDK_INT >= 11 && progressBar != null) {
            usingListener = true;
            progressBar.setMax(soundsToLoad);
            pool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                int loadedSounds = 0;
                @Override
                public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                    loadedSounds++;
                    progressBar.incrementProgressBy(1);

                    if (loadedSounds == soundsToLoad) {

                        pool.setLoaded(true);

                        if (callback != null && !pool.isCanceled())
                            callback.run();

                    }
                }
            });
        }


        if (!usingListener) {
            updatePB = progressBar != null;
            if (updatePB)
                progressBar.setMax(5);
        }

        for (Channel channel : mChannels) {
            if (!channel.loadSoundSet())
                return;
            if (updatePB)
                progressBar.incrementProgressBy(1);

        }

        if (!usingListener) {
            if (!pool.isCanceled())
                callback.run();
        }

        soundPoolInitialized = true;
    }



    public void playBeatSampler(int subbeat) {

        if (subbeat == 0) {
            for (Channel channel : mChannels) {
                channel.resetI();
            }

            //basslineChannel.resetI();
            //guitarChannel.resetI();
            //keyboardChannel.resetI();
            dialpadChannel.resetI();

        }

        for (Channel channel : mChannels) {
            channel.playBeat(subbeat);
        }

        double beat = subbeat / (double)subbeats;

        //basslineChannel.playBeat(subbeat);
        //playChannelBeat(basslineChannel, beat);
        //playChannelBeat(guitarChannel, beat);
        //playChannelBeat(keyboardChannel, beat);
        playChannelBeat(dialpadChannel, beat);

    }

    public void makeChannelNotes(Channel channel) {

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


    public void kickIt() {

        if (!playing) {
            cancel = false;
            playbackThread = new PlaybackThread();
            playbackThread.start();
        }

    }



    public boolean  toggleMuteDsp() {
        dialpadChannel.toggleEnabled();
        return dialpadChannel.enabled;
    }

    public int getCurrentSubbeat() {
        int i = playbackThread.ibeat;
        if (i == 0) i = beats * subbeats;
        return i - 1;

    }

    public int getClosestSubbeat(DebugTouch debugTouch) {
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

    public int getSubbeatLength() {
        return subbeatLength;
    }

    public boolean load(String json) {

        boolean good = false;
        try {

            JSONObject jsonData = new JSONObject(json);

            JSONArray parts;
            parts = jsonData.getJSONArray("parts");

            if (jsonData.has("subbeatMillis")) {
                setSubbeatLength(jsonData.getInt("subbeatMillis"));
            }

            if (jsonData.has("rootNote")) {
                setKey(jsonData.getInt("rootNote") % 12);
            }

            if (jsonData.has("scale")) {
                setScale(jsonData.getString("scale"));
            }

            dialpadChannel = new DialpadChannel(mContext, this, pool, "MELODY", new DialpadChannelSettings());

            Channel channel;

            for (int ip = 0; ip < parts.length(); ip++) {
                JSONObject part = parts.getJSONObject(ip);
                String type = part.getString("type");

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
                load(channel, part);
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

    public ArrayList<Channel> getChannels() {
        return mChannels;
    }


    class PlaybackThread extends Thread {

        int ibeat;
        int lastI;
        long timeSinceLast;

        public void run() {

            playing = true;
            progressionI = -1; // gets incremented by onNewLoop
            onNewLoop();

            long lastBeatPlayed = 0;
            long now;

            ibeat = 0;


            while (!cancel) {

                now = System.currentTimeMillis();
                timeSinceLast = now - lastBeatPlayed;

                if (timeSinceLast < subbeatLength) {
                    pollFinishedNotes(now);
                    continue;
                }

                lastBeatPlayed = now;
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

        public void pollFinishedNotes(long now) {
            long finishAt;
            for (Channel channel : mChannels) {
                finishAt = channel.getFinishAt();
                if (finishAt > 0 && now >= finishAt) {
                    channel.mute();
                }
            }

        }

    }

    private int[] progression = {0};

    void onNewLoop() {

        //long now = System.currentTimeMillis();
        //if (lastHumanInteraction > now - 30000) {
        //    randomRuleChange(now);
        //}

        if (chordsEnabled)
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
        mm.applyScale(dialpadChannel, chord);
    }

    public void finish() {
        cancel = true;

        for (Channel channel : mChannels) {
            channel.mute();
        }


        dialpadChannel.mute();

        //mPool.release();
        //mPool = null;
    }


    public void randomRuleChange(long now) {

        int change = rand.nextInt(7);

        switch (change) {
            case 0:
                //subbeatLength += rand.nextBoolean() ? 10 : -10;
                subbeatLength = 70 + rand.nextInt(125); // 125
                break;
            case 1:
                mm.pickRandomKey();
                break;
            case 2:
                mm.pickRandomScale();
                break;
            case 3:
                makeChordProgression();
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


    public String getKeyName() {
        return mm.getKeyName();
    }

    public void makeChordProgression() {
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

    public int getBPM() {
        return 60000 / (subbeatLength * subbeats);
    }

    public boolean isPlaying() {
        return playing;
    }

    public String getData() {

        StringBuilder sb = new StringBuilder();

        sb.append("{\"type\": \"SECTION\", \"scale\": \"");
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

        sb.append(", \"parts\" : [");

        for (Channel channel : mChannels) {
            channel.getData(sb);
            sb.append(",");
        }

        //sb.delete(sb.length() - 1, sb.length());

        getChordsData(sb);

        sb.append("]}");

        Log.d("MGH getData", sb.toString());
        return sb.toString();

    }


    public void getChordsData(StringBuilder sb) {

        sb.append("{\"type\" : \"CHORDPROGRESSION\", \"data\" : [");

        boolean first = true;

        for (int chord : progression) {
            if (first)
                first = false;
            else
                sb.append(", ");

            sb.append(chord);
        }
        sb.append("]}");

    }

    public void monkeyWithEverything() {
        subbeatLength = 70 + rand.nextInt(125);

        mm.pickRandomKey();
        mm.pickRandomScale();
        makeChordProgression();

        mm.makeMotif();
        mm.makeMelodyFromMotif(beats);

        for (Channel channel : mChannels) {
            monkeyWithChannel(channel);
        }

        makeChannelNotes(dialpadChannel);

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

    void monkeyWithDrums(Channel channel) {

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



    public void monkeyWithChords() {

        makeChordProgression();
        //progressionI = 0;
    }



    public int[] getProgression() {
        return progression;
    }

    public int getChordInProgression() {
        return progressionI;
    }

    public int[] getScale() {
        return mm.getScaleArray();
    }

    public int getScaleIndex() {
        return mm.getScaleIndex();
    }

    public String getScaleString() {
        return mm.getScale();
    }

    public int getKey() {
        return mm.getKey() % 12;
    }

    public void setDrumset(int set) {
        //drumset = set;
        //setCaptions();
    }

    public void addInvalidateOnBeatListener(View view) {
        viewsToInvalidateOnBeat.add(view);
    }

    public void addInvalidateOnNewMeasureListener(View view) {
        viewsToInvalidateOnNewMeasure.add(view);
    }


    public void removeInvalidateOnBeatListener(View view)  {
        viewsToInvalidateOnBeat.remove(view);
    }


    public void setScale(String scale) {
        mm.setScale(scale);
    }
    public void setScale(int scaleI) {
        mm.setScale(scaleI);
    }
    public void setKey(int keyI) {
        mm.setKey(keyI);
    }

    private void playChannelBeat(Channel channel, double beat) {

        if (channel.getState() != Channel.STATE_PLAYBACK)
            return;

        int i = channel.getI();
        if (i <  channel.getNotes().size()) {
            if (channel.getNextBeat() == beat) {
                Note note = channel.getNotes().get(i);

                channel.playRecordedNote(note);
                channel.finishCurrentNoteAt(System.currentTimeMillis() +
                        (long)(note.getBeats() * 4 * subbeatLength) - 50);

                channel.setNextBeat(channel.getNextBeat() +  note.getBeats());

            }
        }
    }

    public void setBPM(float bpm) {
        subbeatLength = (int)((60000 / bpm) / subbeats);
        //bpm = 60000 / (subbeatLength * subbeats);
    }

    public void setSubbeatLength(int length) {
        subbeatLength = length;
    }

    public void setChordProgression(int[] chordProgression) {
        // ifeel like this should be negative one?
        progressionI = 0;
        progression = chordProgression;
        currentChord = progression[0];

        if (chordProgression.length == 1) {
            updateChord(currentChord);
        }
    }

    public int getBeats() {
        return beats;
    }

    public int getSubbeats() {
        return subbeats;
    }

    public int getTotalSubbeats() {
        return totalsubbeats;
    }

    public Random getRand() {
        return rand;
    }

    public boolean isSoundPoolInitialized() {
        return soundPoolInitialized;
    }


    public Channel getDialpadChannel() {
        return dialpadChannel;
    }

    public int getScaledNoteNumber(int basicNote) {
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

            }
            notes.add(newNote);
        }

    }

    private void load(Channel jamChannel, JSONObject part) throws  JSONException {
        String soundsetName = part.getString("soundsetName");
        String soundsetURL = part.getString("soundsetURL");

        jamChannel.prepareSoundSet(soundsetURL);
        jamChannel.loadSoundSet();

        if (part.has("volume")) {
            jamChannel.volume = (float)part.getDouble("volume");
        }
        //todo pan

        if (jamChannel.getSoundSet().isChromatic()) {
            loadMelody(jamChannel, part);
        }
        else {
            loadDrums(jamChannel, part);
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
}
