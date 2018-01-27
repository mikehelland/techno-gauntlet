package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class WelcomeFragment extends OMGFragment {

    private View mView;
    private Cursor mCursor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivityMembers();

        mView = inflater.inflate(R.layout.welcome,
                container, false);

        populateSavedListView();

        if (!mPool.isInitialized()) {

            loadDefaultJam();

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
                Activity activity =  getActivity();
                if (activity != null && !Main.MONKEY_TEST) {
                    activity.finish();
                }
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
        mCursor = ((Main)context).getDatabase().getSavedData().getSavedCursor();

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

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                askToRemoveSavedJam(i);
                return false;
            }
        });
    }

    private void loadSavedJam(int position) {
        mCursor.moveToPosition(position);
        String json = mCursor.getString(mCursor.getColumnIndex("data"));
        loadJam(json);
    }

    private void loadJam(String json) {

        mPool.onAllLoadsFinishedCallback = new Runnable() {
            @Override
            public void run() {
                mPool.onAllLoadsFinishedCallback = null;
                showFragment(new MainFragment());
            }
        };

        ((Main)getActivity()).loadJam(json);
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

    private void loadDefaultJam() {

        mPool.onAllLoadsFinishedCallback = new Runnable() {
            @Override
            public void run() {
                mPool.onAllLoadsFinishedCallback = null;
                showFragment(new MainFragment());
            }
        };

        mPool.allowLoading();

        mJam.load(getActivity().getResources().getString(R.string.default_jam));


        mPool.setInitialized(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mPool.loadSounds();
                mJam.loadSoundSets();
            }
        }).start();

    }

    private void askToRemoveSavedJam(final int i) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        removeSavedJam(i);
                        populateSavedListView();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        Activity activity = getActivity();
        if (activity != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage("Remove this jam?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }

    private void removeSavedJam(int i) {
        mCursor.moveToPosition(i);
        long id = mCursor.getLong(mCursor.getColumnIndex("_id"));
        SavedDataOpenHelper dataHelper = new SavedDataOpenHelper(getActivity());
        dataHelper.delete(id);
    }
}
