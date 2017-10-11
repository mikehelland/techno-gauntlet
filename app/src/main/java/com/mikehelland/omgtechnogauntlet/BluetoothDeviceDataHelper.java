package com.mikehelland.omgtechnogauntlet;

import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class BluetoothDeviceDataHelper extends SQLiteOpenHelper {

    BluetoothDeviceDataHelper(Context context) {
        super(context, "OMG_BT_DEVICE", null, 1);
        Log.d("MGH", "datahelper constructor");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE devices (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, mac TEXT, remote BOOLEAN, brain BOOLEAN, time INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    void addBrainDevice(BluetoothDevice device) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues data = new ContentValues();
        data.put("brain", true);

        Cursor cursor = db.rawQuery("SELECT * FROM devices WHERE mac = '" + device.getAddress() + "'", null);
        if (cursor.getCount() > 0) {
            db.update("devices", data, "mac = '" + device.getAddress() + "'", null);
            return;
        }

        data.put("name", device.getName());
        data.put("mac", device.getAddress());
        data.put("time", System.currentTimeMillis() / 1000);
        db.insert("devices", null, data);
        db.close();
    }

    Cursor getBrainsCursor() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM devices WHERE brain ORDER BY _id DESC", null);
        return cursor;
    }

}