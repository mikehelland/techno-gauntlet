package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnectCallback;
import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnection;
import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothManager;
import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothReadyCallback;
import com.mikehelland.omgtechnogauntlet.remote.CommandProcessor;
import com.mikehelland.omgtechnogauntlet.remote.JamListenersHelper;
//import com.mikehelland.omgtechnogauntlet.remote.RemoteControlBluetoothHelper;


public class ConnectToHostFragment extends OMGFragment {

    private View mView;
    private TextView mStatusText;
    private ImageView mImageView;
    private BluetoothManager mBT;
    private BluetoothDevice bluetoothDevice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.bluetooth_remote,
                container, false);

        mBT = new BluetoothManager(getActivity()); //activity.mBT;

        mStatusText = (TextView)mView.findViewById(R.id.bt_status);
        mImageView = (ImageView)mView.findViewById(R.id.remote_logo);

        //todo test to see if we're already connected, and wait five seconds and go back
        /*if (jamConnection != null && jamConnection.getCommandProcessor() != null && !jamConnection.getCommandProcessor().isDisconnected()) {
            mStatusText.setText(R.string.connected);
            mImageView.setImageResource(R.drawable.device_blue);
            showRemoteControlFragmentAfterDelay(500);
        }*/

        mBT.whenReady(new BluetoothReadyCallback() {
            @Override
            public void onReady() {
                setup();
            }
        });

        return mView;
    }

    private void setup() {
        final Activity activity = getActivity();
        mStatusText.setText(R.string.looking_for_device);

        Animation turnin = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
        turnin.setDuration(4000);
        turnin.setRepeatCount(100);
        mImageView.startAnimation(turnin);


        /*final String address = PreferenceManager.
                getDefaultSharedPreferences(activity).getString("default_host", "");
        String name = PreferenceManager.
                getDefaultSharedPreferences(activity).getString("default_host_name", "");

        String device = "(No Device Chosen)";
        if (address.length() > 0) {
            device = name;
            connectToHost(address);
        }
        */
        connectToHost(bluetoothDevice);
        ((TextView)mView.findViewById(R.id.bt_host)).setText(bluetoothDevice.getName());

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost(bluetoothDevice);
            }
        });
    }

    private void connectToHost(BluetoothDevice device) {

        /*BluetoothDevice device = null;
        for (BluetoothDevice bd : mBT.getPairedDevices()) {
            if (bd.getAddress().equals(address)) {
                device = bd;
            }
        }*/

        if (device == null) {
            mStatusText.setText(String.format(getString(R.string.device_not_paired), device.getName()));
            return;
        }

        mBT.connectTo(device, new BluetoothConnectCallback() {
            @Override
            public void newStatus(final String status) {
                if (getActivity() == null)
                    return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //todo wouldn't this be on disconnected?
                        /*if (status.equals(BluetoothManager.STATUS_IO_CONNECTED_THREAD)) {
                            mImageView.setImageResource(R.drawable.device);
                            mStatusText.setText(R.string.accepting_connections);
                            FragmentManager fm = getFragmentManager();
                            if (fm != null) {
                                int stackCount = fm.getBackStackEntryCount();
                                for (int i = 0; i < stackCount; i++) {
                                    popBackStack();
                                }
                            }
                        }
                        else {
                            mStatusText.setText(status);
                        }*/
                        mStatusText.setText(status);

                    }
                });
            }

            @Override
            public void onConnected(final BluetoothConnection connection) {

                Activity activity = getActivity(); if (activity == null) return;

                //process any incoming messages from this connection
                final CommandProcessor cp = new CommandProcessor(activity);
                cp.setSync(true); 
                cp.setup(connection, jam, null);
                connection.setDataCallback(cp);

                //send any changes to this jam to the host
                JamListenersHelper.setJamListenersForRemote(jam, connection);


                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStatusText.setText(R.string.getting_jam_info);
                        mImageView.setImageResource(R.drawable.device_blue);
                        //todo RemoteControlBluetoothHelper.setupRemote(connection);

                        //todo get the jam as json from the host
                        //load it, and show the main fragment

                    }
                });
            }

            @Override
            public void onDisconnected(BluetoothConnection connection) {
                //todo remove the listeners, at least
            }
        });
    }

    private void showRemoteControlFragment() {
        //todo?
        //RemoteControlFragment f = new RemoteControlFragment();
        //animateFragment(f, 0);
    }


    private void showRemoteControlFragmentAfterDelay(final int delay) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                showRemoteControlFragment();
            }
        }).start();
    }

    public void setDevice(BluetoothDevice device) {
        bluetoothDevice = device;
    }
}