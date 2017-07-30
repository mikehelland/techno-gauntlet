package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

class BluetoothFactory {

    static final String STATUS_IO_CONNECTED_THREAD  = "IOException in ConnectedThread";
    static final String STATUS_IO_OPEN_STREAMS  = "IOException opening streams";
    static final String STATUS_ACCEPTING_CONNECTIONS = "Accepting Connections";
    static final String STATUS_CONNECTING_TO = "Searching ...";
    static final String STATUS_IO_CONNECT_THREAD  = "Device not Available";
    static final String STATUS_BLUETOOTH_TURNED_ON = "Bluetooth has been turned on";
    static final int REQUEST_ENABLE_BT = 2;

    private static final String NAME = "OMG BANANAS";
    private static final UUID MY_UUID = UUID.fromString("e0358210-6406-11e1-b86c-0800200c9a66");
    private BluetoothAdapter mBluetooth;
    private AcceptThread acceptThread;
    private final Activity ctx;

    private final static String TAG = "MGH Bluetooth";

    private BluetoothSetupCallback setupCallback;

    private ArrayList<BluetoothConnection> connectionThreads = new ArrayList<BluetoothConnection>();

    boolean cleaningUp = false;

    BluetoothServerSocket mServerSocket;

    private Set<BluetoothDevice> paired;

    private boolean isSetup = false;

    private ArrayList<BluetoothConnection> mConnections = new ArrayList<BluetoothConnection>();

    private String partialTransmission = "";

