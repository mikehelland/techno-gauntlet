package com.mikehelland.omgtechnogauntlet.jam;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by m on 4/29/18.
 */

public class JamLoader {
    static Song load(String json) throws JamLoaderException {

        Song song = new Song();
        try {
            JSONObject jsonData = new JSONObject(json);

            //check to see if it has a type and if it song or section
            //section is legacy/deprecated in this app
            if (!jsonData.has("type")) {
                throw new JamLoaderException("JSON data has no TYPE field.");
            }

            String type = jsonData.getString("type");
            if ("SONG".equals(type)) {
                //song = SongFromJSON.fromJSON(json);
            }
            else if ("SECTION".equals(type)) {
                //this is what was saved before the rearachitiverture of early 2018
                //before then it only saved sections, not songs
                //song = new Song();
                //song.sections.add(SectionFromJSON.fromJSON(json));
                song.sections.add(SectionFromJSON.fromJSON(jsonData));
            }
            else {
                throw new JamLoaderException("Unsupported type has no TYPE: " + type);
            }

            if (song.sections.size() < 1) {
                throw new JamLoaderException("There are no sections to play");
            }

            if (jsonData.has("tags")) {
                song.tags = jsonData.getString("tags");
            }

            song.beatParameters = loadBeatParameters(jsonData);
            song.keyParameters = loadKeyParameters(jsonData);

        } catch (final JSONException e) {
            Log.e("MGH loaddata exception", e.getMessage());
            throw new JamLoaderException("Could not load data: " + e.getMessage());
        }

        return song;
    }


    static BeatParameters loadBeatParameters(JSONObject jsonData) throws JSONException {
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

    static KeyParameters loadKeyParameters(JSONObject jsonData) throws JSONException {
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
}
