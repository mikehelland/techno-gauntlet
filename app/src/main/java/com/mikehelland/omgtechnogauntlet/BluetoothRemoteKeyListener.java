package com.mikehelland.omgtechnogauntlet;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnection;
import com.mikehelland.omgtechnogauntlet.jam.OnKeyChangeListener;

/**
 * Created by m on 4/12/18.
 *
 */

public class BluetoothRemoteKeyListener extends OnKeyChangeListener {

    private BluetoothConnection connection;

    BluetoothRemoteKeyListener(BluetoothConnection connection) {
        this.connection = connection;
    }

    @Override
    public void onKeyChange(int key, String source) {
        if (source != null)
            return;

        connection.sendNameValuePair(CommandProcessor.SET_KEY, Integer.toString(key));
    }

    @Override
    public void onScaleChange(int[] scale, String source) {
        if (source != null)
            return;

        StringBuilder sb = new StringBuilder();
        if (scale.length > 0) {
            sb.append(scale[0]);
        }
        for (int i = 1; i < scale.length; i++) {
            sb.append(",");
            sb.append(scale[i]);
        }
        connection.sendNameValuePair(CommandProcessor.SET_SCALE, sb.toString());
    }
}
