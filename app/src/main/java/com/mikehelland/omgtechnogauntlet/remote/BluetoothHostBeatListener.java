package com.mikehelland.omgtechnogauntlet.remote;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothManager;
import com.mikehelland.omgtechnogauntlet.jam.OnBeatChangeListener;

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
    }

    @Override
    public void onBeatsChange(int length, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_BEATS,
                Integer.toString(length), source);
    }

    @Override
    public void onMeasuresChange(int length, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_MEASURES,
                Integer.toString(length), source);
    }

    @Override
    public void onShuffleChange(float length, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_SHUFFLE,
                Float.toString(length), source);
    }
}
