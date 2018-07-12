package com.mikehelland.omgtechnogauntlet.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BluetoothConnection extends Thread {
    private BluetoothDevice mDevice;
    private BluetoothManager bluetoothFactory;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final BluetoothSocket socket;
    private List<BluetoothConnectCallback> mConnectedCallbacks = new CopyOnWriteArrayList<>();
    private BluetoothDataCallback mDataCallback;

    private boolean disconnected = false;

    private final static String TAG = "MGH bluetoothconnection";
    
    BluetoothConnection(BluetoothDevice device, BluetoothManager bluetoothFactory,
                               BluetoothSocket socket, List<BluetoothConnectCallback> callbacks){
        this.bluetoothFactory = bluetoothFactory;
        mConnectedCallbacks.addAll(callbacks);
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.socket = socket;
        mDevice = device;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            newStatus(BluetoothManager.STATUS_IO_OPEN_STREAMS);
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;

    }

    public void run(){

        onConnected();

        int bytes;
        boolean hasData;

        while (!isInterrupted()){

            byte[] buffer = new byte[1024];
            hasData = false;

            try {
                bytes = mmInStream.read(buffer);
                if (bytes > 0) {
                    hasData = true;
                }
            } catch (IOException e){
                Log.e(TAG, e.getMessage());

                if (!bluetoothFactory.cleaningUp) {
                    newStatus(BluetoothManager.STATUS_IO_CONNECTED_THREAD);
                    onDisconnected();
                }
                break;
            }

            if (hasData)  {

                final String data = new String(buffer, 0, bytes); //new String(buffer).substring(0, bytes);

                if (mDataCallback != null)
                    bluetoothFactory.newData(mDataCallback, data);
            }

        }

        disconnected = true;
        if (!bluetoothFactory.cleaningUp) {
            resetConnections();
        }
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void sendNameValuePair(String name, String value) {
        writeString(name + "=" + value + ";");
    }

    public void sendCommand(String command) {
        writeString(command + ";");
    }

    private void writeString(String toWrite){
        Log.d("MGH bt writeString", toWrite);
        try {
            mmOutStream.write(toWrite.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void resetConnections() {
        try {
            mmOutStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mmInStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public void setDataCallback(BluetoothDataCallback callback) {
        mDataCallback = callback;
    }

    public BluetoothDataCallback getDataCallback() {
        return mDataCallback;
    }
    public void addConnectedCallback(BluetoothConnectCallback callback) {
        mConnectedCallbacks.add(callback);
    }

    private void newStatus(String status) {
        for (BluetoothConnectCallback callback : mConnectedCallbacks) {
            if (callback != null) {
                callback.newStatus(status);
            }
        }
    }

    private void onConnected() {
        if (mConnectedCallbacks != null) {
            for (BluetoothConnectCallback callback : mConnectedCallbacks) {
                if (callback != null) {
                    callback.onConnected(this);
                }
            }
        }
    }
    private void onDisconnected() {
        if (mConnectedCallbacks != null) {
            for (BluetoothConnectCallback callback : mConnectedCallbacks) {
                if (callback != null) {
                    callback.onDisconnected(this);
                }
            }
        }
    }
}
