package com.mikehelland.omgtechnogauntlet;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class BluetoothAcceptThread  extends Thread {

    private BluetoothServerSocket mServerSocket;

    private List<BluetoothConnectCallback> mCallbacks = new CopyOnWriteArrayList<>();
    private BluetoothManager mBluetoothManager;

    private boolean isAccepting;

    BluetoothAcceptThread (BluetoothManager bt) {
        mBluetoothManager = bt;
    }

    public void run(){
        BluetoothServerSocket tmp = null;

        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mServerSocket = null;
        }

        try {
            tmp =  mBluetoothManager.getAdapter().listenUsingRfcommWithServiceRecord("OMG BANANAS",
                    mBluetoothManager.getUUID());

        }    catch (IOException e) {
            newStatus("IOException in listenUsingRfcomm");
        }
        mServerSocket = tmp;
        isAccepting = true;

        if (mServerSocket == null) {
            return;
        }

        BluetoothSocket socket;
        while (isAccepting){
            try {
                socket = mServerSocket.accept();
            } catch (IOException e){
                if (isAccepting)
                    newStatus("IOException in accept()");

                break;
            }

            if (socket != null){
                newStatus("Connecting...");
                mBluetoothManager.newConnection(socket.getRemoteDevice(), socket, mCallbacks);
            }

        }

        cleanUp();
    }

    void stopAccepting() {
        isAccepting = false;
        cleanUp();
    }

    private void cleanUp() {
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
                mServerSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void addCallback(BluetoothConnectCallback callback) {
        if (callback != null) {
            mCallbacks.add(callback);
        }
    }

    void removeCallback(BluetoothConnectCallback callback) {
        mCallbacks.remove(callback);
    }

    private void newStatus(String status) {
        for (BluetoothConnectCallback callback : mCallbacks) {
            if (callback != null) {
                callback.newStatus(status);
            }
        }
    }
}
