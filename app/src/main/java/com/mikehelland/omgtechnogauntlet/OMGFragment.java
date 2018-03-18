package com.mikehelland.omgtechnogauntlet;

//import android.support.v4.app.Fragment;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

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
        Activity activity = getActivity(); if (activity == null) return null;
        return ((Main)activity).mJam;
    }

    protected void setJam(Jam jam) {
        Activity activity = getActivity(); if (activity == null) return;
        ((Main)activity).mJam = jam;
    }

    protected void showFragmentRight(Fragment f) {

        try {
            FragmentManager fm = getFragmentManager();
            if (fm != null) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.animator.slide_in_right,
                        R.animator.slide_out_left,
                        R.animator.slide_in_left,
                        R.animator.slide_out_right
                );
                ft.replace(R.id.main_layout, f);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.addToBackStack(null);
                ft.commit();
            }
        } catch (Exception ignore) {}
    }
}
