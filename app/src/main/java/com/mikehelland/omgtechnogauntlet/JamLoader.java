package com.mikehelland.omgtechnogauntlet;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JamLoader {

    private Jam mJam;

    public JamLoader(Jam jam) {
        mJam = jam;
    }

    public boolean loadData(String data) {

        boolean good = false;
        try {

            JSONObject jsonData = new JSONObject(data);

            JSONArray parts;
            if (jsonData.has("parts")) {
                parts = jsonData.getJSONArray("parts");
            } else {
                parts = jsonData.getJSONArray("data");
            }

            if (jsonData.has("subbeatMillis")) {
                mJam.setSubbeatLength(jsonData.getInt("subbeatMillis"));
            }

            if (jsonData.has("rootNote")) {
                mJam.setKey(jsonData.getInt("rootNote") % 12);
            }

            if (jsonData.has("scale")) {
                mJam.setScale(jsonData.getString("scale"));
            }


            for (int ip = 0; ip < parts.length(); ip++) {
                JSONObject part = parts.getJSONObject(ip);
                String type = part.getString("type");

                if ("DRUMBEAT".equals(type)) {

                    if ("PRESET_PERCUSSION_SAMPLER".equals(part.getString("kit"))) {
                        loadDrums(mJam.getSamplerChannel(), part);
                    } else {
                        loadDrums(mJam.getDrumChannel(), part);

                    }
                } else if ("MELODY".equals(type)) {
                    if ("PRESET_SYNTH1".equals(part.getString("sound"))) {
                        loadMelody(mJam.getSynthChannel(), part);
                    } else if ("PRESET_GUITAR1".equals(part.getString("sound"))) {
                        loadMelody(mJam.getGuitarChannel(), part);
                    }
                    else if ("DIALPAD_SINE_DELAY".equals(part.getString(("sound")))) {
                        loadMelody(mJam.getDialpadChannel(), part);
                    }
                } else if ("BASSLINE".equals(type)) {
                    loadMelody(mJam.getBassChannel(), part);

                } else if ("CHORDPROGRESSION".equals(type)) {
                    JSONArray chordsData = part.getJSONArray("data");
                    int[] newChords = new int[chordsData.length()];
                    for (int ic = 0; ic < chordsData.length(); ic++) {
                        newChords[ic] = chordsData.getInt(ic);
                    }
                    mJam.setChordProgression(newChords);

                }

            }

            mJam.onNewLoop();

            good = true;

        } catch (JSONException e) {
            Log.d("MGH loaddata exception", e.getMessage());
            e.printStackTrace();
        }

        return good;
    }

    private void loadDrums(DrumChannel jamChannel, JSONObject part) throws JSONException {

        //todo    drumset = jsonData.getInt("kit");


        JSONArray tracks;
        if (part.has("tracks")) {
            tracks = part.getJSONArray("tracks");
        }
        else {
            //backwards compat
            tracks = part.getJSONArray("data");
        }
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
}