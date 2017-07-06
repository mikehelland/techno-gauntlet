package com.mikehelland.omgtechnogauntlet;


import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;


public class BluetoothRemoteFragment extends OMGFragment {

    private BluetoothConnection mConnection;
    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.bluetooth_remote,
                container, false);

        getActivityMembers();

        final TextView statusView = (TextView)mView.findViewById(R.id.bt_status);

        final ImageView spinningImage = (ImageView)mView.findViewById(R.id.remote_logo);
        Animation turnin = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
        turnin.setDuration(4000);
        turnin.setRepeatCount(100);
        spinningImage.startAnimation(turnin);

        mBtf.startAccepting(new BluetoothConnectCallback() {
            @Override
            public void newStatus(final String status) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusView.setText(status);
                    }
                });
            }


            @Override
            public void onConnected(BluetoothConnection connection) {
                mConnection = connection;

                connection.setDataCallback(new BluetoothDataCallback() {
                    @Override
                    public void newData(String name, String value) {

                            processCommand(name, value);

                    }
                });

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusView.setText("Connected");
                        spinningImage.setImageResource(R.drawable.device_blue);
                    }
                });

            }
        });


        return mView;
    }

    private void launchFretboard(int low, int high, int octave) {
        GuitarFragment f = new GuitarFragment();
        BluetoothChannel channel = new BluetoothChannel(getActivity(), mJam, mPool, mConnection);
        channel.setLowHigh(low, high, octave);
        f.setJam(mJam, channel);

        showFragment(f);
    }

    private void launchDrumpad(boolean[][] pattern) {
        Log.d("MGH", "launch drum pad");
        BluetoothDrumChannel channel = new BluetoothDrumChannel(getActivity(), mPool, mJam, mConnection);
        channel.setPattern(pattern);

        DrumFragment f = new DrumFragment();
        f.setJam(mJam, channel);

        showFragment(f);
    }

    private void showFragment(Fragment f) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.animator.slide_in_left,
                R.animator.slide_out_right,
                R.animator.slide_in_right,
                R.animator.slide_out_left

                //R.anim.slide_in_up,
               // R.anim.slide_out_up,
                //R.anim.slide_in_down,
                //R.anim.slide_out_down
        );
        ft.replace(R.id.main_layout, f);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void processCommand(String name, String value) {

        if ("LAUNCH_FRETBOARD".equals(name)) {
            String[] lowhigh = value.split(",");
            launchFretboard(Integer.parseInt(lowhigh[0]), Integer.parseInt(lowhigh[1]),
                    Integer.parseInt(lowhigh[2]));

        }
        else if ("LAUNCH_DRUMPAD".equals(name)) {;
            boolean[][] pattern;
            try {
                Log.d("MGH launch drumpad", value.substring(value.length() - 20));
                JSONArray jsonPattern = new JSONArray(value);
                JSONArray jsonTrackPattern;
                pattern = new boolean[jsonPattern.length()][];
                for (int i = 0; i < jsonPattern.length(); i++) {
                    jsonTrackPattern = jsonPattern.getJSONArray(i);
                    pattern[i] = new boolean[jsonTrackPattern.length()];
                    for (int j = 0; j < jsonTrackPattern.length(); j++) {
                        pattern[i][j] = jsonTrackPattern.getBoolean(j);
                    }
                }

                launchDrumpad(pattern);

            }
            catch (JSONException ex) {

                Log.d("MGH launch drumpad", "json exception");

            }

        }
        else if (name.equals("JAM_SET_KEY")) {
            mJam.setKey(Integer.parseInt(value));
        }
        else if (name.equals("JAM_SET_SCALE")) {
            mJam.setScale(Integer.parseInt(value));
        }


    }
}