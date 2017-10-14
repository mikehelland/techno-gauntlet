package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

class BluetoothManager {

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
    private final Activity ctx;

    private final static String TAG = "MGH Bluetooth";

    private BluetoothReadyCallback readyCallback;

    private ArrayList<BluetoothConnection> connectionThreads = new ArrayList<>();
    private BluetoothAcceptThread acceptThread;

    private String partialTransmission = "";

    boolean cleaningUp = false;

    BluetoothManager(Activity context) {
        ctx = context;
        mBluetooth = BluetoothAdapter.getDefaultAdapter();
    }

    void whenReady(BluetoothReadyCallback callback) {
        readyCallback = callback;

        if (!isBlueToothOn()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ctx.startActivityForResult(enableBT, REQUEST_ENABLE_BT);
            ctx.registerReceiver(btStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        }
        else {
            callback.onReady();
        }
    }

    private BroadcastReceiver btStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null &&
                    BluetoothAdapter.STATE_ON == intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR)) {
                readyCallback.onReady();
                context.unregisterReceiver(this);
            }
        }
    };

    boolean isBlueToothOn() {
        return mBluetooth != null && mBluetooth.isEnabled();
    }

    void startAccepting(final BluetoothConnectCallback callback) {
        if (!isBlueToothOn()) return;
        newStatus(callback, STATUS_ACCEPTING_CONNECTIONS);

        acceptThread = new BluetoothAcceptThread(this, callback);
        acceptThread.start();
    }

    void newAcceptThreadCallback(BluetoothConnectCallback callback) {
        if (acceptThread != null) {
            acceptThread.newCallback(callback);
        }
    }

    void connectTo(BluetoothDevice device, BluetoothConnectCallback callback) {
        if (!isBlueToothOn()) return;
        newStatus(callback, STATUS_CONNECTING_TO);

        new BluetoothConnectThread(this, device, callback).start();
    }

    BluetoothAdapter getAdapter() {
        return mBluetooth;
    }

    UUID getUUID() {
        return MY_UUID;
    }

    void sendCommandToDevices(String command, String exceptAddress) {
        for(BluetoothConnection conn : connectionThreads) {
            if (exceptAddress == null || !conn.getDevice().getAddress().equals(exceptAddress))
                conn.sendCommand(command);
        }
    }

    void sendNameValuePairToDevices(String name, String value, String exceptAddress) {
        for(BluetoothConnection conn : connectionThreads) {
            if (exceptAddress == null || !conn.getDevice().getAddress().equals(exceptAddress))
                conn.sendNameValuePair(name, value);
        }
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

    void newConnection(BluetoothDevice device, BluetoothSocket socket, BluetoothConnectCallback callback){
        BluetoothConnection ct = new BluetoothConnection(device, this, socket, callback);
        connectionThreads.add(ct);

        // if you don't add to the arrayList before you start
        // any Write's that occur on CONNECT will fail
        ct.start();
    }

    void cleanUp() {
        cleaningUp = true;
        Log.d("MGH bt cleanup", "start");
        /*if (acceptThread != null && !acceptThread.isInterrupted()){
            acceptThread.interrupt();
            try {
                acceptThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                //newStatus(statusCallback, "cleanup catch", -1);
            }
        }
        acceptThread = null;*/
        if (acceptThread != null) {
            acceptThread.stopAccepting();
            acceptThread = null;
        }
        Log.d("MGH bt cleanup", "acceptThread null");

        for (BluetoothConnection ct : connectionThreads) {
            ct.resetConnections();
        }
        connectionThreads.clear();
        Log.d("MGH bt cleanup", "connections cleared");
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

    ArrayList<BluetoothDevice> getPairedDevices() {
        Set<BluetoothDevice> set = mBluetooth.getBondedDevices();
        ArrayList<BluetoothDevice> list = new ArrayList<>();
        for (BluetoothDevice device : set) {
            list.add(device);
        }
        return list;
    }

    ArrayList<BluetoothConnection> getConnections() {
        return connectionThreads;
    }

    boolean isAccepting() {
        return (acceptThread != null && acceptThread.isAlive());
    }
}