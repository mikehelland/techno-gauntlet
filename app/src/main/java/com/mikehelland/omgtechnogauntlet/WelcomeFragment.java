package com.mikehelland.omgtechnogauntlet;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.database.Cursor;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

public class WelcomeFragment extends OMGFragment {

    private View mView;
    private ProgressBar mProgressBar;
    private Cursor mCursor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivityMembers();

        mView = inflater.inflate(R.layout.welcome,
                container, false);

        //mProgressBar = (ProgressBar)mView.findViewById(R.id.loading_progress);
        mProgressBar = (ProgressBar)getActivity().findViewById(R.id.loading_progress);
        populateSavedListView();

        if (!mPool.isInitialized()) {

            setupSoundPool();

            animateIcons();

        }



        mView.findViewById(R.id.return_to_omg_bananas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainFragment mainFragment = new MainFragment();
                showFragment(mainFragment);
            }
        });

        mView.findViewById(R.id.exit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        mView.findViewById(R.id.blank_jam_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadJam(getActivity().getResources().getString(R.string.blank_jam));
            }
        });


        return mView;
    }

    private void populateSavedListView() {
        Context context = getActivity();

        ListView listView = (ListView)mView.findViewById(R.id.saved_list);
        mCursor = new SavedDataOpenHelper(context).getSavedCursor();

        SimpleCursorAdapter curA = new SavedDataAdapter(context,
                R.layout.saved_row,
                mCursor, new String[]{"tags", "time"},
                new int[]{R.id.saved_data_tags, R.id.saved_data_date});

        listView.setAdapter(curA);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                loadSavedJam(i);
            }
        });

    }

    private void loadSavedJam(int position) {
        mCursor.moveToPosition(position);
        String json = mCursor.getString(mCursor.getColumnIndex("data"));
        loadJam(json);
    }

    private void loadJam(String json) {

        final Jam jam = new Jam(getActivity(), mPool, mJamCallback);
        jam.load(json, false);

        final Runnable callback = new Runnable() {
            @Override
            public void run() {
                jam.loadSoundSets();

                mJam.finish();
                mJam = jam;

                ((Main)getActivity()).mJam = jam;

                //pretty lousy spot for this
                BluetoothFactory btf = ((Main)getActivity()).mBtf;
                CommandProcessor cp;
                for (BluetoothConnection connection : btf.getConnections()) {
                    cp = new CommandProcessor();
                    cp.setup(connection, jam, null);
                    connection.setDataCallback(cp);
                }

                if (!mJam.isPlaying())
                    mJam.kickIt();


            }
        };

        if (mPool.soundsToLoad == 0) {
            callback.run();
            showFragment(new MainFragment());
            return;
        }

        mProgressBar.setMax(mPool.soundsToLoad);

        new Thread(new Runnable() {
            @Override
            public void run() {
                callback.run();
            }
        }).start();
    }

    public void showFragment(Fragment f) {


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

    private void setupSoundPool() {

        mPool.onAllLoadsFinishedCallback = new Runnable() {
            @Override
            public void run() {
                MainFragment mainFragment = new MainFragment();
                showFragment(mainFragment);
            }
        };

        mPool.allowLoading();

        mJam.load(getActivity().getResources().getString(R.string.default_jam), false);

        //mProgressBar.setMax(mPool.soundsToLoad);
        //mProgressBar.setProgress(0);
        mPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                if (mProgressBar.getVisibility() == View.INVISIBLE) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setMax(mPool.soundsToLoad);
                }

                mProgressBar.incrementProgressBy(1);

                mPool.soundsToLoad--;
                if (mPool.soundsToLoad <= 0) {

                    mPool.setLoaded(true);
                    mProgressBar.setProgress(0);
                    mProgressBar.setVisibility(View.INVISIBLE);

                    if (!mPool.isCanceled() && mPool.onAllLoadsFinishedCallback != null)
                        mPool.onAllLoadsFinishedCallback.run();

                }
            }
        });

        mPool.setInitialized(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mJam.loadSoundSets();
            }
        }).start();

    }

    void animateIcons() {

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (mPool.isCanceled()) return;

                View view = mView.findViewById(R.id.img_press_banana);
                Animation turnin = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
                view.startAnimation(turnin);


            }
        }, 250);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (mPool.isCanceled()) return;

                View view = mView.findViewById(R.id.img_press_monkey);
                Animation turnin = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
                view.startAnimation(turnin);

            }
        }, 500);

    }
}
