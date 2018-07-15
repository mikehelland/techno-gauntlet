package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnection;
import com.mikehelland.omgtechnogauntlet.jam.SoundSet;
import com.mikehelland.omgtechnogauntlet.remote.CommandProcessor;
import com.mikehelland.omgtechnogauntlet.remote.OnReceiveSoundSetsListener;
import com.mikehelland.omgtechnogauntlet.remote.RemoteControlBluetoothHelper;

import java.util.ArrayList;

public class SoundSetRemoteFragment extends OMGFragment {

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.choose_soundset_remote,
                container, false);

        Main activity = (Main)getActivity();
        if (activity == null || !activity.isRemote()) {
            return mView;
        }

        final CommandProcessor cp =(CommandProcessor)activity.bluetoothJamStatus.getConnectionToHost().getDataCallback();

        cp.setOnReceiveSoundSetsListener(new OnReceiveSoundSetsListener() {
            @Override
            public void onReceiveSoundSets(ArrayList<SoundSet> soundSets) {
                setup(soundSets);
                cp.setOnReceiveSoundSetsListener(null);
            }
        });

        requestSoundSetsFromHost(activity.bluetoothJamStatus.getConnectionToHost());

        return mView;
    }

    private void requestSoundSetsFromHost(BluetoothConnection connection) {
        // send a message to the host to send us available soundsets
        RemoteControlBluetoothHelper.requestSoundSets(connection);
    }

    private void setup(final ArrayList<SoundSet> soundSets) {

        Activity activity = getActivity(); if (activity == null) return;

        final ListView chordsList = (ListView)mView.findViewById(R.id.soundset_list);
        final SoundSetsAdapter soundSetsAdapter = new SoundSetsAdapter(activity, R.layout.chordoption,
                soundSets);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chordsList.setAdapter(soundSetsAdapter);
            }
        });

        chordsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Main activity = (Main)getActivity();
                if (activity == null || activity.bluetoothJamStatus == null || !activity.bluetoothJamStatus.isRemote()) {
                    return;
                }

                activity.bluetoothJamStatus.getConnectionToHost().sendNameValuePair(CommandProcessor.ADD_PART, "" + soundSets.get(i).getID());
                popBackStack();
            }
        });

    }

}
