package com.mikehelland.omgtechnogauntlet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mikehelland.omgtechnogauntlet.jam.Surface;

class SufacesDataHelper extends SQLiteOpenHelper {

    SufacesDataHelper(Context context) {
        super(context, "OMG_SURFACES", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE surfaces (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, data TEXT, url TEXT, type TEXT, chromatic BOOLEAN, time INTEGER, omg_id TEXT)");
        setupDefaults(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void setupDefaults(SQLiteDatabase db) {

        ContentValues data = new ContentValues();
        data.put("name", "Sequencer");
        data.put("url", Surface.PRESET_SEQUENCER);
        data.put("data", Surface.PRESET_SEQUENCER);
        data.put("time", System.currentTimeMillis() / 1000);
        db.insert("surfaces", null, data);

        data = new ContentValues();
        data.put("name", "Vertical");
        data.put("url", Surface.PRESET_VERTICAL);
        data.put("data", Surface.PRESET_VERTICAL);
        data.put("time", System.currentTimeMillis() / 1000);
        db.insert("surfaces", null, data);

        data = new ContentValues();
        data.put("name", "Fretboard");
        data.put("url", Surface.PRESET_FRETBOARD);
        data.put("data", Surface.PRESET_FRETBOARD);
        data.put("time", System.currentTimeMillis() / 1000);
        db.insert("surfaces", null, data);

    }


    Cursor getSavedCursor(SQLiteDatabase db) {

        Cursor cursor = db.rawQuery("SELECT * FROM surfaces ORDER BY time DESC", null);
        return cursor;

    }

}