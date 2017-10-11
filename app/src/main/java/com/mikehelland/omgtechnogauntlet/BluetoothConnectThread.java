package com.mikehelland.omgtechnogauntlet;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

/**
 * Created by m on 10/10/17.
 */

class BluetoothConnectThread extends Thread {
    BluetoothDevice mDevice;
    BluetoothSocket mSocket;
    BluetoothConnectCallback mConnectCallback;
    BluetoothManager mBT;

    BluetoothConnectThread(BluetoothManager bt, BluetoothDevice device, BluetoothConnectCallback callback) {
        mBT = bt;
        mConnectCallback = callback;
        mDevice = device;
        BluetoothSocket tmp = null;

        try {
            tmp = device.createRfcommSocketToServiceRecord(mBT.getUUID());
        } catch (IOException e) {
            mBT.newStatus(mConnectCallback, "IOException in createRfcommSocket");
            Log.d("MGH bt error", e.getMessage());}
        mSocket = tmp;
    }

    public void run() {
        //mBluetooth.cancelDiscovery();

        boolean good = false;
        try {
            mSocket.connect();
            good = true;

        } catch (IOException connectException) {
            Log.d("MGH bt error", connectException.getMessage());
            mBT.newStatus(mConnectCallback, BluetoothManager.STATUS_IO_CONNECT_THREAD);
        }
        if (good)
            mBT.newConnection(mDevice, mSocket, mConnectCallback);
        else {
            try {
                mSocket.close();
            }
            catch (IOException e) {
                Log.d("MGH bt error", e.getMessage());
            }
        }
    }
}