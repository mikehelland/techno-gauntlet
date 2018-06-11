package com.mikehelland.omgtechnogauntlet;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnection;
import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.Note;
import com.mikehelland.omgtechnogauntlet.jam.OnBeatChangeListener;
import com.mikehelland.omgtechnogauntlet.jam.OnJamChangeListener;

/**
 * Created by m on 4/12/18.
 *
 */

public class BluetoothRemoteBeatListener extends OnBeatChangeListener {

    private BluetoothConnection connection;

    BluetoothRemoteBeatListener(BluetoothConnection connection) {
        this.connection = connection;
    }

    @Override
    public void onSubbeatLengthChange(int length, String source) {
        RemoteControlBluetoothHelper.sendNewSubbeatLength(connection, length);
    }
}
