package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothDevice;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

public class BluetoothBrainFragment extends OMGFragment {

    private View mView;
    private HashMap<String, BtRelativeLayout> mViewMap = new HashMap<>();

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.bluetooth_brains,
                container, false);

        getActivityMembers();


        mBtf.whenReady(new BluetoothReadyCallback() {
            @Override
            public void onReady() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setup();
                        setupAddDeviceButton();
                        setupBrainList(inflater);
                    }
                });
                if (!mBtf.isAccepting()) {
                    mBtf.startAccepting(makeConnectCallback(null));
                }
                else {
                    mBtf.addAcceptThreadCallback(makeConnectCallback(null));
                }
            }
        });

        return mView;
    }

    private void setupAddDeviceButton() {
        Button addButton = (Button) mView.findViewById(R.id.bt_add_device);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Main.MONKEY_TEST) {
                    BluetoothChooseDeviceFragment f = new BluetoothChooseDeviceFragment();
                    f.setCallback(new BluetoothChooseDeviceFragment.Callback() {
                        @Override
                        void run(BluetoothDevice device) {
                            addDevice(device);
                        }
                    });
                    showFragmentDown(f);
                }
            }
        });
    }

    private void setupBrainList(LayoutInflater inflater) {
        Cursor cursor = getBrainsCursor();
        ViewGroup viewGroup = (ViewGroup) mView.findViewById(R.id.brain_devices);
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);

            for (BluetoothDevice device : mBtf.getPairedDevices()) {
                if (cursor.getString(cursor.getColumnIndex("mac")).equals(device.getAddress())) {
                    setupDeviceButton(inflater, viewGroup, device);
                    break;
                }
            }
        }
        cursor.close();
    }

    private Cursor getBrainsCursor() {
        BluetoothDeviceDataHelper data = ((Main)getActivity()).getDatabase().mBluetoothDeviceData;
        return data.getBrainsCursor();
    }

    private void setup() {

        mBtf.checkConnections();

    }


    private void connectToDevice(final BluetoothDevice device, final View controls) {
        mBtf.connectTo(device, makeConnectCallback(controls));
    }

    private CommandProcessor setupDataCallBackForConnection(BluetoothConnection connection) {

        CommandProcessor cp = (CommandProcessor) connection.getDataCallback();
        if (cp == null) {
            Activity activity = getActivity();
            if (activity == null) {
                return null;
            }
            cp = new CommandProcessor(activity);
            cp.setup(connection, mJam, null);
            connection.setDataCallback(cp);
        }

        BtRelativeLayout controls = mViewMap.get(connection.getDevice().getAddress());
        if (controls != null) {
            cp.setOnPeerChangeListener(makeOnChangeListener(controls));
        }

        return cp;
    }

    public void showFragmentDown(Fragment f) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.animator.slide_in_down,
                R.animator.slide_out_up,
                R.animator.slide_in_up,
                R.animator.slide_out_down
        );
        ft.replace(R.id.main_layout, f);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void addDevice(BluetoothDevice device) {
        Main activity = (Main)getActivity();
        if (activity != null) {
            BluetoothDeviceDataHelper data = activity.getDatabase().mBluetoothDeviceData;
            data.addBrainDevice(device);
        }
    }

    private void setupDeviceButton(LayoutInflater inflater, ViewGroup container, final BluetoothDevice device) {
        final BtRelativeLayout controls = (BtRelativeLayout)inflater.inflate(R.layout.bt_device_panel, container, false);
        container.addView(controls);

        ((TextView)controls.findViewById(R.id.bt_device_name)).setText(device.getName());

        CommandProcessor cp;
        for (BluetoothConnection connection : mBtf.getConnections()) {
            if (connection.getDevice().getAddress().equals(device.getAddress())) {

                if (!connection.isDisconnected()) {
                    cp = (CommandProcessor)connection.getDataCallback();
                    if (cp != null && cp.getJam() != null) {
                        cp.setOnPeerChangeListener(makeOnChangeListener(controls));
                        onPanelConnected(controls, cp);
                        setPanelInfo(controls, cp.getJam());
                        connection.addConnectedCallback(makeConnectCallback(null));
                    }
                }
            }
        }

        controls.findViewById(R.id.bt_brain_connect_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToDevice(device, controls);
            }
        });

        mViewMap.put(device.getAddress(), controls);
    }

    BluetoothConnectCallback makeConnectCallback(final View view) {
        return new BluetoothConnectCallback() {
            @Override
            public void newStatus(final String status) {
                if (view != null && getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView)view.findViewById(R.id.bt_device_status)).setText(status);
                        }
                    });
                }
            }
                    @Override
            public void onConnected(BluetoothConnection connection) {
                final CommandProcessor cp = setupDataCallBackForConnection(connection);
                if (mViewMap.containsKey(connection.getDevice().getAddress())) {
                    final View freshView = mViewMap.get(connection.getDevice().getAddress());
                    if (cp != null && getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onPanelConnected(freshView, cp);
                            }
                        });
                    }
                }
            }

            public void onDisconnected(final BluetoothConnection connection) {
                if (mViewMap.containsKey(connection.getDevice().getAddress())) {
                    final BtRelativeLayout view = mViewMap.get(connection.getDevice().getAddress());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
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

    private CommandProcessor.OnPeerChangeListener makeOnChangeListener(final BtRelativeLayout controls) {
        return new CommandProcessor.OnPeerChangeListener() {

            @Override
            void onChange(final JamInfo jam) {
                if (getActivity() == null)
                    return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setPanelInfo(controls, jam);
                    }
                });
            }
        };

    }

    private void onPanelConnected(View controls, final CommandProcessor cp) {

        controls.findViewById(R.id.tempo_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mJam.setSubbeatLength(cp.getJam().getSubbeatLength());
            }
        });

        controls.findViewById(R.id.key_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mJam.setKey(cp.getJam().getKey());
                mJam.setScale(cp.getJam().getScale());
            }
        });

        final Button syncButton = (Button)controls.findViewById(R.id.sync_button);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cp.isSynced()) {
                    cp.setSync(false);
                    syncButton.setText(R.string.syncbeats);
                }
                else {
                    cp.setSync(true);
                    syncButton.setText(R.string.unsyncbeats);
                }
            }
        });
    }

    private void setPanelInfo(BtRelativeLayout controls, JamInfo jam) {

        if (jam == null || controls == null) {
            return;
        }

        if (!controls.getShowDetails()) {
            controls.setShowDetails(true);
            ((TextView)controls.findViewById(R.id.bt_device_status)).setText(R.string.connected);
            controls.findViewById(R.id.bt_brain_connect_button).setVisibility(View.GONE);
            ((ImageView)controls.findViewById(R.id.img_device)).setImageResource(R.drawable.device_blue);
            controls.findViewById(R.id.peer_jam_controls).setVisibility(View.VISIBLE);
            controls.findViewById(R.id.sync_button).setVisibility(View.VISIBLE);
        }

        ((Button)controls.findViewById(R.id.tempo_button)).
                setText(String.format("%s bpm", Integer.toString(JamInfo.getBPM(jam))));
        ((Button)controls.findViewById(R.id.key_button)).
                setText(JamInfo.getKeyName(jam));
    }

    private void resetPanel(BtRelativeLayout controls) {
        controls.setShowDetails(false);
        ((TextView)controls.findViewById(R.id.bt_device_status)).setText(R.string.disconnected);
        ((ImageView)controls.findViewById(R.id.img_device)).setImageResource(R.drawable.device);
        controls.findViewById(R.id.bt_brain_connect_button).setVisibility(View.VISIBLE);
        controls.findViewById(R.id.peer_jam_controls).setVisibility(View.GONE);
        controls.findViewById(R.id.sync_button).setVisibility(View.GONE);
    }
}