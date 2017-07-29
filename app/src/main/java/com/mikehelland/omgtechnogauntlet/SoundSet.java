package com.mikehelland.omgtechnogauntlet;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class SoundSet {

    private String mName;
    private String mURL;
    private long mID;
    private ArrayList<Sound> mSounds = new ArrayList<>();

    private boolean mChromatic = false;
    private int mHighNote;
    private int mLowNote;

    private boolean mIsValid = false;

    private boolean mIsOscillator = false;
    private Oscillator mOscillator = null;

    SoundSet() {}

    SoundSet(Cursor cursor) {
        mName = cursor.getString(cursor.getColumnIndex("name"));
        mURL = cursor.getString(cursor.getColumnIndex("url"));
        mID = cursor.getLong(cursor.getColumnIndex("_id"));

        mIsValid = loadFromJSON(cursor.getString(cursor.getColumnIndex("data")));
    }

    SoundSet(ContentValues data) {
        mName = (String)data.get("name");
        mURL = (String)data.get("url");
        mID = (long)data.get("_id");

        mIsValid = loadFromJSON((String)data.get("data"));
    }

    boolean isValid() {
        return mIsValid;
    }

    private boolean loadFromJSON(String json) {
        try {
            JSONObject soundSet = new JSONObject(json);

            String type = soundSet.getString("type");
            if (!"SOUNDSET".equals(type)) {
                Log.e("MGH", "soundset not type=SOUNDSET");
                mIsValid = false;
                return false;
            }

            mName = soundSet.getString("name");
            mChromatic = soundSet.has("chromatic") && soundSet.getBoolean("chromatic");
            if (soundSet.has("highNote") && soundSet.has("lowNote")) {
                mHighNote = soundSet.getInt("highNote");
                mLowNote = soundSet.getInt("lowNote");
            }

            mIsOscillator = mURL.startsWith("PRESET_OSC_");
            if (mIsOscillator) {
                mOscillator = new Oscillator(new OscillatorSettings(mURL));
                return true;
            }

            JSONArray data = soundSet.getJSONArray("data");

            JSONObject soundJSON;
            Sound sound;

            for (int i = 0; i < data.length(); i++) {
                sound = new Sound();
                soundJSON = data.getJSONObject(i);

                if (soundJSON.has("name")) {
                    sound.setName(soundJSON.getString("name"));
                }

                sound.setURL(soundJSON.getString("url"));

                if (soundJSON.has("preset_id")) {
                    sound.setPresetId((soundJSON.getInt("preset_id")));
                }

                mSounds.add(sound);
            }

        }
        catch (JSONException exp) {
            Log.d("MGH", exp.getMessage());
            mIsValid = false;
            return false;
        }

        return true;
    }

    boolean isChromatic() {
        return mChromatic;
    }
    int getHighNote() {
        return mHighNote;
    }
    int getLowNote() {
        return mLowNote;
    }

    String getName() {
        return mName;
    }
    String getURL() {
        return mURL;
    }
    void setName(String name) {
        mName = name;
    }
    void setURL(String url) {
        mURL = url;
    }

    ArrayList<Sound> getSounds() {
        return mSounds;
    }

    //public void setID(long ID) {
    //    this.mID = ID;
    //}
    long getID() {
        return mID;
    }

    public void setChromatic(boolean b) {
        mChromatic = b;
    }

    String[] getSoundNames() {
        String[] names = new String[mSounds.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = mSounds.get(i).getName();
        }
        return names;
    }

    boolean isOscillator() {
        return mIsOscillator;
    }

    Oscillator getOscillator() {
        return mOscillator;
    }

    class Sound {

        private String mName = "";
        private String mURL = "";
        private int preset_id = -1;

        String getName() {
            return mName;
        }

        void setName(String mName) {
            this.mName = mName;
        }

        String getURL() {
            return mURL;
        }

        void setURL(String mURL) {
            this.mURL = mURL;
        }

        boolean isPreset() {
            return preset_id > 0;
        }

        int getPresetId() {
            return preset_id;
        }

        void setPresetId(int id) {
            preset_id = id;
        }
    }

}
