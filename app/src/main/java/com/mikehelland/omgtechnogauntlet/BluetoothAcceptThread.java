package com.mikehelland.omgtechnogauntlet;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

/**
 * Created by m on 10/10/17.
 */

class BluetoothAcceptThread  extends Thread {

    BluetoothServerSocket mServerSocket;

    private BluetoothConnectCallback mCallback;
    private BluetoothManager mBluetoothManager;
    private boolean isFinished = false;

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
    }

    public void run(){

        if (mServerSocket == null) {
            return;
        }

        BluetoothSocket socket;
        //while (!isInterrupted()){
        while (!isFinished){
            try {
                socket = mServerSocket.accept();
            } catch (IOException e){
                if (!isFinished)
                    mBluetoothManager.newStatus(mCallback, "IOException in accept()");

                break;
            }

            if (socket != null){
                mBluetoothManager.newStatus(mCallback, "Connecting...");
                mBluetoothManager.newConnection(socket.getRemoteDevice(), socket, mCallback);
            }

        }

        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
            }
        }
    }

    void finish() {
        isFinished = true;
    }
}
