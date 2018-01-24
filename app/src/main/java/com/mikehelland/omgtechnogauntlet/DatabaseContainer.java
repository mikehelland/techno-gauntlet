package com.mikehelland.omgtechnogauntlet;

import android.content.Context;

class DatabaseContainer {
    SoundSetDataOpenHelper mSoundSetData;
    SavedDataOpenHelper mSavedData;
    final BluetoothDeviceDataHelper mBluetoothDeviceData;

    DatabaseContainer(Context context) {

        //deleteDatabase("OMG_TECHNO_GAUNTLET"); // soundsets
        //deleteDatabase("OMG_SURFACES");
        //context.deleteDatabase("OMG_BT_DEVICE");

        mSoundSetData = new SoundSetDataOpenHelper(context);
        mSoundSetData.updatePresetResource();

        mSavedData = new SavedDataOpenHelper(context);

        mBluetoothDeviceData = new BluetoothDeviceDataHelper(context);
    }

    void close() {
        mSoundSetData.cleanUp();
        mSavedData.cleanUp();
        mBluetoothDeviceData.cleanUp();
    }

    SoundSetDataOpenHelper getSoundSetData() {
        return mSoundSetData;
    }
    SavedDataOpenHelper getSavedData() {return mSavedData;}
}
