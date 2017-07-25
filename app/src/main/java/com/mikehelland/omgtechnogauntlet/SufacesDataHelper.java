package com.mikehelland.omgtechnogauntlet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

class SufacesDataHelper extends SQLiteOpenHelper {

    private String cErrorMessage = "";

    private Context mContext;
    SufacesDataHelper(Context context) {
        super(context, "OMG_SURFACES", null, 1);
        Log.d("MGH", "datahelper constructor");
        mContext = context;
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
        data.put("url", "PRESET_SEQUENCER");
        data.put("data", "PRESET_SEQUENCER");
        data.put("time", System.currentTimeMillis() / 1000);
        db.insert("surfaces", null, data);

        data = new ContentValues();
        data.put("name", "Vertical");
        data.put("url", "PRESET_VERTICAL");
        data.put("data", "PRESET_VERTICAL");
        data.put("time", System.currentTimeMillis() / 1000);
        db.insert("surfaces", null, data);

        data = new ContentValues();
        data.put("name", "Default Fretboard");
        data.put("url", "PRESET_DEFAULT_FRETBOARD");
        data.put("data", "PRESET_DEFAULT_FRETBOARD");
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

    private Fretboard getByQuery(String where) {

        Fretboard fretboard = null;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM surfaces WHERE " + where, null);

        if (cursor.getCount() == 0) {
            cErrorMessage = "Couldn't find surface in the database";
            showToast();
        }
        else {
            cursor.moveToFirst();
            /*fretboard = new Fretboard();
            if (!fretboard.isValid()) {
                cErrorMessage = "Not a valid soundset";
                showToast();
                fretboard = null;
            }*/
        }

        db.close();
        return fretboard;
    }

    Fretboard getById(long id) {
        return getByQuery("_id = " + Long.toString(id));
    }

    Fretboard getByURL(String url) {
        return getByQuery("url = '" + url + "'");
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