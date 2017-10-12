package com.mikehelland.omgtechnogauntlet;

//import android.support.v4.app.Fragment;
import android.app.Fragment;
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
        return ((Main)getActivity()).mJam;
    }

    protected void setJam(Jam jam) {
        ((Main)getActivity()).mJam = jam;
    }


    public void showFragmentRight(Fragment f) {


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

}
