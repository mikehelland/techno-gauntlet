package com.mikehelland.omgtechnogauntlet;

import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

class BluetoothDeviceDataHelper extends SQLiteOpenHelper {

    private SQLiteDatabase mDB;

    BluetoothDeviceDataHelper(Context context) {
        super(context, "OMG_BT_DEVICE", null, 1);
        mDB = getWritableDatabase();
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

        SQLiteDatabase db = mDB;

        ContentValues data = new ContentValues();
        data.put("brain", true);

        Cursor cursor = db.rawQuery("SELECT * FROM devices WHERE mac = '" + device.getAddress() + "'", null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        if (exists) {
            db.update("devices", data, "mac = '" + device.getAddress() + "'", null);
            return;
        }

        data.put("name", device.getName());
        data.put("mac", device.getAddress());
        data.put("time", System.currentTimeMillis() / 1000);
        db.insert("devices", null, data);
    }

    void removeBrainDevice(BluetoothDevice device) {
        String[] args = new String[]{device.getAddress()};
        mDB.delete("devices", "mac = $1", args);
    }

    List<String> getBrainMACList() {
        Cursor cursor = mDB.rawQuery("SELECT * FROM devices WHERE brain ORDER BY _id DESC", null);
        List<String> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(cursor.getString(cursor.getColumnIndex("mac")));
        }
        cursor.close();
        return list;
    }

    void cleanUp() {
        mDB.close();
        this.close();
    }
}