package com.mikehelland.omgtechnogauntlet;

import android.content.Context;

class DatabaseContainer {
    SoundSetDataOpenHelper mSoundSetData;
    SavedDataOpenHelper mSavedData;

    DatabaseContainer(Context context) {

        //deleteDatabase("OMG_TECHNO_GAUNTLET"); // soundsets
        //deleteDatabase("OMG_SURFACES");
        //deleteDatabase("OMG_BT_DEVICE");

        mSoundSetData = new SoundSetDataOpenHelper(context);
        mSoundSetData.updatePresetResource();

        mSavedData = new SavedDataOpenHelper(context);

    }

    void close() {
        mSoundSetData.cleanUp();
        mSavedData.cleanUp();
    }

    SoundSetDataOpenHelper getSoundSetData() {
        return mSoundSetData;
    }
    SavedDataOpenHelper getSavedData() {return mSavedData;}
}
