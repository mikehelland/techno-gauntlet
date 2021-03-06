package com.mikehelland.omgtechnogauntlet.jam;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SoundSet {

    private String mName = "";
    private String mURL = "";
    private long mID;
    private ArrayList<Sound> mSounds = new ArrayList<>();

    private boolean mChromatic = false;
    private int mHighNote;
    private int mLowNote;
    private int mRootNote;

    private String mPrefix = "";
    private String mPostfix = "";

    private boolean mIsValid = false;

    private boolean mIsOscillator = false;
    private Oscillator mOscillator = null;

    private String mDefaultSurface = "";

    private boolean mIsSoundFont = false;

    private String mJSON = "";

    SoundSet() {}

    public SoundSet(String url, long id, String json) {
        mURL = url;
        mID = id;

        mIsValid = loadFromJSON(json);
    }

    public SoundSet(long id, String name) {
        mName = name;
        mID = id;
    }

    public boolean isValid() {
        return mIsValid;
    }


    boolean load(JSONObject soundSet) throws JSONException {
        mIsValid = false;

        if (soundSet.has("url") && mURL.length() == 0) {
            mURL = soundSet.getString("url");
        }
        if (!soundSet.has("url") && mURL.length() > 0) {
            soundSet.put("url", mURL);
        }

        mJSON = soundSet.toString();

        String type = soundSet.getString("type");
        if (!"SOUNDSET".equals(type)) {
            Log.e("MGH", "soundset not type=SOUNDSET");
            return false;
        }

        mName = soundSet.getString("name");
        mIsSoundFont = soundSet.has("soundFont") && soundSet.getBoolean("soundFont");

        mChromatic = soundSet.has("chromatic") && soundSet.getBoolean("chromatic");
        if (soundSet.has("lowNote")) {
            mLowNote = soundSet.getInt("lowNote");
        }
        if (soundSet.has("highNote")) {
            mHighNote = soundSet.getInt("highNote");
        }

        if (soundSet.has("defaultSurface")) {
            mDefaultSurface = soundSet.getString("defaultSurface");
        }

        //todo should probably have an osc settings instead of read the url
        mIsOscillator = mURL != null && mURL.startsWith("PRESET_OSC_");
        if (mIsOscillator) {
            mOscillator = new Oscillator(new OscillatorSettings(mURL));
            mIsValid = true;
            return true;
        }

        JSONArray data = soundSet.getJSONArray("data");

        if (soundSet.has("prefix")) {
            mPrefix = soundSet.getString("prefix");
        }
        if (soundSet.has("postfix")) {
            mPostfix = soundSet.getString("postfix");
        }

        if (!mChromatic) {
            mLowNote = 0;
            mHighNote = data.length() - 1;
        }
        else if (!soundSet.has("highNote")) {
            mHighNote = mLowNote + data.length() - 1;
        }

        JSONObject soundJSON;
        Sound sound;

        for (int i = 0; i < data.length(); i++) {
            sound = new Sound();
            soundJSON = data.getJSONObject(i);

            sound.setSoundSetId(mID);
            sound.setSoundSetIndex(i);

            if (soundJSON.has("name")) {
                sound.setName(soundJSON.getString("name"));
            }

            sound.setURL(mPrefix + soundJSON.getString("url") + mPostfix);

            if (soundJSON.has("preset_id")) {
                sound.setPresetId((soundJSON.getInt("preset_id")));
            }

            mSounds.add(sound);
        }

        mIsValid = true;
        return true;
    }

    boolean loadFromJSON(String json) {
        mJSON = json;
        try {
            JSONObject soundSet = new JSONObject(json);
            mIsValid = load(soundSet);
        }
        catch (JSONException exp) {
            Log.e("MGH", exp.getMessage());
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
    String getURL() {
        return mURL;
    }
    void setName(String name) {
        mName = name;
    }
    void setURL(String url) {
        mURL = url;
    }

    public ArrayList<Sound> getSounds() {
        return mSounds;
    }

    //public void setID(long ID) {
    //    this.uuid = ID;
    //}
    public long getID() {
        return mID;
    }

    public String[] getSoundNames() {
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

    String getDefaultSurface() {
        return mDefaultSurface;
    }

    boolean isSoundFont() {
        return mIsSoundFont;
    }

    void setSoundFont(boolean value) {
        mIsSoundFont = value;
    }

    public class Sound {

        private String mName = "";
        private String mURL = "";
        private int preset_id = -1;
        private long soundset_id = -1;
        private int soundset_index = -1;

        public String getName() {
            return mName;
        }

        void setName(String mName) {
            this.mName = mName;
        }

        public String getURL() {
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

        long getSoundSetId() {
            return soundset_id;
        }

        void setSoundSetId(long id) {
            soundset_id = id;
        }

        void setSoundSetIndex(int index) {
            soundset_index = index;
        }

        int getSoundSetIndex() {
            return soundset_index;
        }
    }

    void getData(StringBuilder sb) {
        sb.append(mJSON);
        /*sb.append("{\"url\": \"");
        sb.append(mURL);
        sb.append("\", \"name\": \"");
        sb.append(mName);
        sb.append("\", \"soundFont\": ");
        sb.append(mIsSoundFont);
        sb.append(", \"data\": ");
        sb.append(mJSON);
        sb.append("}");*/
    }
}
