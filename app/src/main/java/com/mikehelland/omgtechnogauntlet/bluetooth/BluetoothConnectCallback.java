package com.mikehelland.omgtechnogauntlet.bluetooth;

public abstract class BluetoothConnectCallback {

    public abstract void newStatus(String status);
    public abstract void onConnected(BluetoothConnection connection);
    public abstract void onDisconnected(BluetoothConnection connection);
}
