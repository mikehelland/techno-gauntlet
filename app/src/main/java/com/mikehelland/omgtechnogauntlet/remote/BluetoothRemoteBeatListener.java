package com.mikehelland.omgtechnogauntlet.remote;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnection;
import com.mikehelland.omgtechnogauntlet.jam.OnBeatChangeListener;

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
        if (source != null)
            return;

        RemoteControlBluetoothHelper.sendNewSubbeatLength(connection, length);
    }

    @Override
    public void onBeatsChange(int beats, String source) {
        if (source != null)
            return;

        RemoteControlBluetoothHelper.sendNewBeats(connection, beats);
    }

    @Override
    public void onMeasuresChange(int measures, String source) {
        if (source != null)
            return;

        RemoteControlBluetoothHelper.sendNewMeasures(connection, measures);
    }

    @Override
    public void onShuffleChange(float shuffle, String source) {
        if (source != null)
            return;

        RemoteControlBluetoothHelper.sendNewShuffle(connection, shuffle);
    }
}