    BluetoothFactory(Activity context) {
        ctx = context;
        mBluetooth = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean setup(BluetoothSetupCallback callback) {

        // for the receiver
        setupCallback = callback;

        if (mBluetooth == null){
            callback.newStatus("Bluetooth is not available");
            return false;
        }

        if (!mBluetooth.isEnabled()){

            callback.newStatus("Bluetooth is off");

            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ctx.startActivityForResult(enableBT, REQUEST_ENABLE_BT);
            ctx.registerReceiver(btStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        }
        else {
            isSetup = true;
        }

        return isSetup;
    }


    public void startAccepting(final BluetoothConnectCallback callback) {

        // a new callback to relay the old one, plus a few things

        if (isSetup || setup(new BluetoothSetupCallback() {
            @Override
            public void newStatus(String status) {

                BluetoothFactory.this.newStatus(callback, status);

                if (BluetoothFactory.STATUS_BLUETOOTH_TURNED_ON.equals(status)) {
                    isSetup = true;
                    startAccepting(callback);
                }
            }
        })) {

            newStatus(callback, STATUS_BLUETOOTH_TURNED_ON);
        }
        else {

            // wait for the  bluetooth to turn on
            return;
        }

        newStatus(callback, STATUS_ACCEPTING_CONNECTIONS);

        acceptThread = new AcceptThread(callback);
        acceptThread.start();
    }

    public void sendCommandToDevices(String command) {
        for(BluetoothConnection conn : connectionThreads) {
            conn.writeString(command + ";");
        }
    }

    /*public void toggleDeviceStatus(int deviceI) {

        if (deviceI < 0 && deviceI > isConnected.length)
            return;

        if (!isConnected[deviceI]) {
            Iterator<BluetoothDevice> iterator = paired.iterator();
            BluetoothDevice device = iterator.next();
            int currentDeviceI = 0;

            while (currentDeviceI < deviceI && iterator.hasNext()) {
                device = iterator.next();
                currentDeviceI++;
            }

            newStatus(statusCallback, STATUS_CONNECTING_TO + device.getName(), deviceI);
            new ConnectThread(device, deviceI).start();
        }
        else {
            for (ConnectedThread ct : connectionThreads) {
                if (ct.deviceI == deviceI) {
                    Log.d("MGH", "reseting...");
                    ct.resetConnections();
                    Log.d("MGH", "connections are reset");
                    connectionThreads.remove(ct);
                    isConnected[deviceI] = false;
                    Log.d("MGH", "disconnected");
                    return;
                }
            }
        }
    }*/


    private class AcceptThread extends Thread{
        private BluetoothConnectCallback mCallback;
        public AcceptThread(BluetoothConnectCallback callback){
            mCallback = callback;
            if (mServerSocket == null ) {
                BluetoothServerSocket tmp = null;
                try {
                    tmp =  mBluetooth.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);

                }    catch (IOException e) {
                    newStatus(callback, "IOException in listenUsingRfcomm");
                }
                mServerSocket = tmp;
            }
        }

        public void run(){

            BluetoothSocket socket;
            while (!isInterrupted()){
                try {
                    socket = mServerSocket.accept();
                } catch (IOException e){
                    if (!cleaningUp)
                        newStatus(mCallback, "IOException in accept()");

                    break;
                }

                if (socket != null){
                    newStatus(mCallback, "Connecting...");
                    readSocket(socket.getRemoteDevice(), socket, mCallback);
                }

            }
        }

    }

    void connectToDevice(BluetoothDevice device, BluetoothConnectCallback callback) {
        newStatus(callback, STATUS_CONNECTING_TO);

        new ConnectThread(device, callback).start();
    }

    void checkConnections() {
        ArrayList<BluetoothConnection> toRemove = new ArrayList<>();
        for (BluetoothConnection connection : connectionThreads) {
            if (connection.isDisconnected()) {
                toRemove.add(connection);
            }
        }
        connectionThreads.removeAll(toRemove);
    }

    void connectToPairedDevices(final BluetoothConnectCallback callback) {

        checkConnections();

        boolean isConnected;

        paired = mBluetooth.getBondedDevices();
        Iterator<BluetoothDevice> iterator = paired.iterator();
        while (iterator.hasNext()) {
            BluetoothDevice device = iterator.next();
            isConnected = false;
            for (BluetoothConnection connection : connectionThreads) {
                if (connection.getDevice().getAddress().equals(device.getAddress())) {
                    isConnected = true;
                    break;
                }
            }
            if (!isConnected) {
                newStatus(callback, STATUS_CONNECTING_TO + device.getName());

                new ConnectThread(device, callback).start();

            }
        }
    }


    private class ConnectThread extends Thread {
        BluetoothDevice mDevice;
        BluetoothSocket mSocket;
        BluetoothConnectCallback mConnectCallback;

        public ConnectThread(BluetoothDevice device, BluetoothConnectCallback callback) {
            mConnectCallback = callback;
            BluetoothSocket tmp = null;
            mDevice = device;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                newStatus(mConnectCallback, "IOException in createRfcommSocket");
                Log.d(TAG, e.getMessage());}
            mSocket = tmp;
        }

        public void run() {
            mBluetooth.cancelDiscovery();
            Log.d(TAG, "connectthread");
            boolean good = false;
            try {
                mSocket.connect();
                good = true;

            } catch (IOException connectException) {
                Log.d(TAG, connectException.getMessage());
                newStatus(mConnectCallback, STATUS_IO_CONNECT_THREAD);
            }
            if (good)
                readSocket(mDevice, mSocket, mConnectCallback);
            else {
                try {
                    mSocket.close();
                }
                catch (IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }
    }

    private void readSocket(BluetoothDevice device, BluetoothSocket socket, BluetoothConnectCallback callback){


        BluetoothConnection ct = new BluetoothConnection(device, this, socket, callback);
        connectionThreads.add(ct);

        // if you don't add to the arrayList before you start
        // any Write's that occur on CONNECT will fail
        ct.start();

    }

    private void writeSocket(String toWrite){
        //Log.d(TAG, "write socket");
        //newStatus("write socket");

        //new ConnectedThread(socket).write(toWrite.getBytes());

        for (BluetoothConnection ct : connectionThreads) {
            ct.write(toWrite.getBytes());
        }

    }

    public void writeToBluetooth(String toWrite){
        writeSocket(toWrite);
    }


    public void cleanUp() {
        cleaningUp = true;

        Log.d("MGH", "cleanup 2");
        for (BluetoothConnection ct : connectionThreads) {
            ct.resetConnections();
        }
        connectionThreads.clear();

        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (acceptThread != null && !acceptThread.isInterrupted()){
            Log.d("MGH", "cleanup 3");
            acceptThread.interrupt();
            try {
                acceptThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                //newStatus(statusCallback, "cleanup catch", -1);
            }
        }
        Log.d("MGH", "cleanup 4");
    }

    void newStatus(BluetoothConnectCallback callback, String newString) {
        Log.d("MGH newStatus", newString);

        if (callback != null) {
            callback.newStatus(newString);
        }
    }

    void newData(BluetoothDataCallback callback, String newData) {

        // if this doesn't end with semicolon, save it for when it does
        // total nasty hack for now

        if (!newData.substring(newData.length() - 1).equals(";")) {
            partialTransmission = partialTransmission + newData;
            return;
        }

        String newString = partialTransmission + newData;
        partialTransmission = "";

        if (callback != null) {

            String[] commands = newString.split(";");
            for (String command : commands) {
                String[] nvp = command.split("=");
                if (nvp.length > 1) {
                    callback.newData(nvp[0], nvp[1]);
                }
                else {
                    callback.newData(nvp[0], null);
                }
            }
        }
    }
    BroadcastReceiver btStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null &&
                    BluetoothAdapter.STATE_ON == intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR)) {

                isSetup = true;
                setupCallback.newStatus(STATUS_BLUETOOTH_TURNED_ON);
                context.unregisterReceiver(this);

            }
        }
    };

    public boolean isEnabled() {
        return mBluetooth != null && mBluetooth.isEnabled();
    }

    public ArrayList<BluetoothDevice> getPairedDevices() {

        ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

        paired = mBluetooth.getBondedDevices();
        Iterator<BluetoothDevice> iterator = paired.iterator();

        while (iterator.hasNext()) {
            BluetoothDevice device = iterator.next();
            devices.add(device);
        }
        return devices;
    }

    private abstract class BluetoothSetupCallback {

        abstract void newStatus(String data);

    }

    public ArrayList<BluetoothConnection> getConnections() {
        return connectionThreads;
    }

    public void whenReady(final BluetoothReadyCallback callback) {

        if (isSetup) {
            callback.onReady();
            return;
        }

        boolean lsetup = setup(new BluetoothSetupCallback() {
            @Override
            public void newStatus(String status) {
                //BluetoothFactory.this.newStatus(callback, status);

                if (BluetoothFactory.STATUS_BLUETOOTH_TURNED_ON.equals(status)) {
                    isSetup = true;
                    callback.onReady();
                }
            }
        });
        if (lsetup) {
            callback.onReady();
        }

    }
}
