package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothManager;
import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothReadyCallback;

import java.util.ArrayList;
import java.util.List;

public class BluetoothChooseDeviceFragment extends OMGFragment {

    private View mView;
    private Callback mCallback;
    private List<BluetoothDevice> mPairedList;

    private BluetoothManager bluetoothManager = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.bluetooth_devices,
                container, false);

        if (bluetoothManager != null) {
            bluetoothManager.whenReady(new BluetoothReadyCallback() {
                @Override
                public void onReady() {
                    Activity activity = getActivity();
                    if (activity == null) return;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setup();
                        }
                    });
                }
            });
        }

        return mView;
    }

    void setCallback(BluetoothManager bluetoothManager, Callback callback) {
        this.bluetoothManager = bluetoothManager;
        mCallback = callback;
    }

    private void setup() {

//        Activity activity = getActivity(); if (activity == null)  return;

        ///BluetoothManager bluetoothManager = ((Main)activity).bluetoothManager;
        if (bluetoothManager == null || !bluetoothManager.isBlueToothOn()) {
            //this should only be called if bluetooth is on
            return;
        }

        ListView list = (ListView)mView.findViewById(R.id.paired_device_list);
        ArrayList<String> names = new ArrayList<>();
        mPairedList = bluetoothManager.getPairedDevices();
        for (BluetoothDevice device : mPairedList) {
            names.add(device.getName());
        }


        ArrayAdapter adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, names);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectDevice(i);
            }
        });
    }

    private void selectDevice(int i) {

        if (mCallback != null) {
            mCallback.run(mPairedList.get(i));
            FragmentManager fm = getFragmentManager();
            if (fm != null) {
                fm.popBackStack();
            }
        }
    }


    abstract static class Callback {
        abstract void run(BluetoothDevice device);
    }
}
