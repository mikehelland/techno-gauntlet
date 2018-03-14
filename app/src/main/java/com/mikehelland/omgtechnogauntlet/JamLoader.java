package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by m on 2/28/18.
 * take the loading out of the jam, because it needs access to the database
 */

class JamLoader {

    static Jam load(String json, final Main context) throws Exception {

        if (context == null) {
            return null;
        }

        Jam jam = new Jam(new MelodyMaker(context), context.mPool, getAppName(context));
        DatabaseContainer dbc = context.getDatabase();

        try {

            JSONObject jsonData = new JSONObject(json);

            JSONArray parts;
            parts = jsonData.getJSONArray("parts");

            if (jsonData.has("measures")) {
                jam.setMeasures(jsonData.getInt("measures"));
                if (jsonData.has("beats")) {
                    jam.setBeats(jsonData.getInt("beats"));
                }
                if (jsonData.has("subbeats")) {
                    jam.setSubbeats(jsonData.getInt("subbeats"));
                }
            }

            if (jsonData.has("subbeatMillis")) {
                jam.setSubbeatLength(jsonData.getInt("subbeatMillis"));
            }

            if (jsonData.has("shuffle")) {
                jam.setShuffle((float)jsonData.getDouble("shuffle"));
            }

            if (jsonData.has("rootNote")) {
                jam.setKey(jsonData.getInt("rootNote") % 12);
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
                jam.setChordProgression(newChords);
            }

            if (jsonData.has("tags")) {
                jam.setTags(jsonData.getString("tags"));
            }

            Channel channel;

            for (int ip = 0; ip < parts.length(); ip++) {
                JSONObject part = parts.getJSONObject(ip);

                String soundsetURL = part.getString("soundsetURL");

                final SoundSetDataOpenHelper dataHelper = dbc.getSoundSetData();
                SoundSet soundSet = dataHelper.getSoundSetByURL(soundsetURL);
                if (soundSet != null) {
                    channel = new Channel(jam, context.mPool);
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

    static private void loadPart(Channel jamChannel, JSONObject part) throws  JSONException {

        if (part.has("surface")) {
            JSONObject surfaceJSONObject = part.getJSONObject("surface");
            jamChannel.setSurface(parseSurfaceJSONObject(surfaceJSONObject));
        }
        else if (part.has("surfaceURL")) { //the old way
            String surfaceURL = part.getString("surfaceURL");
            jamChannel.setSurface(new Surface(surfaceURL));
        }

        if (part.has("volume")) {
            jamChannel.setVolume((float)part.getDouble("volume"));
        }
        if (part.has("pan")) {
            jamChannel.setPan((float)part.getDouble("pan"));
        }
        if (part.has("sampleSpeed")) {
            jamChannel.setSampleSpeed((float)part.getDouble("sampleSpeed"));
        }

        if (jamChannel.useSequencer()) {
            loadDrums(jamChannel, part);
        }
        else {
            loadMelody(jamChannel, part);
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

    private static Surface parseSurfaceJSONObject(JSONObject jsonObject) throws JSONException{
        final Surface surface = new Surface();
        surface.setName(jsonObject.getString("name"));
        surface.setURL(jsonObject.getString("url"));
        if (jsonObject.has("zoomSkipBottom") && jsonObject.has("zoomSkipTop")) {
            surface.setSkipBottomAndTop(jsonObject.getInt("zoomSkipBottom"),
                    jsonObject.getInt("zoomSkipTop"));
        }
        return  surface;
    }
}
