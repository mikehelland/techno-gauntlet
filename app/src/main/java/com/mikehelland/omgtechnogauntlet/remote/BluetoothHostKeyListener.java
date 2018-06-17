package com.mikehelland.omgtechnogauntlet.remote;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothManager;
import com.mikehelland.omgtechnogauntlet.jam.OnKeyChangeListener;

/**
 * Created by m on 4/12/18.
 *
 */

public class BluetoothHostKeyListener extends OnKeyChangeListener {

    private BluetoothManager bluetoothManager;

    BluetoothHostKeyListener(BluetoothManager bluetoothManager) {
        this.bluetoothManager = bluetoothManager;
    }
    

    @Override
    public void onKeyChange(int key, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_KEY,
                Integer.toString(key), source);
    }

    @Override
    public void onScaleChange(int[] scale, String source) {
        StringBuilder sb = new StringBuilder();
        if (scale.length > 0) {
            sb.append(scale[0]);
        }
        for (int i = 1; i < scale.length; i++) {
            sb.append(",");
            sb.append(scale[i]);
        }
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.SET_SCALE,
                sb.toString(), source);
    }

}
