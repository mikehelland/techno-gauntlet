package com.mikehelland.omgtechnogauntlet;

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
    private HashMap<String, View> mViewMap = new HashMap<>();

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
                mBtf.newAcceptThreadCallback(makeConnectCallback(null));
            }
        });

        return mView;
    }

    private void setupAddDeviceButton() {
        Button addButton = (Button) mView.findViewById(R.id.bt_add_device);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothChooseDeviceFragment f = new BluetoothChooseDeviceFragment();
                f.setCallback(new BluetoothChooseDeviceFragment.Callback() {
                    @Override
                    void run(BluetoothDevice device) {
                        addDevice(device);
                    }
                });
                showFragmentDown(f);
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
    }

    private Cursor getBrainsCursor() {
        BluetoothDeviceDataHelper data = new BluetoothDeviceDataHelper(getActivity());
        return data.getBrainsCursor();
    }

    private void setup() {

        mBtf.checkConnections();

    }


    private void connectToDevice(final BluetoothDevice device, final View controls) {
        mBtf.connectTo(device, makeConnectCallback(controls));
    }

    private CommandProcessor setupDataCallBackForConnection(BluetoothConnection connection) {
        CommandProcessor cp = new CommandProcessor();

        View controls = mViewMap.get(connection.getDevice().getAddress());
        if (controls != null) {
            cp.setOnPeerChangeListener(makeOnChangeListener(controls));
        }

        cp.setup(getActivity(), connection, mJam, null);
        connection.setDataCallback(cp);
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
        BluetoothDeviceDataHelper data = new BluetoothDeviceDataHelper(getActivity());
        data.addBrainDevice(device);
    }

    private void setupDeviceButton(LayoutInflater inflater, ViewGroup container, final BluetoothDevice device) {
        final View controls = inflater.inflate(R.layout.bt_device_panel, container, false);
        container.addView(controls);

        ((TextView)controls.findViewById(R.id.bt_device_name)).setText(device.getName());

        CommandProcessor cp;
        for (BluetoothConnection connection : mBtf.getConnections()) {
            if (connection.getDevice().getAddress().equals(device.getAddress())) {

                if (!connection.isDisconnected()) {
                    cp = (CommandProcessor)connection.getDataCallback();
                    cp.setOnPeerChangeListener(makeOnChangeListener(controls));
                    onPanelConnected(controls, cp);
                    setPanelInfo(controls, cp.getJam());
                    connection.setConnectedCallback(makeConnectCallback(null));
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
                    if (getActivity() != null) {
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
                    final View view = mViewMap.get(connection.getDevice().getAddress());
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

    private CommandProcessor.OnPeerChangeListener makeOnChangeListener(final View controls) {
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
        ((TextView)controls.findViewById(R.id.bt_device_status)).setText(R.string.connected);
        controls.findViewById(R.id.bt_brain_connect_button).setVisibility(View.GONE);
        ((ImageView)controls.findViewById(R.id.img_device)).setImageResource(R.drawable.device_blue);
        controls.findViewById(R.id.peer_jam_controls).setVisibility(View.VISIBLE);

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
    }

    private void setPanelInfo(View controls, JamInfo jam) {

        ((Button)controls.findViewById(R.id.tempo_button)).
                setText(String.format("%s bpm", Integer.toString(JamInfo.getBPM(jam))));
        ((Button)controls.findViewById(R.id.key_button)).
                setText(JamInfo.getKeyName(jam));
    }

    private void resetPanel(View controls) {
        ((TextView)controls.findViewById(R.id.bt_device_status)).setText(R.string.disconnected);
        ((ImageView)controls.findViewById(R.id.img_device)).setImageResource(R.drawable.device);
        controls.findViewById(R.id.bt_brain_connect_button).setVisibility(View.VISIBLE);
        controls.findViewById(R.id.peer_jam_controls).setVisibility(View.GONE);
    }
}