package com.mikehelland.omgtechnogauntlet;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class JamLoader {

    private Jam mJam;

    JamLoader(Jam jam) {
        mJam = jam;
    }

    boolean loadData(String data) {

        boolean good = false;
        try {

            JSONObject jsonData = new JSONObject(data);

            JSONArray parts;
            parts = jsonData.getJSONArray("parts");

            if (jsonData.has("subbeatMillis")) {
                mJam.setSubbeatLength(jsonData.getInt("subbeatMillis"));
            }

            if (jsonData.has("rootNote")) {
                mJam.setKey(jsonData.getInt("rootNote") % 12);
            }

            if (jsonData.has("scale")) {
                mJam.setScale(jsonData.getString("scale"));
            }


            Channel channel;
            for (int ip = 0; ip < parts.length(); ip++) {
                JSONObject part = parts.getJSONObject(ip);
                String type = part.getString("type");

                if ("CHORDPROGRESSION".equals(type)) {
                    JSONArray chordsData = part.getJSONArray("data");
                    int[] newChords = new int[chordsData.length()];
                    for (int ic = 0; ic < chordsData.length(); ic++) {
                        newChords[ic] = chordsData.getInt(ic);
                    }
                    mJam.setChordProgression(newChords);
                    continue;
                }

                Log.d("MGH loadData()", part.toString(4));
                String soundsetURL = part.getString("soundsetURL");

                if ("MELODY".equals(type)) {
                    if ("PRESET_SYNTH1".equals(soundsetURL)) {
                        loadMelody(mJam.getSynthChannel(), part);
                    } else if ("PRESET_GUITAR1".equals(soundsetURL)) {
                        loadMelody(mJam.getGuitarChannel(), part);
                    }
                    else if ("DIALPAD_SINE_DELAY".equals(soundsetURL)) {
                        loadMelody(mJam.getDialpadChannel(), part);
                    }
                    continue;
                } else if ("BASSLINE".equals(type)) {
                    loadMelody(mJam.getBassChannel(), part);
                    continue;
                }


                if ("DRUMBEAT".equals(type)) {

                    if ("PRESET_PERCUSSION_SAMPLER".equals(soundsetURL)) {
                        loadDrums(mJam.getSamplerChannel(), part);
                    } else {
                        loadDrums(mJam.getDrumChannel(), part);

                    }
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

        String soundsetName = part.getString("soundsetName");
        String soundsetURL = part.getString("soundsetURL");

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