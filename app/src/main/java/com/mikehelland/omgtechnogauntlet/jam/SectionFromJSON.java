package com.mikehelland.omgtechnogauntlet.jam;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.mikehelland.omgtechnogauntlet.R;
import com.mikehelland.omgtechnogauntlet.SoundSetDataOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by m on 2/28/18.
 * take the loading out of the jam, because it needs access to the database
 */

class SectionFromJSON {

    static Section fromOMG(String json) throws Exception {

        Section jam = new Section();

        try {

            JSONObject jsonData = new JSONObject(json);

            jam.beatParameters = loadBeatParameters();
            jam.keyParameters = loadKeyParameters();

            JSONArray parts;
            parts = jsonData.getJSONArray("parts");

            if (jsonData.has("measures")) {
                jam.beatParameters.measures = jsonData.getInt("measures");
            }
            if (jsonData.has("beats")) {
                jam.beatParameters.beats = jsonData.getInt("beats");
            }
            if (jsonData.has("subbeats")) {
                jam.beatParameters.subbeats = jsonData.getInt("subbeats");
            }

            if (jsonData.has("subbeatMillis")) {
                jam.beatParameters.subbeatLength = jsonData.getInt("subbeatMillis");
            }

            if (jsonData.has("shuffle")) {
                jam.beatParameters.shuffle = (float)jsonData.getDouble("shuffle");
            }

            if (jsonData.has("rootNote")) {
                jam.keyParameters.rootNote = jsonData.getInt("rootNote") % 12;
            }

            if (jsonData.has("scale")) {
                jam.setScale(jsonData.getString("scale"));
            }

            if (jsonData.has("chordProgression")) {
                JSONArray chordsData = jsonData.getJSONArray("chordProgression");
                int[] newChords = new int[chordsData.length()];
                for (int ic = 0; ic < chordsData.length(); ic++) {
                    newChords[ic] = chordsData.getInt(ic);
                }
                jam.progression = newChords;
            }

            if (jsonData.has("tags")) {
                jam.tags = jsonData.getString("tags");
            }

            Part channel;

            for (int ip = 0; ip < parts.length(); ip++) {
                JSONObject part = parts.getJSONObject(ip);

                String soundsetURL = part.getString("soundsetURL");

                final SoundSetDataOpenHelper dataHelper = dbc.getSoundSetData();
                SoundSet soundSet = dataHelper.getSoundSetByURL(soundsetURL);
                if (soundSet != null) {
                    channel = new Part(jam, context.mPool);
                    channel.prepareSoundSet(soundSet);
                    loadPart(channel, part);
                    jam.getChannels().add(channel);
                }
                else {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, dataHelper.getLastErrorMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            //todo jam.onNewLoop(System.currentTimeMillis());

        } catch (final JSONException e) {
            Log.e("MGH loaddata exception", e.getMessage());
            throw new Exception("Could not load data: " + e.getMessage());
        }

        return jam;
    }

    static private void loadPart(Part part, JSONObject partJSON) throws  JSONException {

        if (partJSON.has("surface")) {
            JSONObject surfaceJSONObject = partJSON.getJSONObject("surface");
            part.surface = loadSurface(surfaceJSONObject);
        }
        else if (partJSON.has("surfaceURL")) { //the old way
            String surfaceURL = partJSON.getString("surfaceURL");
            part.surface = new Surface(surfaceURL);
        }

        if (partJSON.has("volume")) {
            part.setVolume((float)partJSON.getDouble("volume"));
        }
        if (partJSON.has("pan")) {
            part.setPan((float)partJSON.getDouble("pan"));
        }
        if (partJSON.has("sampleSpeed")) {
            part.setSampleSpeed((float)partJSON.getDouble("sampleSpeed"));
        }

        if (part.useSequencer()) {
            loadDrums(part, partJSON);
        }
        else {
            loadMelody(part, partJSON);
        }
    }

    static private void loadDrums(Channel jamChannel, JSONObject part) throws JSONException {

        JSONArray tracks = part.getJSONArray("tracks");

        JSONObject track;
        JSONArray trackData;

        boolean[][] pattern = jamChannel.pattern;

        if (part.has("volume")) {
            jamChannel.setVolume((float)part.getDouble("volume"));
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

            if (i < jamChannel.getPatternInfo().getTracks().size()) {
                if (track.has("mute") && track.getBoolean("mute")) {
                    jamChannel.getPatternInfo().getTrack(i).setMute(true);
                }
                if (track.has("volume")) {
                    jamChannel.getPatternInfo().getTrack(i).setVolume((float)track.getDouble("volume"));
                }
                if (track.has("pan")) {
                    jamChannel.getPatternInfo().getTrack(i).setPan((float)track.getDouble("pan"));
                }
            }

            trackData = track.getJSONArray("data");

            for (int j = 0; j < trackData.length(); j++) {
                if (i < pattern.length && j < pattern[i].length)
                    pattern[i][j] = trackData.getInt(j) == 1;
            }

        }
    }

    static private void loadMelody(Channel channel, JSONObject part) throws JSONException {

        NoteList notes = channel.getNotes();
        notes.clear();

        if (part.has("volume")) {
            channel.setVolume((float)part.getDouble("volume"));
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

    private static Surface loadSurface(JSONObject jsonObject) throws JSONException{
        final Surface surface = new Surface();
        surface.setName(jsonObject.getString("name"));
        surface.setURL(jsonObject.getString("url"));
        if (jsonObject.has("skipBottom") && jsonObject.has("skipTop")) {
            surface.setSkipBottomAndTop(jsonObject.getInt("skipBottom"),
                    jsonObject.getInt("skipTop"));
        }
        return  surface;
    }

    private static BeatParameters loadBeatParameters(JSONObject jsonObject) {

    }
    private static KeyParameters loadKeyParameters(JSONObject jsonObject) {

    }
}
