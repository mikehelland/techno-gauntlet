package com.mikehelland.omgtechnogauntlet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class SoundSetDataOpenHelper extends SQLiteOpenHelper {

    //private Context mContext;
    SoundSetDataOpenHelper(Context context) {
        super(context, "OMG_TECHNO_GAUNTLET", null, 1);
        Log.d("MGH", "datahelper constructor");
        //mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("MGH", "onCreate soundsets table");
        db.execSQL("CREATE TABLE soundsets (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, data TEXT, url TEXT, time INTEGER, omg_id TEXT)");
        setupDefaultSoundSets(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void setupDefaultSoundSets(SQLiteDatabase db) {

        setupSamplerSoundSet(db);
        setupHipHopDrumsSoundSet(db);
        setupRockDrumsSoundSet(db);

    }

    private void setupSamplerSoundSet(SQLiteDatabase db) {
        String json = SamplerChannel.getDefaultSoundSetJson();

        ContentValues data = new ContentValues();
        data.put("name", "Percussion Sampler");
        data.put("url", "PRESET_PERCUSSION_SAMPLER");
        data.put("omg_id", "PRESET_PERCUSSION_SAMPLER");
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

    SoundSet getSoundSetById(long id) {

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM soundsets WHERE _id = " + Long.toString(id), null);
        if (cursor.getCount() == 0) {
            return null;
        }

        cursor.moveToFirst();
        SoundSet soundset = new SoundSet(cursor);

        db.close();
        return soundset;
    }

}