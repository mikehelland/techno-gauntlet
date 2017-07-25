package com.mikehelland.omgtechnogauntlet;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class OMGHelper {

    private static String mSubmitUrl = "data/";
    //private static String mHomeUrl = "http://10.0.2.2:8888/";
    //private static String mHomeUrl = "http://192.168.1.116:8888/";
    private static String mHomeUrl = "http://openmusic.gallery/";
    private static String SHARE_URL_SUFFIX = "viewer.htm?id=";

    private Context mContext;

    private Type mType;
    private String mData;

    private long lastSavedId = -1;
    private long lastSavedSQLId = -1;

    public enum Type {
        DRUMBEAT, BASSLINE, MELODY, CHORDPROGRESSION, SECTION
    }

    public OMGHelper(Context context, Type type, String data) {
        mContext =  context;
        mType = type;
        mData = data;

    }

    public void submit() {

        final ContentValues data = new ContentValues();
        data.put("tags", "");
        data.put("data", mData);
        data.put("time", System.currentTimeMillis()/1000);


        final SQLiteDatabase db = new SavedDataOpenHelper(mContext).getWritableDatabase();
        lastSavedSQLId = db.insert("saves", null, data);
        db.close();

        OMGCallback callback = new OMGCallback() {
            @Override
            public void onSuccess(long id) {

                lastSavedId = id;

                ContentValues data = new ContentValues();
                data.put("omg_id", id);

                SQLiteDatabase db = new SavedDataOpenHelper(mContext).getWritableDatabase();
                db.update("saves", data, "_id=" + Long.toString(lastSavedSQLId), null);
                db.close();

            }
        };

        new SaveToOMG().execute(mHomeUrl + mSubmitUrl, mType.toString(), mData,
                callback);

    }

    public void updateTags(String tags) {
        ContentValues data = new ContentValues();
        data.put("tags", tags);

        SQLiteDatabase db = new SavedDataOpenHelper(mContext).getWritableDatabase();
        db.update("saves", data, "_id=" + Long.toString(lastSavedSQLId), null);
        db.close();

    }

    public void shareLastSaved() {
        Log.d("MGH last saved ID", Long.toString(lastSavedId));
        if (lastSavedId <= 0) {
            return;
        }

        String shareUrl = mHomeUrl + SHARE_URL_SUFFIX + Long.toString(lastSavedId);
        String actionSend = Intent.ACTION_SEND;
        Intent shareIntent = new Intent(actionSend);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mContext.getString(R.string.app_name));
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareUrl);
        mContext.startActivity(Intent.createChooser(shareIntent, "Share"));

    }

}
