package com.mikehelland.omgtechnogauntlet;

import android.database.Cursor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by m on 7/9/17.
 */

public class SoundSet {

    private String mName;
    private String mURL;
    private long mID;
    private ArrayList<Sound> mSounds = new ArrayList<Sound>();

    private boolean mChromatic = false;
    private int mHighNote;
    private int mLowNote;

    private boolean mIsValid = false;

    public SoundSet() {}

    public SoundSet(Cursor cursor) {
        mName = cursor.getString(cursor.getColumnIndex("name"));
        mURL = cursor.getString(cursor.getColumnIndex("url"));
        mID = cursor.getLong(cursor.getColumnIndex("_id"));

        mIsValid = loadFromJSON(cursor.getString(cursor.getColumnIndex("data")));
    }

    boolean isValid() {
        return mIsValid;
    }

    public boolean loadFromJSON(String json) {
        try {
            JSONObject soundset = new JSONObject(json);

            String type = soundset.getString("type");
            if (!"SOUNDSET".equals(type)) {
                Log.e("MGH", "soundset not type=SOUNDSET");
                mIsValid = false;
                return false;
            }

            mName = soundset.getString("name");
            mChromatic = soundset.has("chromatic") && soundset.getBoolean("chromatic");
            if (soundset.has("highNote") && soundset.has("lowNote")) {
                mHighNote = soundset.getInt("highNote");
                mLowNote = soundset.getInt("lowNote");
            }

            JSONArray data = soundset.getJSONArray("data");

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

    public boolean isChromatic() {
        return mChromatic;
    }
    public int getHighNote() {
        return mHighNote;
    }
    public int getLowNote() {
        return mLowNote;
    }

    public String getName() {
        return mName;
    }
    public String getURL() {
        return mURL;
    }
    public void setName(String name) {
        mName = name;
    }
    public void setURL(String url) {
        mURL = url;
    }

    public ArrayList<Sound> getSounds() {
        return mSounds;
    }

    public void setID(long ID) {
        this.mID = ID;
    }
    public long getID() {
        return mID;
    }

    public void setChromatic(boolean b) {
        mChromatic = b;
    }

    public String[] getSoundNames() {
        String[] names = new String[mSounds.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = mSounds.get(i).getName();
        }
        return names;
    }

    class Sound {

        private String mName;
        private String mURL;
        private int preset_id = -1;

        public String getName() {
            return mName;
        }

        public void setName(String mName) {
            this.mName = mName;
        }

        public String getURL() {
            return mURL;
        }

        public void setURL(String mURL) {
            this.mURL = mURL;
        }

        public boolean isPreset() {
            return preset_id > 0;
        }

        public int getPresetId() {
            return preset_id;
        }

        public void setPresetId(int id) {
            preset_id = id;
        }
    }

}
