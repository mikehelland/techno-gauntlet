package com.mikehelland.omgtechnogauntlet;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

class BluetoothAcceptThread  extends Thread {

    private BluetoothServerSocket mServerSocket;

    private BluetoothConnectCallback mCallback;
    private BluetoothManager mBluetoothManager;

    private boolean isAccepting;

    BluetoothAcceptThread (BluetoothManager bt, BluetoothConnectCallback callback){
        mCallback = callback;
        mBluetoothManager = bt;
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
            tmp =  bt.getAdapter().listenUsingRfcommWithServiceRecord("OMG BANANAS", bt.getUUID());

        }    catch (IOException e) {
            bt.newStatus(callback, "IOException in listenUsingRfcomm");
        }
        mServerSocket = tmp;
        isAccepting = true;
    }

    public void run(){

        if (mServerSocket == null) {
            return;
        }

        BluetoothSocket socket;
        while (isAccepting){
            try {
                socket = mServerSocket.accept();
            } catch (IOException e){
                if (isAccepting)
                    mBluetoothManager.newStatus(mCallback, "IOException in accept()");

                break;
            }

            if (socket != null){
                mBluetoothManager.newStatus(mCallback, "Connecting...");
                mBluetoothManager.newConnection(socket.getRemoteDevice(), socket, mCallback);
            }

        }

        cleanUp();
        Log.d("MGH accept thread", "finish up");
    }

    void stopAccepting() {
        isAccepting = false;
        cleanUp();
    }

    private void cleanUp() {
        Log.d("MGH accept thread", "close socket");
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
                mServerSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
