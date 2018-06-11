package com.mikehelland.omgtechnogauntlet;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothManager;
import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.Note;
import com.mikehelland.omgtechnogauntlet.jam.OnBeatChangeListener;
import com.mikehelland.omgtechnogauntlet.jam.OnJamChangeListener;

/**
 * Created by m on 4/12/18.
 *
 */

public class BluetoothHostBeatListener extends OnBeatChangeListener {

    private BluetoothManager bluetoothManager;

    BluetoothHostBeatListener(BluetoothManager bluetoothManager) {
        this.bluetoothManager = bluetoothManager;
    }
    
    @Override
    public void onSubbeatLengthChange(int length, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_SUBBEATLENGTH,
                Integer.toString(length), source);
    }}
