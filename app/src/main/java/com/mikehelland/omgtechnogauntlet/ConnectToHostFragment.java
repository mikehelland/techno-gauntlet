package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnectCallback;
import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnection;
import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothManager;
import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothReadyCallback;
import com.mikehelland.omgtechnogauntlet.jam.Jam;
import com.mikehelland.omgtechnogauntlet.jam.JamHeader;
import com.mikehelland.omgtechnogauntlet.jam.JamPart;
import com.mikehelland.omgtechnogauntlet.jam.Note;
import com.mikehelland.omgtechnogauntlet.jam.OnJamChangeListener;
import com.mikehelland.omgtechnogauntlet.remote.CommandProcessor;
import com.mikehelland.omgtechnogauntlet.remote.JamListenersHelper;
import com.mikehelland.omgtechnogauntlet.remote.OnReceiveSavedJamsListener;
import com.mikehelland.omgtechnogauntlet.remote.RemoteControlBluetoothHelper;

import java.util.ArrayList;


public class ConnectToHostFragment extends OMGFragment {

    private View mView;
    private TextView mStatusText;
    private ImageView mImageView;
    private BluetoothManager mBT;
    private BluetoothDevice bluetoothDevice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.bluetooth_remote,
                container, false);

        final Main activity = ((Main)getActivity());

        mBT = activity.bluetoothManager;

        mStatusText = (TextView)mView.findViewById(R.id.bt_status);
        mImageView = (ImageView)mView.findViewById(R.id.remote_logo);

        mView.findViewById(R.id.exit_remote).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Activity a = getActivity();
                if (a != null) {
                    a.finish();
                }
                return true;
            }
        });

        if (activity.isRemote() && !activity.bluetoothJamStatus.getConnectionToHost().isDisconnected()) {
            mStatusText.setText(R.string.connected);
            mImageView.setImageResource(R.drawable.device_blue);
            ((TextView)mView.findViewById(R.id.bt_host)).setText(
                    activity.bluetoothJamStatus.getConnectionToHost().getDevice().getName());

            setupSavedJames(activity.bluetoothJamStatus.getConnectionToHost());
        }
        else {
            mBT.whenReady(new BluetoothReadyCallback() {
                @Override
                public void onReady() {
                    setup();
                }
            });
        }

        return mView;
    }

    private void setup() {
        final Activity activity = getActivity();
        mStatusText.setText(R.string.looking_for_device);

        Animation turnin = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
        turnin.setDuration(4000);
        turnin.setRepeatCount(100);
        mImageView.startAnimation(turnin);


        final String address = PreferenceManager.
                getDefaultSharedPreferences(activity).getString("default_host", "");
        final String name = PreferenceManager.
                getDefaultSharedPreferences(activity).getString("default_host_name", "");

        if (address.length() > 0) {
            connectToHost(address, name);
        }

        ((TextView)mView.findViewById(R.id.bt_host)).setText(name);

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost(address, name);
            }
        });
        mImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                PreferenceManager.
                        getDefaultSharedPreferences(activity).edit().putString("default_host", "").apply();
                return true;
            }
        });
    }

    private void connectToHost(String address, String name) {

        BluetoothDevice device = null;
        for (BluetoothDevice bd : mBT.getPairedDevices()) {
            if (bd.getAddress().equals(address)) {
                device = bd;
            }
        }

        if (device == null) {
            mStatusText.setText(String.format(getString(R.string.device_not_paired), name));
            return;
        }

        mBT.connectTo(device, new BluetoothConnectCallback() {
            @Override
            public void newStatus(final String status) {
                if (getActivity() == null)
                    return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //todo wouldn't this be on disconnected?
                        /*if (status.equals(BluetoothManager.STATUS_IO_CONNECTED_THREAD)) {
                            mImageView.setImageResource(R.drawable.device);
                            mStatusText.setText(R.string.accepting_connections);
                            FragmentManager fm = getFragmentManager();
                            if (fm != null) {
                                int stackCount = fm.getBackStackEntryCount();
                                for (int i = 0; i < stackCount; i++) {
                                    popBackStack();
                                }
                            }
                        }
                        else {
                            mStatusText.setText(status);
                        }*/
                        mStatusText.setText(status);

                    }
                });
            }

            @Override
            public void onConnected(final BluetoothConnection connection) {

                final Main activity = (Main)getActivity(); if (activity == null) return;

                activity.bluetoothJamStatus.setupRemote(connection);

                //process any incoming messages from this connection
                final CommandProcessor cp = new CommandProcessor(activity.onGetSoundSetsListener,
                        activity.jamsProvider);
                cp.setSync(true); 
                cp.setup(activity.bluetoothJamStatus, connection, jam, null);
                connection.setDataCallback(cp);

                //send any changes to this jam to the host
                JamListenersHelper.setJamListenersForRemote(jam, connection);

                jam.addOnJamChangeListener(new OnJamChangeListener() {
                    @Override public void onChordProgressionChange(int[] chords, String source) { }

                    @Override
                    public void onNewJam(Jam jam, String source) {
                        jam.removeOnJamChangeListener(this);
                        OMGFragment f = new MainFragment();
                        animateFragment(f, 0);
                    }

                    @Override public void onNewPart(JamPart part, String source) { }
                    @Override public void onPlay(String source) { }
                    @Override public void onStop(String source) { }
                    @Override public void onNewLoop(String source) { }
                    @Override public void onPartTrackValueChange(JamPart jamPart, int track, int subbeat, boolean value, String source) { }
                    @Override public void onPartStartLiveNotes(JamPart jamPart, Note note, int autoBeat, String source) { }
                    @Override public void onPartUpdateLiveNotes(JamPart jamPart, Note[] notes, int autoBeat, String source) { }
                    @Override public void onPartRemoveLiveNotes(JamPart jamPart, Note note, Note[] notes, String source) { }
                    @Override public void onPartEndLiveNotes(JamPart jamPart, String source) { }
                    @Override public void onPartClear(JamPart jamPart, String source) { }
                });

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStatusText.setText(R.string.getting_jam_info);
                        mImageView.setImageResource(R.drawable.device_blue);
                        //RemoteControlBluetoothHelper.requestJam(connection);
                        connection.sendNameValuePair(CommandProcessor.REMOTE_CONTROL, "TRUE");


                        //todo get the jam as json from the host
                        //load it, and show the main fragment

                    }
                });
            }

            @Override
            public void onDisconnected(BluetoothConnection connection) {
                //todo remove the listeners, at least
            }
        });
    }

    private void showRemoteControlFragment() {
        //todo?
        //RemoteControlFragment f = new RemoteControlFragment();
        //animateFragment(f, 0);
    }


    private void showRemoteControlFragmentAfterDelay(final int delay) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                showRemoteControlFragment();
            }
        }).start();
    }

    public void setDevice(BluetoothDevice device) {
        bluetoothDevice = device;
    }

    private void setupSavedJames(BluetoothConnection connection) {

        mView.findViewById(R.id.back_to_jam_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OMGFragment f = new MainFragment();
                animateFragment(f, 0);
            }
        });

        RemoteControlBluetoothHelper.requestSavedJams(connection);

        ((CommandProcessor)connection.getDataCallback()).setOnReceiveSavedJamsListener(new OnReceiveSavedJamsListener() {
            @Override
            public void onReceiveSavedJams(ArrayList<JamHeader> jams) {
                setupSavedJamsList(jams);
            }
        });
    }

    private void setupSavedJamsList(final ArrayList<JamHeader> jams) {

        Activity activity = getActivity(); if (activity == null) return;

        final ListView list = (ListView)mView.findViewById(R.id.saved_list);
        final SavedJamsAdapter adapter = new SavedJamsAdapter(activity, R.layout.chordoption,
                jams);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                list.setAdapter(adapter);
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Main activity = (Main)getActivity();
                if (activity == null || !activity.isRemote()) {
                    return;
                }

                activity.bluetoothJamStatus.getConnectionToHost().sendNameValuePair(CommandProcessor.LOAD_JAM, "" + jams.get(i).id);
                popBackStack();
            }
        });

    }
}