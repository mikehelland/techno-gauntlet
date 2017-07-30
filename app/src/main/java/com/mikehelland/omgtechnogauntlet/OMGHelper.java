package com.mikehelland.omgtechnogauntlet;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

class OMGHelper {

    private static String mSubmitUrl = "data/";
    private static String mHomeUrl = "http://openmusic.gallery/";
    private static String SHARE_URL_SUFFIX = "viewer.htm?id=";

    private Context mContext;

    private Jam mJam;

    enum Type {
        DRUMBEAT, BASSLINE, MELODY, CHORDPROGRESSION, SECTION
    }

    OMGHelper(Context context, Jam jam) {
        mContext =  context;
        mJam = jam;

    }

    void submit(final boolean shareAfter) {

        final String jamData = mJam.getData();
        OMGCallback callback = new OMGCallback() {
            @Override
            public void onSuccess(long id) {

                final ContentValues data = new ContentValues();
                data.put("tags", mJam.getTags());
                data.put("data", jamData);
                data.put("time", System.currentTimeMillis()/1000);
                data.put("omg_id", id);

                final SQLiteDatabase db = new SavedDataOpenHelper(mContext).getWritableDatabase();
                db.insert("saves", null, data);
                db.close();

                if (shareAfter) {
                    share(id);
                }
            }
        };

        new SaveToOMG().execute(mHomeUrl + mSubmitUrl, Type.SECTION.toString(), jamData,
                callback);

    }

    private void share(long id) {

        String shareUrl = mHomeUrl + SHARE_URL_SUFFIX + Long.toString(id);
        String actionSend = Intent.ACTION_SEND;
        Intent shareIntent = new Intent(actionSend);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mContext.getString(R.string.app_name));
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareUrl);
        mContext.startActivity(Intent.createChooser(shareIntent, "Share"));

    }

}
