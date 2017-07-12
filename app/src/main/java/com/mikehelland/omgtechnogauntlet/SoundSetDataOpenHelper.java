package com.mikehelland.omgtechnogauntlet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

class SoundSetDataOpenHelper extends SQLiteOpenHelper {

    private String cErrorMessage = "";

    private Context mContext;
    SoundSetDataOpenHelper(Context context) {
        super(context, "OMG_TECHNO_GAUNTLET", null, 1);
        Log.d("MGH", "datahelper constructor");
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("MGH", "onCreate soundsets table");
        db.execSQL("CREATE TABLE soundsets (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, data TEXT, url TEXT, type TEXT, time INTEGER, omg_id TEXT)");
        setupDefaultSoundSets(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void setupDefaultSoundSets(SQLiteDatabase db) {

        setupSamplerSoundSet(db);
        setupHipHopDrumsSoundSet(db);
        setupRockDrumsSoundSet(db);
        setupBassSoundSet(db);

    }

    private void setupSamplerSoundSet(SQLiteDatabase db) {
        String json = SamplerChannel.getDefaultSoundSetJson();

        ContentValues data = new ContentValues();
        data.put("name", "Percussion Sampler");
        data.put("url", "PRESET_PERCUSSION_SAMPLER");
        data.put("omg_id", "PRESET_PERCUSSION_SAMPLER");
        data.put("type", "DRUMBEAT");
        data.put("data", json);
        data.put("time", System.currentTimeMillis() / 1000);
        db.insert("soundsets", null, data);

    }

    private void setupRockDrumsSoundSet(SQLiteDatabase db) {
        String json = RockDrumChannel.getDefaultSoundSetJson();

        ContentValues data = new ContentValues();
        data.put("name", "Rock Drum Kit");
        data.put("url", "PRESET_ROCKKIT");
        data.put("omg_id", "PRESET_ROCKKIT");
        data.put("type", "DRUMBEAT");
        data.put("data", json);
        data.put("time", System.currentTimeMillis() / 1000);
        db.insert("soundsets", null, data);

    }

    private void setupHipHopDrumsSoundSet(SQLiteDatabase db) {
        String json = HipDrumChannel.getDefaultSoundSetJson();

        ContentValues data = new ContentValues();
        data.put("name", "Hip Hop Drum Kit");
        data.put("url", "PRESET_HIPKIT");
        data.put("omg_id", "PRESET_HIPKIT");
        data.put("type", "DRUMBEAT");
        data.put("data", json);
        data.put("time", System.currentTimeMillis() / 1000);
        db.insert("soundsets", null, data);
    }

    private void setupBassSoundSet(SQLiteDatabase db) {
        String json = BassSamplerChannel.getDefaultSoundSetJSON(mContext);

        ContentValues data = new ContentValues();
        data.put("name", "Electric Bass");
        data.put("url", "PRESET_BASS");
        data.put("omg_id", "PRESET_BASS");
        data.put("type", "BASSLINE");
        data.put("data", json);
        data.put("time", System.currentTimeMillis() / 1000);
        db.insert("soundsets", null, data);

    }


    Cursor getSavedCursor(SQLiteDatabase db) {

        Cursor cursor = db.rawQuery("SELECT * FROM soundsets ORDER BY time DESC", null);
        //db.close();

        Log.d("MGH", "opening cursor");
        Log.d("MGH", Integer.toString(cursor.getCount()));

        return cursor;

    }

    public String getLastSaved() {
        String ret = "";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM soundsets ORDER BY time DESC LIMIT 1", null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            ret = cursor.getString(cursor.getColumnIndex("data"));
        }
        cursor.close();
        db.close();
        return ret;
    }

    long newSoundSet(ContentValues data) {
        long id;
        final SQLiteDatabase db = getWritableDatabase();
        id = db.insert("soundsets", null, data);
        db.close();
        return id;
    }

    private SoundSet getSoundSetByQuery(String where) {

        SoundSet soundset = null;

        SQLiteDatabase db = getReadableDatabase();
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

        db.close();
        return soundset;
    }

    SoundSet getSoundSetById(long id) {
        return getSoundSetByQuery("_id = " + Long.toString(id));
    }

    SoundSet getSoundSetByURL(String url) {
        return getSoundSetByQuery("url = '" + url + "'");
    }

    public String getLastErrorMessage() {
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
}