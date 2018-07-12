package com.mikehelland.omgtechnogauntlet;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnectCallback;
import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothConnection;
import com.mikehelland.omgtechnogauntlet.bluetooth.BluetoothManager;
import com.mikehelland.omgtechnogauntlet.jam.Jam;
import com.mikehelland.omgtechnogauntlet.jam.JamHeader;
import com.mikehelland.omgtechnogauntlet.jam.OnGetSoundSetListener;
import com.mikehelland.omgtechnogauntlet.jam.OnSoundLoadedListener;
import com.mikehelland.omgtechnogauntlet.jam.OnSubbeatListener;
import com.mikehelland.omgtechnogauntlet.jam.SoundManager;
import com.mikehelland.omgtechnogauntlet.jam.SoundSet;
import com.mikehelland.omgtechnogauntlet.remote.BluetoothJamStatus;
import com.mikehelland.omgtechnogauntlet.remote.CommandProcessor;
import com.mikehelland.omgtechnogauntlet.remote.JamsProvider;
import com.mikehelland.omgtechnogauntlet.remote.OnGetSoundSetsListener;

import java.util.ArrayList;

public class Main extends FragmentActivity {

    Jam jam;

    BluetoothManager bluetoothManager;
    DatabaseContainer mDatabase;

    private WelcomeFragment mWelcomeFragment;

    private BeatView mBeatView;

    private ImageLoader mImages;

    public OnGetSoundSetsListener onGetSoundSetsListener;
    public JamsProvider jamsProvider;

    BluetoothJamStatus bluetoothJamStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN|
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main);

        mBeatView = (BeatView)findViewById(R.id.main_beatview);
        bluetoothManager = new BluetoothManager(this);

        if (hasDefaultHost()) {
            connectToDefaultHost();
            return;
        }

        mDatabase = new DatabaseContainer(Main.this);

        onGetSoundSetsListener = new OnGetSoundSetsListener() {
            @Override
            public ArrayList<SoundSet> getSoundSets() {
                return mDatabase.getSoundSetData().getList();
            }

            @Override
            public SoundSet getSoundSet(long id) {
                return mDatabase.getSoundSetData().getSoundSetById(id);
            }
        };
        jamsProvider = new JamsProvider() {
            @Override
            public ArrayList<JamHeader> getJams() {
                return mDatabase.getSavedData().getList();
            }

            @Override
            public String getJamJson(long id) {
                return mDatabase.getSavedData().getJamJson(id);
            }
        };

        OnGetSoundSetListener getSoundSetFromDatabase = new OnGetSoundSetListener() {
            @Override
            public SoundSet onGetSoundSet(String url) {
                //todo what if it's not in the database? go online? gonna need a callback
                //although, it should only really affect external jam's loaded by URL (so download them then?)
                return mDatabase.getSoundSetData().getSoundSetByURL(url);
            }
        };

        OnSoundLoadedListener updateBeatViewWithLoadProgress = new OnSoundLoadedListener() {
            @Override
            public void onSoundLoaded(int howManyLoaded, int howManyTotal) {
                mBeatView.setLoadingStatus(howManyLoaded, howManyTotal);

                if (howManyLoaded >= howManyTotal) {
                    jam.play();
                    FragmentManager fm = getSupportFragmentManager();
                    if (fm != null && fm.getBackStackEntryCount() == 0) {
                        mWelcomeFragment.animateFragment(new MainFragment(), 1);
                    }
                }
            }
        };

        SoundManager soundManager = new SoundManager(this, updateBeatViewWithLoadProgress);
        jam = new Jam(soundManager, getSoundSetFromDatabase);
        bluetoothJamStatus = new BluetoothJamStatus(jam, bluetoothManager);

        //final int defaultJam = R.string.blank_jam;
        final int defaultJam =  BuildConfig.FLAVOR.equals("demo") ? R.string.demo_jam : R.string.default_jam;

        //todo does beatView even have to know about Jam?
        mBeatView.setJam(jam);

        jam.addOnSubbeatListener(new OnSubbeatListener() {
            @Override
            public void onSubbeat(int subbeat) {
                mBeatView.postInvalidate();
            }
        });

        mBeatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (jam.isPlaying()) {
                    jam.stop();
                }
                else {
                    jam.play();
                }
                //mBeatView.postInvalidate();
            }
        });

        mImages = new ImageLoader(this);

        setupBluetooth();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mWelcomeFragment == null) {
                    mWelcomeFragment = new WelcomeFragment();
                    mWelcomeFragment.setJam(jam);

                    jam.loadFromJSON(getResources().getString(defaultJam));

                }
                try {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.add(R.id.main_layout, mWelcomeFragment);
                    ft.commit();
                } catch (Exception ignore) { }
            }
        }).start();

    }



    @Override
    public void onPause() {
        super.onPause();
        //todo relocate
        // if (!mPool.isLoaded())
        //    mPool.cancelLoading();
        if (jam.isPlaying() && !isRemote()) {
            jam.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        jam.finish();
        //todo mBtf.cleanUp();
        bluetoothManager.cleanUp();
        mDatabase.close();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            Toast.makeText(this, "Can't back out now! \nHit the 'EXIT' button.", Toast.LENGTH_SHORT).show();

        } else {
            super.onBackPressed();
        }

    }

    private void setupBluetooth() {
        if (bluetoothManager.isBlueToothOn()) {
            bluetoothManager.startAccepting(makeConnectCallback());

        }
    }

    BluetoothConnectCallback makeConnectCallback() {
        return new BluetoothConnectCallback() {
            @Override
            public void newStatus(final String status) {}
            @Override
            public void onConnected(BluetoothConnection connection) {
                final CommandProcessor cp = new CommandProcessor(Main.this.onGetSoundSetsListener,
                        Main.this.jamsProvider);
                cp.setup(bluetoothJamStatus, connection, jam, null);
                connection.setDataCallback(cp);
            }

            public void onDisconnected(final BluetoothConnection connection) {}
        };
    }

    DatabaseContainer getDatabase() {return mDatabase;}
    ImageLoader getImages() {return mImages;}

    private boolean hasDefaultHost() {
        final String address = PreferenceManager.
                getDefaultSharedPreferences(this).getString("default_host", "");

        return address.length() > 0;
    }

    private void connectToDefaultHost() {

        remoteSetup();

        OMGFragment f = new ConnectToHostFragment();
        f.jam = jam;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.main_layout, f);
        ft.commit();
    }

    private void remoteSetup() {
        mDatabase = new DatabaseContainer(Main.this);

        jam = new Jam(null, null);
        bluetoothJamStatus = new BluetoothJamStatus(jam, bluetoothManager);

        jam.loadFromJSON(getResources().getString(R.string.blank_jam));

        //todo does beatView even have to know about Jam?
        mBeatView.setJam(jam);
        mBeatView.postInvalidate();

        jam.addOnSubbeatListener(new OnSubbeatListener() {
            @Override
            public void onSubbeat(int subbeat) {
                mBeatView.postInvalidate();
            }
        });

        mBeatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (jam.isPlaying()) {
                    jam.stop();
                }
                else {
                    jam.play();
                }
                //mBeatView.postInvalidate();
            }
        });

        mImages = new ImageLoader(this);

    }

    boolean isRemote() {
        return bluetoothJamStatus != null && bluetoothJamStatus.isRemote();
    }
}
