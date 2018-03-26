package com.mikehelland.omgtechnogauntlet.jam;

import com.mikehelland.omgtechnogauntlet.dsp.Dac;

import java.util.ArrayList;

/**
 * User: m
 * Date: 11/15/13
 * Time: 1:37 PM
 */
public class OscillatorThread extends Thread {

    private ArrayList<Dac> mDacs;

    public OscillatorThread(ArrayList<Dac> dacs) {

        mDacs = dacs;

    }

    public void run() {

        int i;
        Dac dac;

        //was this: Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
//        setPriority(Process.THREAD_PRIORITY_AUDIO);

        while (!isInterrupted()) {
            for (i = 0; i < mDacs.size(); i++) {
                dac = mDacs.get(i);

                dac.tick();
            }
        }


    }


}
