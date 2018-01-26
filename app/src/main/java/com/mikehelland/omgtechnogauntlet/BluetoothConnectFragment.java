package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class BluetoothConnectFragment extends OMGFragment {

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.bluetooth_connect,
                container, false);

        getActivityMembers();

        mBtf.whenReady(new BluetoothReadyCallback() {
            @Override
            public void onReady() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setup();
                    }
                });
            }
        });

        return mView;
    }

    private void setup() {
        ((TextView)mView.findViewById(R.id.bt_status)).setText("Paired Devices:");

        ViewGroup pairedDevicesViewGroup = (ViewGroup)mView.findViewById(R.id.paired_devices);

        mBtf.checkConnections();

        for (BluetoothDevice device : mBtf.getPairedDevices()) {
            pairedDevicesViewGroup.addView(setupDeviceButton(device));
        }
    }

    private Button setupDeviceButton(final BluetoothDevice device) {
        boolean connected = false;

        final Button button = new Button(getActivity());
        button.setText(device.getName() + " (?)");
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.device, 0, 0, 0);

        for (BluetoothConnection connection : mBtf.getConnections()) {
            if (!connection.isDisconnected() &&
                    connection.getDevice().getAddress().equals(device.getAddress())) {
                connected = true;
                button.setText(device.getName() + "\n(connected)");
                button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.device_blue, 0, 0, 0);
            }
        }

        if (!connected) {
            connectToDevice(device, button);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button.setText(device.getName() + "\n(?)");
                connectToDevice(device, button);
            }
        });
        return button;
    }

    private void connectToDevice(final BluetoothDevice device, final Button button) {
        mBtf.connectTo(device, new BluetoothConnectCallback() {
            @Override
            public void newStatus(final String status) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            button.setText(device.getName() + "\n(" + status + ")");
                            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.device,
                                    0, 0, 0);
                        }
                    });
                }
            }

            @Override
            public void onConnected(BluetoothConnection connection) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            button.setText(device.getName() + "\n(connected)");
                            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.device_blue,
                                    0, 0, 0);
                        }
                    });
                }

                setupDataCallBackForConnection(connection);
            }

            @Override
            public void onDisconnected(BluetoothConnection connection) {

            }
        });
    }

    private void setupDataCallBackForConnection(BluetoothConnection connection) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        CommandProcessor cp = new CommandProcessor(activity);
        cp.setup(connection, mJam, null);
        connection.setDataCallback(cp);
    }
}
