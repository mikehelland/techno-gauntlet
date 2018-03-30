package com.mikehelland.omgtechnogauntlet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mikehelland.omgtechnogauntlet.jam.SoundSet;

import org.json.JSONException;
import org.json.JSONObject;

class SoundSetDataOpenHelper extends SQLiteOpenHelper {

    private String cErrorMessage = "";
    private SQLiteDatabase mDB;

    private String slapBass;
    private String bass;
    SoundSetDataOpenHelper(Context context) {
        super(context, "OMG_TECHNO_GAUNTLET", null, 2);

        slapBass = BassSampler.getSlapSoundSetJSON(context.getResources());
        bass = BassSampler.getDefaultSoundSetJSON(context.getResources());

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
                "name TEXT, data TEXT, url TEXT, type TEXT, chromatic BOOLEAN, " +
                "time INTEGER, omg_id TEXT, downloaded INTEGER)");
        setupDefaultSoundSets(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
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
        String json = PercussionSampler.getDefaultSoundSetJson();

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
        String json = RockDrumSampler.getDefaultSoundSetJson();

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
        String json = HipDrumSampler.getDefaultSoundSetJson();

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

        ContentValues data = new ContentValues();
        data.put("name", "Electric Bass");
        data.put("url", "PRESET_BASS");
        data.put("omg_id", "PRESET_BASS");
        data.put("type", "BASSLINE");
        data.put("chromatic", true);
        data.put("data", bass);
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

        ContentValues data = new ContentValues();
        data.put("name", "Slap Bass");
        data.put("url", "http://openmusic.gallery/data/413");
        data.put("omg_id", "PRESET_SLAP_BASS");
        data.put("type", "BASSLINE");
        data.put("chromatic", true);
        data.put("data", slapBass);
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

        String proto = "\"type\" : \"SOUNDSET\", \"cshromatic\": true, " +
                "\"defaultSurface\": \"PRESET_VERTICAL\", " +
                "\"highNote\": 108, \"lowNote\": 0, \"octave\": 5}";

        String[] oscs = new String[11];
        oscs[10] = "{\"name\": \"Osc Soft Sine Delay\", \"url\": \"PRESET_OSC_SINE_SOFT_DELAY\", " + proto;

        oscs[9] = "{\"name\": \"Osc Sine\", \"url\": \"PRESET_OSC_SINE\", " + proto;

        oscs[8] = "{\"name\": \"Osc Soft Square Delay\", \"url\": \"PRESET_OSC_SQUARE_SOFT_DELAY\", " + proto;

        oscs[7] = "{\"name\": \"Osc Square Delay\", \"url\": \"PRESET_OSC_SQUARE_DELAY\", " + proto;

        oscs[6] = "{\"name\": \"Osc Soft Square Flange\", \"url\": \"PRESET_OSC_SQUARE_SOFT_FLANGE\", " + proto;

        oscs[5] = "{\"name\": \"Osc Square\", \"url\": \"PRESET_OSC_SQUARE\", " + proto;

        oscs[4] = "{\"name\": \"Osc Soft Square\", \"url\": \"PRESET_OSC_SQUARE_SOFT\", " + proto;

        oscs[3] = "{\"name\": \"Osc Soft Saw\", \"url\": \"PRESET_OSC_SAW_SOFT\", " + proto;

        oscs[2] = "{\"name\": \"Osc Saw\", \"url\": \"PRESET_OSC_SAW\", " + proto;

        oscs[1] = "{\"name\": \"Osc Soft Saw Delay\", \"url\": \"PRESET_OSC_SAW_SOFT_DELAT\", " + proto;

        oscs[0] = "{\"name\": \"Osc Saw Delay\", \"url\": \"PRESET_OSC_SAW_DELAY\", " + proto;

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

            catch (JSONException ignore) { }
        }

    }


    Cursor getCursor() {
        SQLiteDatabase db = mDB;
        return db.rawQuery("SELECT * FROM soundsets WHERE downloaded = 1 ORDER BY _id DESC", null);
    }

    /*public String getLastSaved() {
        String ret = "";
        SQLiteDatabase db = mDB;
        Cursor cursor = db.rawQuery("SELECT * FROM soundsets ORDER BY time DESC LIMIT 1", null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            ret = cursor.getString(cursor.getColumnIndex("data"));
        }
        cursor.close();
        return ret;
    }*/

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
        }
        else {
            cursor.moveToFirst();
            //todo this cursor thing can't be right
            soundset = new SoundSet(cursor);
            if (!soundset.isValid()) {
                cErrorMessage = "Not a valid soundset";
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

    void delete(long id) {
        SQLiteDatabase db = mDB;
        db.delete("soundsets", "_id=?", new String[] {Long.toString(id)});
    }

    void cleanUp() {
        mDB.close();
        this.close();
    }

    void saveAsDownloaded(SoundSet mSoundSet) {
        String[] args = {"" + mSoundSet.getID()};
        ContentValues newData = new ContentValues();
        newData.put("downloaded", 1);
        if (mDB.isOpen()) {
            mDB.update("soundsets", newData, "_id=?", args);
        }
    }

    String getLastErrorMessage() {
        return cErrorMessage;
    }
}