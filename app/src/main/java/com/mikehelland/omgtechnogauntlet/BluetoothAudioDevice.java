package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class BluetoothAudioDevice {} //extends AudioDevice {
/*
    private BluetoothFactory btf;

    Context mContext;

    public BluetoothAudioDevice(Context context, String instrument, BluetoothFactory btf) {
        super(instrument);

        mContext = context;
        this.btf = btf;
        Log.d("MGH", "btad create channel");
        String params = getCreateChannelParameters();
        btf.writeToBluetooth(ACTION_CREATECHANNEL + ";" + params + ":");

    }

    public void stopChannel(int chanId) {

        btf.writeToBluetooth(ACTION_STOPCHANNEL + ";" + Integer.toString(chanId) + ":");
        super.stopChannel(chanId);
    }

    public int startChannel(int x) {
        int id = super.startChannel(x);

        Log.d("MGH", "btad startchannel");
        btf.writeToBluetooth(ACTION_STARTCHANNEL + ";" + Integer.toString(id) + ";" + Integer.toString(x) + ":");

        return id;

    }

    public void startChannel(int chan, int x) {
    }

    public void setChannel(int id, int x) {
        btf.writeToBluetooth(ACTION_SETCHANNEL + ";" + Integer.toString(id) + ";" + Integer.toString(x) + ":");
    }

    public void finish() {

    }


    @Override
    public String getCreateChannelParameters() {
        StringBuilder ret = new StringBuilder(instrument);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean delay = prefs.getBoolean("soft_e", true);
        boolean flange = prefs.getBoolean("flange", false);
        boolean softe = prefs.getBoolean("soft_e", false);
        String wave = prefs.getString("waveform", "Sine");


        ret.append(";");
        ret.append(wave);

        if (delay) {
            ret.append(";delay");
        }
        if (flange) {
            ret.append(";flange");
        }
        if (softe) {
            ret.append(";softe");
        }

        return ret.toString();

    }

}
*/