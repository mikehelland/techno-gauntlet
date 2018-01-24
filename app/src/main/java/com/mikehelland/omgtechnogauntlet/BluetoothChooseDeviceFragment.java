package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class BluetoothChooseDeviceFragment extends OMGFragment {

    private View mView;
    private Callback mCallback;
    private List<BluetoothDevice> mPairedList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.bluetooth_devices,
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

    void setCallback(Callback callback) {
        mCallback = callback;
    }

    private void setup() {

        ListView list = (ListView)mView.findViewById(R.id.paired_device_list);
        ArrayList<String> names = new ArrayList<>();
        mPairedList = mBtf.getPairedDevices();
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
            Activity activity = getActivity();
            if (activity != null) {
                getActivity().getFragmentManager().popBackStack();
            }
        }
    }


    abstract static class Callback {
        abstract void run(BluetoothDevice device);
    }
}
