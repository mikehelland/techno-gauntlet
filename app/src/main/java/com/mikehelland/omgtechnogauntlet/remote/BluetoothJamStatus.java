package com.mikehelland.omgtechnogauntlet.remote;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnection;
import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothManager;
import com.mikehelland.omgtechnogauntlet.jam.Jam;

public class BluetoothJamStatus {

    private BluetoothHostJamListener hostJamListener;
    private BluetoothHostKeyListener hostKeyListener;
    private BluetoothHostBeatListener hostBeatListener;
    private BluetoothHostMixerListener hostMixerListener;

    boolean isHost = false;
    boolean isRemote = false;

    Jam jam;
    private BluetoothManager bluetoothManager;
    private BluetoothConnection connectionToHost;

    public BluetoothJamStatus(Jam jam, BluetoothManager bluetoothManager) {
        this.jam = jam;
        this.bluetoothManager = bluetoothManager;
    }

    public void setupJamAsHost() {
        if (!isHost) {
            hostJamListener = new BluetoothHostJamListener(jam, bluetoothManager);
            hostKeyListener = new BluetoothHostKeyListener(bluetoothManager);
            hostBeatListener = new BluetoothHostBeatListener(bluetoothManager);
            hostMixerListener = new BluetoothHostMixerListener(bluetoothManager);
            jam.addOnJamChangeListener(hostJamListener);
            jam.addOnKeyChangeListener(hostKeyListener);
            jam.addOnBeatChangeListener(hostBeatListener);
            jam.addOnMixerChangeListener(hostMixerListener);
        }
        isHost = true;

    }

    public static void setJamListenersForRemote(Jam jam, BluetoothConnection connection) {
        jam.addOnJamChangeListener(new BluetoothRemoteJamListener(connection));
        jam.addOnKeyChangeListener(new BluetoothRemoteKeyListener(connection));
        jam.addOnBeatChangeListener(new BluetoothRemoteBeatListener(connection));
        jam.addOnMixerChangeListener(new BluetoothRemoteMixerListener(connection));

    }

    public boolean isRemote() {
        return isRemote && connectionToHost != null;
    }

    public BluetoothConnection getConnectionToHost() {
        return connectionToHost;
    }

    public void setupRemote(BluetoothConnection connection) {
        if (connection != null) {
            isRemote = true;
            connectionToHost = connection;
        }
        else {
            isRemote = false;
            connectionToHost = null;
        }
    }
}
