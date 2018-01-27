package com.mikehelland.omgtechnogauntlet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

class SoundSetDataOpenHelper extends SQLiteOpenHelper {

    private String cErrorMessage = "";
    private SQLiteDatabase mDB;

    private Context mContext;
    SoundSetDataOpenHelper(Context context) {
        super(context, "OMG_TECHNO_GAUNTLET", null, 2);
        mContext = context;

        mDB = getWritableDatabase();
    }

    void updatePresetResource() {
        SQLiteDatabase db = mDB;
        setupBassSoundSet(db);
        setupSlapBassSoundSet(db);
        setupSamplerSoundSet(db);
        setupRockDrumsSoundSet(db);
        setupHipHopDrumsSoundSet(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE soundsets (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, data TEXT, url TEXT, type TEXT, chromatic BOOLEAN, time INTEGER, omg_id TEXT)");
        setupDefaultSoundSets(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            Log.d("MGH datahelper", "UPGRADE!");
            db.execSQL("ALTER TABLE soundsets ADD COLUMN downloaded INTEGER");
            db.execSQL("UPDATE soundsets SET downloaded = 1");
        }
    }

    private void setupDefaultSoundSets(SQLiteDatabase db) {

        setupOscillatorSoundSet(db);
        setupBassSoundSet(db);
        setupSlapBassSoundSet(db);
        setupSamplerSoundSet(db);
        setupRockDrumsSoundSet(db);
        setupHipHopDrumsSoundSet(db);

    }

    private void setupSamplerSoundSet(SQLiteDatabase db) {
        String json = SamplerChannel.getDefaultSoundSetJson();

        ContentValues data = new ContentValues();
        data.put("name", "Percussion Sampler");
        data.put("url", "PRESET_PERCUSSION_SAMPLER");
        data.put("omg_id", "PRESET_PERCUSSION_SAMPLER");
        data.put("type", "DRUMBEAT");
        data.put("chromatic", false);
        data.put("data", json);
        data.put("time", System.currentTimeMillis() / 1000);
        data.put("downloaded", 1);

        String[] columns = new String[]{"_id"};
        String[] urls = new String[]{data.getAsString("url")};
        Cursor cursor = db.query("soundsets", columns, "url=?", urls, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            long id = cursor.getLong(0);
            db.update("soundsets", data, "_id=" + id, null);
        }
        else {
            db.insert("soundsets", null, data);
        }
        cursor.close();
    }

    private void setupRockDrumsSoundSet(SQLiteDatabase db) {
        String json = RockDrumChannel.getDefaultSoundSetJson();

        ContentValues data = new ContentValues();
        data.put("name", "Rock Drum Kit");
        data.put("url", "PRESET_ROCKKIT");
        data.put("omg_id", "PRESET_ROCKKIT");
        data.put("type", "DRUMBEAT");
        data.put("chromatic", false);
        data.put("data", json);
        data.put("time", System.currentTimeMillis() / 1000);
        data.put("downloaded", 1);

        String[] columns = new String[]{"_id"};
        String[] urls = new String[]{data.getAsString("url")};
        Cursor cursor = db.query("soundsets", columns, "url=?", urls, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            long id = cursor.getLong(0);
            db.update("soundsets", data, "_id=" + id, null);
        }
        else {
            db.insert("soundsets", null, data);
        }
        cursor.close();
    }

    private void setupHipHopDrumsSoundSet(SQLiteDatabase db) {
        String json = HipDrumChannel.getDefaultSoundSetJson();

        ContentValues data = new ContentValues();
        data.put("name", "Hip Hop Drum Kit");
        data.put("url", "PRESET_HIPKIT");
        data.put("omg_id", "PRESET_HIPKIT");
        data.put("type", "DRUMBEAT");
        data.put("chromatic", false);
        data.put("data", json);
        data.put("time", System.currentTimeMillis() / 1000);
        data.put("downloaded", 1);

        String[] columns = new String[]{"_id"};
        String[] urls = new String[]{data.getAsString("url")};
        Cursor cursor = db.query("soundsets", columns, "url=?", urls, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            long id = cursor.getLong(0);
            db.update("soundsets", data, "_id=" + id, null);
        }
        else {
            db.insert("soundsets", null, data);
        }
        cursor.close();
    }

    private void setupBassSoundSet(SQLiteDatabase db) {
        String json = BassSamplerChannel.getDefaultSoundSetJSON(mContext);

        ContentValues data = new ContentValues();
        data.put("name", "Electric Bass");
        data.put("url", "PRESET_BASS");
        data.put("omg_id", "PRESET_BASS");
        data.put("type", "BASSLINE");
        data.put("chromatic", true);
        data.put("data", json);
        data.put("time", System.currentTimeMillis() / 1000);
        data.put("downloaded", 1);

        String[] columns = new String[]{"_id"};
        String[] urls = new String[]{data.getAsString("url")};
        Cursor cursor = db.query("soundsets", columns, "url=?", urls, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            long id = cursor.getLong(0);
            db.update("soundsets", data, "_id=" + id, null);
        }
        else {
            db.insert("soundsets", null, data);
        }
        cursor.close();
    }

    private void setupSlapBassSoundSet(SQLiteDatabase db) {
        String json = BassSamplerChannel.getSlapSoundSetJSON(mContext);

        ContentValues data = new ContentValues();
        data.put("name", "Slap Bass");
        data.put("url", "http://openmusic.gallery/data/413");
        data.put("omg_id", "PRESET_SLAP_BASS");
        data.put("type", "BASSLINE");
        data.put("chromatic", true);
        data.put("data", json);
        data.put("time", System.currentTimeMillis() / 1000);
        data.put("downloaded", 1);

        String[] columns = new String[]{"_id"};
        String[] urls = new String[]{data.getAsString("url")};
        Cursor cursor = db.query("soundsets", columns, "url=?", urls, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            long id = cursor.getLong(0);
            db.update("soundsets", data, "_id=" + id, null);
        }
        else {
            db.insert("soundsets", null, data);
        }
        cursor.close();
    }

    private void setupOscillatorSoundSet(SQLiteDatabase db) {

        String[] oscs = new String[11];
        oscs[10] = "{\"type\" : \"SOUNDSET\", \"chromatic\": true, \"name\": \"" +
                "Osc Soft Sine Delay\", \"url\": \"PRESET_OSC_SINE_SOFT_DELAY\", " +
                "\"highNote\": 108, \"lowNote\": 0, \"octave\": 5}";

        oscs[9] = "{\"type\" : \"SOUNDSET\", \"chromatic\": true, \"name\": \"" +
                "Osc Sine\", \"url\": \"PRESET_OSC_SINE\", " +
                "\"highNote\": 108, \"lowNote\": 0, \"octave\": 5}";

        oscs[8] = "{\"type\" : \"SOUNDSET\", \"chromatic\": true, \"name\": \"" +
                "Osc Soft Square Delay\", \"url\": \"PRESET_OSC_SQUARE_SOFT_DELAY\", " +
                "\"highNote\": 108, \"lowNote\": 0, \"octave\": 5}";

        oscs[7] = "{\"type\" : \"SOUNDSET\", \"chromatic\": true, \"name\": \"" +
                "Osc Square Delay\", \"url\": \"PRESET_OSC_SQUARE_DELAY\", " +
                "\"highNote\": 108, \"lowNote\": 0, \"octave\": 5}";

        oscs[6] = "{\"type\" : \"SOUNDSET\", \"chromatic\": true, \"name\": \"" +
                "Osc Soft Square Flange\", \"url\": \"PRESET_OSC_SQUARE_SOFT_FLANGE\", " +
                "\"highNote\": 108, \"lowNote\": 0, \"octave\": 5}";

        oscs[5] = "{\"type\" : \"SOUNDSET\", \"chromatic\": true, \"name\": \"" +
                "Osc Square\", \"url\": \"PRESET_OSC_SQUARE\", " +
                "\"highNote\": 108, \"lowNote\": 0, \"octave\": 5}";

        oscs[4] = "{\"type\" : \"SOUNDSET\", \"chromatic\": true, \"name\": \"" +
                "Osc Soft Square\", \"url\": \"PRESET_OSC_SQUARE_SOFT\", " +
                "\"highNote\": 108, \"lowNote\": 0, \"octave\": 5}";

        oscs[3] = "{\"type\" : \"SOUNDSET\", \"chromatic\": true, \"name\": \"" +
                "Osc Soft Saw\", \"url\": \"PRESET_OSC_SAW_SOFT\", " +
                "\"highNote\": 108, \"lowNote\": 0, \"octave\": 5}";

        oscs[2] = "{\"type\" : \"SOUNDSET\", \"chromatic\": true, \"name\": \"" +
                "Osc Saw\", \"url\": \"PRESET_OSC_SAW\", " +
                "\"highNote\": 108, \"lowNote\": 0, \"octave\": 5}";


        oscs[1] = "{\"type\" : \"SOUNDSET\", \"chromatic\": true, \"name\": \"" +
                "Osc Soft Saw Delay\", \"url\": \"PRESET_OSC_SAW_SOFT_DELAT\", " +
                "\"highNote\": 108, \"lowNote\": 0, \"octave\": 5}";

        oscs[0] = "{\"type\" : \"SOUNDSET\", \"chromatic\": true, \"name\": \"" +
                "Osc Saw Delay\", \"url\": \"PRESET_OSC_SAW_DELAY\", " +
                "\"highNote\": 108, \"lowNote\": 0, \"octave\": 5}";


        for (String s : oscs) {

            try {

                JSONObject jsonObject = new JSONObject(s);

                ContentValues data = new ContentValues();
                data.put("name", jsonObject.getString("name"));
                data.put("url", jsonObject.getString("url"));
                data.put("omg_id", jsonObject.getString("url"));
                data.put("type", "MELODY");
                data.put("chromatic", true);
                data.put("data", s);
                data.put("time", System.currentTimeMillis() / 1000);
                data.put("downloaded", 1);
                db.insert("soundsets", null, data);
            }

            catch (JSONException e) {
                Log.e("MGH setup oscillators", e.getMessage());
            }
        }

    }


    Cursor getCursor() {
        SQLiteDatabase db = mDB;
        Cursor cursor = db.rawQuery("SELECT * FROM soundsets WHERE downloaded = 1 ORDER BY _id DESC", null);

        return cursor;
    }

    public String getLastSaved() {
        String ret = "";
        SQLiteDatabase db = mDB;
        Cursor cursor = db.rawQuery("SELECT * FROM soundsets ORDER BY time DESC LIMIT 1", null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            ret = cursor.getString(cursor.getColumnIndex("data"));
        }
        cursor.close();
        return ret;
    }

    SoundSet addSoundSet(ContentValues data) {

        final SQLiteDatabase db = mDB;
        Cursor existing = db.rawQuery(
                "SELECT _id FROM soundsets WHERE url='" + data.getAsString("url") + "'", null);
        if (existing.getCount() > 0) {
            existing.moveToFirst();
            long id = existing.getLong(0);
            db.update("soundsets", data, "_id=" + id, null);
            data.put("_id", id);
        }
        else {
            data.put("_id", db.insert("soundsets", null, data));
        }
        existing.close();
        return new SoundSet(data);
    }

    private SoundSet getSoundSetByQuery(String where) {

        SoundSet soundset = null;

        SQLiteDatabase db = mDB;
        Cursor cursor = db.rawQuery("SELECT * FROM soundsets WHERE " + where, null);

        if (cursor.getCount() == 0) {
            cErrorMessage = "Couldn't find Sound Set in the database";
            showToast();
        }
        else {
            cursor.moveToFirst();
            soundset = new SoundSet(cursor);
            if (!soundset.isValid()) {
                cErrorMessage = "Not a valid soundset";
                showToast();
                soundset = null;
            }
        }

        cursor.close();
        return soundset;
    }

    SoundSet getSoundSetById(long id) {
        return getSoundSetByQuery("_id = " + Long.toString(id));
    }

    SoundSet getSoundSetByURL(String url) {
        return getSoundSetByQuery("url = '" + url + "'");
    }

    String getLastErrorMessage() {
        return cErrorMessage;
    }

    private void showToast() {
        ((Main)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, cErrorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void delete(long id) {
        SQLiteDatabase db = mDB;
        db.delete("soundsets", "_id=?", new String[] {Long.toString(id)});
    }

    void cleanUp() {
        mDB.close();
        this.close();
    }

    void saveAsDownlaoded(SoundSet mSoundSet) {
        String[] args = {"" + mSoundSet.getID()};
        ContentValues newData = new ContentValues();
        newData.put("downloaded", 1);
        mDB.update("soundsets", newData, "_id=?", args);
    }
}