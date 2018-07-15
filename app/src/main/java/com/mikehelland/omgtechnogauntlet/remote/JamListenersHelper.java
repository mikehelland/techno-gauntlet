package com.mikehelland.omgtechnogauntlet.remote;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnection;
import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothManager;
import com.mikehelland.omgtechnogauntlet.jam.Jam;

public class JamListenersHelper {

    public static void setJamListenersForHost(Jam jam, BluetoothManager bluetoothManager) {
        jam.addOnJamChangeListener(new BluetoothHostJamListener(jam, bluetoothManager));
        jam.addOnKeyChangeListener(new BluetoothHostKeyListener(bluetoothManager));
        jam.addOnBeatChangeListener(new BluetoothHostBeatListener(bluetoothManager));
        jam.addOnMixerChangeListener(new BluetoothHostMixerListener(bluetoothManager));

    }

    public static void setJamListenersForRemote(Jam jam, BluetoothConnection connection) {
        jam.addOnJamChangeListener(new BluetoothRemoteJamListener(connection));
        jam.addOnKeyChangeListener(new BluetoothRemoteKeyListener(connection));
        jam.addOnBeatChangeListener(new BluetoothRemoteBeatListener(connection));
        jam.addOnMixerChangeListener(new BluetoothRemoteMixerListener(connection));

    }

}
