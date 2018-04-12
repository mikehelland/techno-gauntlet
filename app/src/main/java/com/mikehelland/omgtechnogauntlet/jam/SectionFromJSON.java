package com.mikehelland.omgtechnogauntlet.jam;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.mikehelland.omgtechnogauntlet.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by m on 2/28/18.
 * load up a Section model from JSON
 */

class SectionFromJSON {

    static Section fromJSON(String json) throws Exception {

        Section section = new Section();

        try {

            JSONObject jsonData = new JSONObject(json);

            if (jsonData.has("tags")) {
                section.tags = jsonData.getString("tags");
            }

            section.beatParameters = loadBeatParameters(jsonData);
            section.keyParameters = loadKeyParameters(jsonData);

            section.progression = loadChordProgession(jsonData);

            JSONArray parts;
            parts = jsonData.getJSONArray("parts");

            Part part;

            for (int ip = 0; ip < parts.length(); ip++) {
                JSONObject partJSON = parts.getJSONObject(ip);

                part = new Part(section);
                loadPart(part, partJSON);
                section.parts.add(part);
            }
        } catch (final JSONException e) {
            Log.e("MGH loaddata exception", e.getMessage());
            throw new Exception("Could not load data: " + e.getMessage());
        }

        return section;
    }

    static private void loadPart(Part part, JSONObject partJSON) throws  JSONException {

        part.surface = loadSurface(partJSON);
        part.soundSet = loadSoundSet(partJSON);

        part.audioParameters = loadAudioParameters(partJSON);

        if (part.surface.getURL().equals(Surface.PRESET_SEQUENCER)) {
            loadDrums(part, partJSON);
        }
        else {
            loadMelody(part, partJSON);
        }
    }

    static private void loadDrums(Part part, JSONObject partJSON) throws JSONException {

        JSONArray tracks = partJSON.getJSONArray("tracks");

        JSONObject trackJSON;
        JSONArray trackData;
        SequencerTrack track;
        boolean[][] pattern = new boolean[tracks.length()][];

        //underrun overrun?
        //match the right channels?
        // this assumes things are in the right order
        //todo right, the assumption is the tracks array has the same 1-to-1 as the soundset, fix it?

        for (int i = 0; i < tracks.length(); i++) {
            trackJSON = tracks.getJSONObject(i);

            track = new SequencerTrack("todo"); //todo
            track.audioParameters = loadAudioParameters(trackJSON);
            part.sequencerPattern.getTracks().add(track);
            pattern[i] = track.getData();
            part.pattern = pattern;

            trackData = trackJSON.getJSONArray("data");

            for (int j = 0; j < trackData.length(); j++) {
                if (i < pattern.length && j < pattern[i].length)
                    pattern[i][j] = trackData.getInt(j) == 1;
            }

        }
    }

    static private void loadMelody(Part part, JSONObject jsonData) throws JSONException {

        NoteList notes = part.notes;
        notes.clear();

        JSONArray notesData = jsonData.getJSONArray("notes");

        Note newNote;
        JSONObject noteData;

        for (int i = 0; i < notesData.length(); i++) {
            noteData = notesData.getJSONObject(i);

            newNote = new Note();
            newNote.setBeats(noteData.getDouble("beats"));

            newNote.setRest(noteData.getBoolean("rest"));

            if (!newNote.isRest()) {
                newNote.setBasicNote(noteData.getInt("note"));
                if (!part.soundSet.isChromatic()) {
                    newNote.setScaledNote(newNote.getBasicNote());
                    newNote.setInstrumentNote(newNote.getBasicNote());
                }
            }
            notes.add(newNote);
        }

    }

    private static String getAppName(Context context) {
        String appName = context.getResources().getString(R.string.app_name);
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appName = appName + " "  + pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }

    private static Surface loadSurface(JSONObject jsonData) throws JSONException{
        final Surface surface = new Surface();

        if (jsonData.has("surface")) {
            JSONObject surfaceJSONObject = jsonData.getJSONObject("surface");

            surface.setName(surfaceJSONObject.getString("name"));
            surface.setURL(surfaceJSONObject.getString("url"));
            if (jsonData.has("skipBottom") && jsonData.has("skipTop")) {
                surface.setSkipBottomAndTop(jsonData.getInt("skipBottom"),
                        jsonData.getInt("skipTop"));
            }
        }
        else if (jsonData.has("surfaceURL")) { //the old way
            String surfaceURL = jsonData.getString("surfaceURL");
            surface.setURL(surfaceURL);
        }

        return  surface;
    }

