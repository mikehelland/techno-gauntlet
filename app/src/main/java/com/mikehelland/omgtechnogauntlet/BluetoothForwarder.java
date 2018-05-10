package com.mikehelland.omgtechnogauntlet;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothManager;
import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.OnJamChangeListener;

/**
 * Created by m on 4/12/18.
 *
 */

public class BluetoothForwarder extends OnJamChangeListener {
    
    private BluetoothManager bluetoothManager;
    
    BluetoothForwarder(BluetoothManager bluetoothManager) {
        this.bluetoothManager = bluetoothManager;
    }
    
    @Override
    public void onSubbeatLengthChange(int length, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.JAMINFO_SUBBEATLENGTH,
                Integer.toString(length), source);
    }

    @Override
    public void onKeyChange(int key, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.JAMINFO_KEY,
                Integer.toString(key), source);
    }

    @Override
    public void onScaleChange(String scale, String source) {
        bluetoothManager.sendNameValuePairToDevices(CommandProcessor.JAMINFO_SCALE,
                scale, source);
    }

    @Override
    public void onChordProgressionChange(int[] chords) {

    }

    @Override
    public void onNewPart(JamPart part) {
        bluetoothManager.sendCommandToDevices(CommandHelper.getNewPartCommand(part), null);
    }

    @Override
    public void onPartEnabledChanged(JamPart part, boolean enabled, String source) {
        bluetoothManager.sendCommandToDevices(
                CommandHelper.getPartEnabledCommand(part.getId(), enabled), source);
    }

    @Override
    public void onPartVolumeChanged(JamPart part, float volume, String source) {
        bluetoothManager.sendCommandToDevices(
                CommandHelper.getPartVolumeCommand(part.getId(), volume), source);
    }

    @Override
    public void onPartPanChanged(JamPart part, float pan, String source) {
        bluetoothManager.sendCommandToDevices(
                CommandHelper.getPartPanCommand(part.getId(), pan), source);
    }

    @Override
    public void newState(String state, Object... args) {
        if (state.equals("PLAY") || state.equals("STOP"))
            bluetoothManager.sendCommandToDevices(state, null);

        if (state.equals("ON_NEW_LOOP"))
            bluetoothManager.sendCommandToDevices(state, null);

    }
}
