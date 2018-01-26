package com.mikehelland.omgtechnogauntlet;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
        }
        mSocket = tmp;
    }

    public void run() {
        //mBluetooth.cancelDiscovery();

        boolean good = false;
        try {
            mSocket.connect();
            good = true;

        } catch (IOException connectException) {
            mBT.newStatus(mConnectCallback, BluetoothManager.STATUS_IO_CONNECT_THREAD);
        }
        if (good) {
            List<BluetoothConnectCallback> callbacks = new CopyOnWriteArrayList<>();
            callbacks.add(mConnectCallback);
            mBT.newConnection(mDevice, mSocket, callbacks);
        }
        else {
            try {
                mSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}