    private static SoundSet loadSoundSet(JSONObject jsonData) throws JSONException{
        final SoundSet soundSet = new SoundSet();

        JSONObject jsonObject = jsonData;
        if (jsonData.has("soundSet")) {
            jsonObject = jsonData.getJSONObject("soundSet");

            if (jsonObject.has("name")) {
                soundSet.setName(jsonObject.getString("name"));
            }
            if (jsonObject.has("url")) {
                soundSet.setURL(jsonObject.getString("url"));
            }
        }
        else { // the old way
            if (jsonData.has("soundsetURL")) { //the old way
                soundSet.setURL(jsonData.getString("soundsetURL"));
            }
            if (jsonData.has("soundsetName")) { //the old way
                soundSet.setName(jsonData.getString("soundsetName"));
            }
        }
        if (jsonObject.has("soundFont")) {
            soundSet.setSoundFont(jsonObject.getBoolean("soundFont"));
        }

        return  soundSet;
    }

    private static BeatParameters loadBeatParameters(JSONObject jsonData) throws JSONException {
        BeatParameters beatParameters = new BeatParameters();

        if (jsonData.has("beatParameters")) {
            jsonData = jsonData.getJSONObject("beatParameters");
        }

        if (jsonData.has("measures")) {
            beatParameters.measures = jsonData.getInt("measures");
        }
        if (jsonData.has("beats")) {
            beatParameters.beats = jsonData.getInt("beats");
        }
        if (jsonData.has("subbeats")) {
            beatParameters.subbeats = jsonData.getInt("subbeats");
        }
        if (jsonData.has("subbeatMillis")) {
            beatParameters.subbeatLength = jsonData.getInt("subbeatMillis");
        }
        if (jsonData.has("shuffle")) {
            beatParameters.shuffle = (float)jsonData.getDouble("shuffle");
        }
        return beatParameters;
    }

    private static KeyParameters loadKeyParameters(JSONObject jsonData) throws JSONException {
        KeyParameters keyParameters = new KeyParameters();
        if (jsonData.has("keyParameters")) {
            jsonData = jsonData.getJSONObject("keyParameters");
        }

        if (jsonData.has("rootNote")) {
            keyParameters.rootNote = jsonData.getInt("rootNote") % 12;
        }

        JSONArray scaleJSON = null;
        if (jsonData.has("ascale")) { // the old way
            scaleJSON = jsonData.getJSONArray("ascale");
        }
        else if (jsonData.has("scale")) {
            scaleJSON = jsonData.getJSONArray("scale");
        }
        if (scaleJSON != null) {
            int[] scale = new int[scaleJSON.length()];
            for (int i = 0; i < scaleJSON.length(); i++) {
                scale[i] = scaleJSON.getInt(i);
            }
            keyParameters.scale = scale;
        }
        return keyParameters;
    }

    private static AudioParameters loadAudioParameters(JSONObject jsonData) throws JSONException{
        AudioParameters audioParameters = new AudioParameters();

        if (jsonData.has("audioParameters")) {
            jsonData = jsonData.getJSONObject("audioParameters");
        }

        if (jsonData.has("mute")) {
            audioParameters.mute = jsonData.getBoolean("mute");
        }
        if (jsonData.has("volume")) {
            audioParameters.volume = (float)jsonData.getDouble("volume");
        }
        if (jsonData.has("pan")) {
            audioParameters.pan = (float)jsonData.getDouble("pan");
        }
        if (jsonData.has("sampleSpeed")) {
            audioParameters.speed = (float)jsonData.getDouble("sampleSpeed");
        }
        return audioParameters;
    }

    private static int[] loadChordProgession(JSONObject jsonData) throws JSONException {
        int[] chords = new int[] {0};
        if (jsonData.has("chordProgression")) {
            JSONArray chordsData = jsonData.getJSONArray("chordProgression");
            chords = new int[chordsData.length()];
            for (int ic = 0; ic < chordsData.length(); ic++) {
                chords[ic] = chordsData.getInt(ic);
            }
        }
        return chords;
    }
}
