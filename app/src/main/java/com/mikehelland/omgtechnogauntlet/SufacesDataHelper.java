package com.mikehelland.omgtechnogauntlet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class SufacesDataHelper extends SQLiteOpenHelper {

    static String PRESET_VERTICAL = "PRESET_VERTICAL";
    static String PRESET_FRETBOARD = "PRESET_FRETBOARD";
    static String PRESET_SEQUENCER = "PRESET_SEQUENCER";

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
        data.put("url", PRESET_SEQUENCER);
        data.put("data", PRESET_SEQUENCER);
        data.put("time", System.currentTimeMillis() / 1000);
        db.insert("surfaces", null, data);

        data = new ContentValues();
        data.put("name", "Vertical");
        data.put("url", PRESET_VERTICAL);
        data.put("data", PRESET_VERTICAL);
        data.put("time", System.currentTimeMillis() / 1000);
        db.insert("surfaces", null, data);

        data = new ContentValues();
        data.put("name", "Fretboard");
        data.put("url", PRESET_FRETBOARD);
        data.put("data", PRESET_FRETBOARD);
        data.put("time", System.currentTimeMillis() / 1000);
        db.insert("surfaces", null, data);

    }


    Cursor getSavedCursor(SQLiteDatabase db) {

        Cursor cursor = db.rawQuery("SELECT * FROM surfaces ORDER BY time DESC", null);
        //db.close();

        Log.d("MGH", "opening cursor");
        Log.d("MGH", Integer.toString(cursor.getCount()));

        return cursor;

    }

}