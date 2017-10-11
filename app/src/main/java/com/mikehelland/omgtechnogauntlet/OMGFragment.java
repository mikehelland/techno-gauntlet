package com.mikehelland.omgtechnogauntlet;

//import android.support.v4.app.Fragment;
import android.app.Fragment;

public class OMGFragment extends Fragment{

    protected Jam mJam;
    protected OMGSoundPool mPool;
    protected BluetoothManager mBtf;
    protected Jam.StateChangeCallback mJamCallback;

    protected void getActivityMembers() {

        Main main = ((Main)getActivity());
        mJam = main.mJam;
        mPool = main.mPool;
        mBtf = main.mBtf;
        mJamCallback = main.mJamCallback;
    }

    protected Jam getJam() {
        return ((Main)getActivity()).mJam;
    }

    protected void setJam(Jam jam) {
        ((Main)getActivity()).mJam = jam;
    }



}
