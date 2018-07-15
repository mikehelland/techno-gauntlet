package com.mikehelland.omgtechnogauntlet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mikehelland.omgtechnogauntlet.jam.JamHeader;

import java.util.ArrayList;

class SavedDataOpenHelper extends SQLiteOpenHelper {

    private SQLiteDatabase mDB;

    SavedDataOpenHelper(Context context) {
        super(context, "OMG_SAVES", null, 5);
        mDB = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE saves (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tags TEXT, data TEXT, time INTEGER, omg_id INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


        if (oldVersion == 1) {

            db.execSQL("CREATE TABLE saves (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "tags TEXT, data TEXT, time INTEGER)");

            Cursor cursor = db.rawQuery("SELECT * FROM bananas", null);
            ContentValues data;

            int tagsColumn = cursor.getColumnIndex("TAGS");
            int dataColumn = cursor.getColumnIndex("DATA");


            while (cursor.moveToNext()) {
                data = new ContentValues();
                data.put("tags", cursor.getString(tagsColumn));
                data.put("data", cursor.getString(dataColumn));
                data.put("time", System.currentTimeMillis()/1000);
                db.insert("saves", null, data);

            }
            cursor.close();

            db.execSQL("DROP TABLE bananas");

        }

        if (oldVersion == 4) {
            db.execSQL("ALTER TABLE saves ADD COLUMN omg_id INTEGER");
        }
    }

    Cursor getSavedCursor() {
        return mDB.rawQuery("SELECT * FROM saves ORDER BY time DESC", null);
    }

    public String getLastSaved() {
        String ret = "";
        Cursor cursor = mDB.rawQuery("SELECT * FROM saves ORDER BY time DESC LIMIT 1", null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            ret = cursor.getString(cursor.getColumnIndex("data"));
        }
        cursor.close();
        return ret;
    }

    void delete(long id) {
        mDB.delete("saves", "_id=?", new String[] {Long.toString(id)});
    }

    String getJamJson(long id) {
        Cursor cursor = mDB.rawQuery("SELECT data FROM saves WHERE _id=" + id, null);
        cursor.moveToFirst();
        String json = cursor.getString(0);
        cursor.close();
        return json;
    }

    void cleanUp() {
        mDB.close();
        this.close();
    }

    void insert(long id, String tags, String jamData) {
        final ContentValues data = new ContentValues();
        data.put("tags", tags);
        data.put("data", jamData);
        data.put("time", System.currentTimeMillis()/1000);
        data.put("omg_id", id);

        mDB.insert("saves", null, data);
    }

    public ArrayList<JamHeader> getList() {
        ArrayList<JamHeader> result = new ArrayList<>();
        Cursor cursor = getSavedCursor();
        int idColumn = cursor.getColumnIndex("_id");
        int nameColumn = cursor.getColumnIndex("tags");
        int urlColumn = cursor.getColumnIndex("url");
        while (cursor.moveToNext()) {
            //using the id here, why not the url?
            result.add(new JamHeader(cursor.getLong(idColumn), cursor.getString(nameColumn)));
        }
        cursor.close();
        return result;
    }

}