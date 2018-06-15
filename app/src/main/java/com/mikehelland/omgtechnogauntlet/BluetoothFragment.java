package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnectCallback;
import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnection;
import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothManager;
import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothReadyCallback;
import com.mikehelland.omgtechnogauntlet.jam.Jam;

import java.util.HashMap;
import java.util.List;

//import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothDeviceDataHelper;

public class BluetoothFragment extends OMGFragment {

    private View mView;
    private HashMap<String, BtRelativeLayout> mViewMap = new HashMap<>();
    
    private BluetoothManager bluetoothManager;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.bluetooth_fragment,
                container, false);

        
        bluetoothManager = ((Main)getActivity()).bluetoothManager;

        bluetoothManager.whenReady(new BluetoothReadyCallback() {
            @Override
            public void onReady() {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setup();
                        setupMainButtons();
                        setupBrainList(inflater);
                    }
                });
                if (!bluetoothManager.isAccepting()) {
                    bluetoothManager.startAccepting(makeConnectCallback(null));
                }
                else {
                    bluetoothManager.addAcceptThreadCallback(makeConnectCallback(null));
                }
            }
        });

        return mView;
    }

    private void setupMainButtons() {

        Button addButton = (Button) mView.findViewById(R.id.bt_add_device);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothChooseDeviceFragment f = new BluetoothChooseDeviceFragment();
                f.setCallback(bluetoothManager, new BluetoothChooseDeviceFragment.Callback() {
                    @Override
                    void run(BluetoothDevice device) {
                        addDevice(device);
                    }
                });
                animateFragment(f, 1);
            }
        });
    }

    private void setupBrainList(LayoutInflater inflater) {
        List<String> list = getBrainMACList();
        if (list == null) {
            return;
        }

        ViewGroup viewGroup = (ViewGroup) mView.findViewById(R.id.brain_devices);
        for (String macAddress : list) {
            for (BluetoothDevice device : bluetoothManager.getPairedDevices()) {
                if (macAddress.equals(device.getAddress())) {
                    setupDeviceButton(inflater, viewGroup, device);
                    break;
                }
            }
        }
    }

    private List<String> getBrainMACList() {
        Main activity = (Main)getActivity();
        if (activity == null || activity.getDatabase() == null) {
            return null;
        }

        return activity.getDatabase().mBluetoothDeviceData.getBrainMACList();
    }

    private void setup() {

        bluetoothManager.checkConnections();

    }


    private void connectToDevice(final BluetoothDevice device, final View controls) {
        bluetoothManager.connectTo(device, makeConnectCallback(controls));
    }

    private CommandProcessor setupDataCallBackForConnection(BluetoothConnection connection) {

        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }
        CommandProcessor cp = new CommandProcessor(activity);
        //cp.setup(connection, getPeerJam(), null);
        cp.setup(connection, getJam(), null);
        connection.setDataCallback(cp);

        BtRelativeLayout controls = mViewMap.get(connection.getDevice().getAddress());
        if (controls != null) {
            //cp.setOnPeerChangeListener(makeOnChangeListener(controls));
        }

        return cp;
    }

    private void addDevice(BluetoothDevice device) {
        Main activity = (Main)getActivity();
        if (activity != null) {
            BluetoothDeviceDataHelper data = activity.getDatabase().mBluetoothDeviceData;
            data.addBrainDevice(device);
        }
    }
    private void removeDevice(BluetoothDevice device) {
        Main activity = (Main)getActivity();
        if (activity != null) {
            BluetoothDeviceDataHelper data = activity.getDatabase().mBluetoothDeviceData;
            data.removeBrainDevice(device);
        }
    }

    private void setupDeviceButton(LayoutInflater inflater, final ViewGroup container, final BluetoothDevice device) {
        final BtRelativeLayout controls = (BtRelativeLayout)inflater.inflate(R.layout.bt_device_panel, container, false);
        container.addView(controls);

        ((TextView)controls.findViewById(R.id.bt_device_name)).setText(device.getName());

        // check to see if this device is already connected and setup the panel
        CommandProcessor cp;
        BluetoothConnection connection = getConnectionFromDevice(device);

        if (connection != null && !connection.isDisconnected()) {
            onPanelConnected(controls);
            connection.addConnectedCallback(new BluetoothConnectCallback() {
                @Override
                public void newStatus(String status) {

                }

                @Override
                public void onConnected(BluetoothConnection connection) {

                }

                @Override
                public void onDisconnected(BluetoothConnection connection) {
                    if (mViewMap.containsKey(connection.getDevice().getAddress())) {
                        final BtRelativeLayout view = mViewMap.get(connection.getDevice().getAddress());
                        Activity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    resetPanel(view);
                                }
                            });
                        }
                    }
                }
            });

            //todo
            /*cp = (CommandProcessor)connection.getDataCallback();
            if (cp != null && cp.getPeerJam() != null) {
                //cp.setOnPeerChangeListener(makeOnChangeListener(controls));
                onPanelConnected(controls, cp);
                setPanelInfo(controls, cp.getPeerJam());
                //connection.addConnectedCallback(makeConnectCallback(null));
            }*/
        }


        controls.findViewById(R.id.bt_brain_connect_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToDevice(device, controls);
            }
        });
        controls.findViewById(R.id.bt_brain_connect_button).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                removeDevice(device);
                container.removeView(controls);
                return true;
            }
        });

        controls.findViewById(R.id.remote_control_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                BluetoothConnection connection = getConnectionFromDevice(device);
                ((CommandProcessor)connection.getDataCallback()).setSync(true);
                jam.addOnJamChangeListener(new BluetoothRemoteJamListener(connection));
                jam.addOnKeyChangeListener(new BluetoothRemoteKeyListener(connection));
                jam.addOnBeatChangeListener(new BluetoothRemoteBeatListener(connection));
                jam.addOnMixerChangeListener(new BluetoothRemoteMixerListener(connection));
                RemoteControlBluetoothHelper.setupRemote(connection);
            }
        });

        mViewMap.put(device.getAddress(), controls);
    }

    BluetoothConnectCallback makeConnectCallback(final View view) {
        return new BluetoothConnectCallback() {
            @Override
            public void newStatus(final String status) {
                Activity activity = getActivity();
                if (view != null && activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView)view.findViewById(R.id.bt_device_status)).setText(status);
                        }
                    });
                }
            }
                    @Override
            public void onConnected(final BluetoothConnection connection) {
                final CommandProcessor cp = setupDataCallBackForConnection(connection);
                RemoteControlBluetoothHelper.getJam(connection);
                if (mViewMap.containsKey(connection.getDevice().getAddress())) {
                    final View freshView = mViewMap.get(connection.getDevice().getAddress());
                    Activity activity = getActivity();
                    if (cp != null && activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //onPanelConnected(freshView, cp);
                                onPanelConnected((BtRelativeLayout)freshView);
                            }
                        });
                    }
                }
            }

            public void onDisconnected(final BluetoothConnection connection) {
                if (mViewMap.containsKey(connection.getDevice().getAddress())) {
                    final BtRelativeLayout view = mViewMap.get(connection.getDevice().getAddress());
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                resetPanel(view);
                            }
                        });
                    }
                }
            }
        };
    }

    private void onPanelConnected(BtRelativeLayout controls) {
        if (!controls.getShowDetails()) {
            controls.setShowDetails(true);
            ((TextView)controls.findViewById(R.id.bt_device_status)).setText(R.string.connected);
            controls.findViewById(R.id.bt_brain_connect_button).setVisibility(View.GONE);
            ((ImageView)controls.findViewById(R.id.img_device)).setImageResource(R.drawable.device_blue);
            controls.findViewById(R.id.remote_control_button).setVisibility(View.VISIBLE);
            //controls.findViewById(R.id.peer_jam_controls).setVisibility(View.VISIBLE);
            //controls.findViewById(R.id.sync_button).setVisibility(View.VISIBLE);
            //controls.findViewById(R.id.peer_jam_stoplight).setVisibility(View.VISIBLE);
        }

    }

    /*private CommandProcessor.OnPeerChangeListener makeOnChangeListener(final BtRelativeLayout controls) {
        return new CommandProcessor.OnPeerChangeListener() {

            @Override
            void onChange(final Jam jam) {
                Activity activity = getActivity();
                if (activity == null)
                    return;

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setPanelInfo(controls, jam);
                    }
                });
            }
        };

    }*/

    private void onPanelConnected(View controls, final CommandProcessor cp) {

        controls.findViewById(R.id.tempo_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cp != null && cp.getPeerJam() != null) {
                    getJam().setSubbeatLength(cp.getPeerJam().getSubbeatLength(), null);
                }
            }
        });

        controls.findViewById(R.id.key_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cp != null && cp.getPeerJam() != null) {
                    getJam().setKey(cp.getPeerJam().getKey(), null);
                    getJam().setScale(cp.getPeerJam().getScale(), null);
                }
            }
        });

        final Button syncButton = (Button)controls.findViewById(R.id.sync_button);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (cp.isSynced()) {
                    cp.setSync(false);
                    syncButton.setText(R.string.syncbeats);
                }
                else {
                    cp.setSync(true);
                    syncButton.setText(R.string.unsyncbeats);
                }*/
            }
        });

        final Button redLight = (Button)controls.findViewById(R.id.redlight_button);
        final Button yellowLight = (Button)controls.findViewById(R.id.yellowlight_button);
        final Button greenLight = (Button)controls.findViewById(R.id.greenlight_button);
        redLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redLight.setAlpha(1.0f);
                redLight.setPadding(0, 0, 0, 0);
                yellowLight.setAlpha(0.5f);
                yellowLight.setPadding(5, 5, 5, 5);
                greenLight.setAlpha(0.5f);
                greenLight.setPadding(5, 5, 5, 5);
            }
        });
        yellowLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yellowLight.setAlpha(1.0f);
                yellowLight.setPadding(0, 0, 0, 0);
                redLight.setAlpha(0.5f);
                redLight.setPadding(5, 5, 5, 5);
                greenLight.setAlpha(0.5f);
                greenLight.setPadding(5, 5, 5, 5);
            }
        });
        greenLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                greenLight.setAlpha(1.0f);
                greenLight.setPadding(0, 0, 0, 0);
                yellowLight.setAlpha(0.5f);
                yellowLight.setPadding(5, 5, 5, 5);
                redLight.setAlpha(0.5f);
                redLight.setPadding(5, 5, 5, 5);
            }
        });

        setPanelInfo((BtRelativeLayout)controls, null);
    }

    private void setPanelInfo(BtRelativeLayout controls, Jam jam) {

        //if (jam == null || controls == null) {
        if (controls == null) {
            return;
        }

        /*((Button)controls.findViewById(R.id.tempo_button)).
                setText(String.format("%s bpm", Integer.toString(jam.getBPM())));
        ((Button)controls.findViewById(R.id.key_button)).
                setText(jam.getKeyName());
                */
    }

    private void resetPanel(BtRelativeLayout controls) {
        controls.setShowDetails(false);
        ((TextView)controls.findViewById(R.id.bt_device_status)).setText(R.string.disconnected);
        ((ImageView)controls.findViewById(R.id.img_device)).setImageResource(R.drawable.device);
        controls.findViewById(R.id.bt_brain_connect_button).setVisibility(View.VISIBLE);
        controls.findViewById(R.id.peer_jam_controls).setVisibility(View.GONE);
        controls.findViewById(R.id.sync_button).setVisibility(View.GONE);
        controls.findViewById(R.id.remote_control_button).setVisibility(View.GONE);
    }

    private void chooseHost() {

        BluetoothChooseDeviceFragment f = new BluetoothChooseDeviceFragment();
        f.setCallback(bluetoothManager, new BluetoothChooseDeviceFragment.Callback() {
            @Override
            void run(BluetoothDevice device) {
                ConnectToHostFragment connectToHostFragment = new ConnectToHostFragment();
                connectToHostFragment.setDevice(device);
                animateFragment(connectToHostFragment, 1);
            }
        });
        animateFragment(f, 1);
    }

    private BluetoothConnection getConnectionFromDevice(BluetoothDevice device) {
        for (BluetoothConnection connection : bluetoothManager.getConnections()) {
            if (connection.getDevice().getAddress().equals(device.getAddress())) {
                return connection;
            }
        }
        return null;
    }
}