package com.mikehelland.omgtechnogauntlet;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.mikehelland.omgtechnogauntlet.jam.Jam;
import com.mikehelland.omgtechnogauntlet.jam.JamPart;

public class OMGFragment extends Fragment {

    protected Jam jam;
    private JamPart part;

    protected Jam getJam() {
        return jam;
    }
    protected JamPart getPart() { return part;}
    void setPart(JamPart part) {
        this.part = part;
    }

    private boolean finished = false;

    protected void animateFragment(OMGFragment f, int direction) {
        f.jam = jam;
        try {
            FragmentManager fm = getFragmentManager();
            if (fm != null) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();

                //0 is right, 1 is down, 2 is left, 3 is up
                /*if (direction == 1) {
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
                }*/
                ft.replace(R.id.main_layout, f);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.addToBackStack(null);
                ft.commit();
            }
        } catch (Exception ignore) {}
    }

    protected void popBackStack() {
        if (finished) return;

        FragmentManager fm = getFragmentManager();
        if (fm != null && fm.getBackStackEntryCount() > 1) {
            fm.popBackStack();
        }

        finished = true;
    }
}
