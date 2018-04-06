package com.mikehelland.omgtechnogauntlet;

//todo since android is deprecating the main Fragment, use the support Fragment
//import android.support.v4.app.Fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothManager;
import com.mikehelland.omgtechnogauntlet.jam.Jam;
import com.mikehelland.omgtechnogauntlet.jam.Part;

public class OMGFragment extends Fragment{

    protected Jam jam;
    protected BluetoothManager mBtf;
    //protected Jam.StateChangeCallback mJamCallback;
    private Part part;

    protected void getActivityMembers() {

        Main main = ((Main)getActivity());
        //mJam = main.mJam;
        mBtf = main.mBtf;
        //mJamCallback = main.mJamCallback;
    }

    protected Jam getJam() {
        return jam;
    }
    protected Part getPart() { return part;}
    void setPart(Part part) {
        this.part = part;
    }

    protected void animateFragment(OMGFragment f, int direction) {
        f.jam = jam;
        try {
            FragmentManager fm = getFragmentManager();
            if (fm != null) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();

                //0 is right, 1 is down, 2 is left, 3 is up
                if (direction == 1) {
                    ft.setCustomAnimations(R.animator.slide_in_down, R.animator.slide_out_up,
                            R.animator.slide_in_up, R.animator.slide_out_down
                    );
                }
                else if (direction == 2) {
                    ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right,
                            R.animator.slide_in_right, R.animator.slide_out_left
                    );
                } if (direction == 3) {
                    ft.setCustomAnimations(R.animator.slide_in_up, R.animator.slide_out_down,
                            R.animator.slide_in_down, R.animator.slide_out_up
                    );
                }
                else {
                    ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left,
                            R.animator.slide_in_left, R.animator.slide_out_right
                    );
                }
                ft.replace(R.id.main_layout, f);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.addToBackStack(null);
                ft.commit();
            }
        } catch (Exception ignore) {}
    }
}
