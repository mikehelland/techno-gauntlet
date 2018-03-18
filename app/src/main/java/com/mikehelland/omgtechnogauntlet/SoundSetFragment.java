package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class SoundSetFragment extends OMGFragment {

    private View mView;
    private Channel mChannel;
    private ChoiceCallback mCallback = null;

    private boolean mDownloadedFromOMG = false;

    private Cursor mCursor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.choose_soundset,
                container, false);

        TabHost tabHost = (TabHost)mView.findViewById(R.id.soundset_tab_host);
        tabHost.setup();

        TabHost.TabSpec spec1 = tabHost.newTabSpec("tag1");
        spec1.setContent(R.id.tab1);
        spec1.setIndicator("Installed Sounds");
        tabHost.addTab(spec1);

        TabHost.TabSpec spec2 = tabHost.newTabSpec("tag2");
        spec2.setContent(R.id.tab2);
        spec2.setIndicator("Browse Online Gallery");
        tabHost.addTab(spec2);

        TabHost.TabSpec spec3 = tabHost.newTabSpec("tag3");
        spec3.setContent(R.id.tab3);
        spec3.setIndicator("Open Source SoundFonts");
        tabHost.addTab(spec3);

        TextView textView = (TextView)tabHost.getTabWidget().getChildAt(0)
                .findViewById(android.R.id.title);
        textView.setGravity(Gravity.CENTER);
        textView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        textView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;

        textView = (TextView)tabHost.getTabWidget().getChildAt(1)
                .findViewById(android.R.id.title);
        textView.setGravity(Gravity.CENTER);
        textView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        textView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;

        textView = (TextView)tabHost.getTabWidget().getChildAt(2)
                .findViewById(android.R.id.title);
        textView.setGravity(Gravity.CENTER);
        textView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        textView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                if (s.equals("tag2") && !mDownloadedFromOMG) {
                    setupOMGTab();
                    mDownloadedFromOMG = true;

                }
            }
        });

        getActivityMembers();
        if (mChannel != null)
            setup();

        return mView;
    }

    public void setJam(Jam jam, Channel channel) {
        mJam = jam;
        mChannel = channel;

        if (mView != null)
            setup();
    }

    private void setup() {

        final Context context = getActivity();
        if (context == null) {
            return;
        }

        setupOptionsButton();

        SoundSetDataOpenHelper openHelper = ((Main)context).getDatabase().getSoundSetData();
        mCursor = openHelper.getCursor();

        final SoundSetAdapter curA = new SoundSetAdapter(context,
                R.layout.saved_row,
                mCursor, new String[]{"name"},
                new int[]{R.id.saved_data_tags});

        ListView soundsetList = (ListView)mView.findViewById(R.id.installed_soundset_list);
        soundsetList.setAdapter(curA);

        //db.close();

        soundsetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                mCursor.moveToPosition(i);

                mChannel.prepareSoundSet(new SoundSet(mCursor));
                if (mCallback != null)
                    mCallback.onChoice(mChannel.getSoundSet());

                Activity activity = getActivity();
                if (activity != null)
                    activity.getFragmentManager().popBackStack();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mPool.loadSounds();
                        mChannel.loadSoundSetIds();
                    }
                }).start();
            }
        });

        soundsetList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                askToRemove(i);
                return true;
            }
        });

        setupSoundfontTab();
    }

    public void setupOMGTab() {

        final Context context = getActivity();
        if (context == null) {
            return;
        }
        new SoundSetListDownloader(context, new SoundSetListDownloader.DownloaderCallback() {
            @Override
            void run(final MatrixCursor cursor) {

                final SoundSetAdapter curA = new SoundSetAdapter(context,
                        R.layout.saved_row,
                        cursor, new String[]{"name"},
                        new int[]{R.id.saved_data_tags});

                ListView soundsetList = (ListView)mView.findViewById(R.id.online_soundset_list);
                soundsetList.setAdapter(curA);

                soundsetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        cursor.moveToPosition(i);
                        String url = cursor.getString(cursor.getColumnIndex("url"));

                        new SoundSetDownloader(context, url, new SoundSetDownloader.DownloaderCallback() {

                            public void run(SoundSet soundSet) {
                                onSoundSetFilesDownloaded(soundSet);
                            }
                        }).download();
                    }
                });

            }
        });
    }

    static abstract class ChoiceCallback {
        abstract void onChoice(SoundSet soundSet);
    }

    void setCallback(ChoiceCallback callback) {
        mCallback = callback;
    }

    private void askToRemove(final int i) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        delete(i);
                        setup();
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
            builder.setMessage("Remove this SoundSet?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }

    private void delete(int i) {
        Activity activity = getActivity();
        if (activity == null)
            return;

        mCursor.moveToPosition(i);
        long id = mCursor.getLong(mCursor.getColumnIndex("_id"));
        ((Main)activity).getDatabase().getSoundSetData().delete(id);
    }

    private void setupSoundfontTab() {
        Activity activity = getActivity();
        if (activity == null)
            return;

        final String[] soundfonts = activity.getResources().getStringArray(R.array.soundfonts);

        ListView list = (ListView)mView.findViewById(R.id.soundfont_list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                loadSoundfont(soundfonts[i]);
            }
        });
    }

    private void loadSoundfont(String soundfontName) {
        Activity activity = getActivity();
        if (activity == null)
            return;

        final String[] soundfontURLs = activity.getResources().getStringArray(R.array.soundfont_urls);

        Spinner librarySpinner = (Spinner)mView.findViewById(R.id.soundfont_library_spinner);
        String url = soundfontURLs[librarySpinner.getSelectedItemPosition()];

        url = url + soundfontName + "-mp3/";

        String soundfontJSON = "{\"name\": \"" + soundfontName.replace("_", " ") +
                " (" + librarySpinner.getSelectedItem().toString() + ")\", \"prefix\": \"" + url +"\"," +
                activity.getResources().getString(R.string.soundfont_json);


        Toast.makeText(activity, url, Toast.LENGTH_SHORT).show();
        new SoundSetDownloader(activity, url, new SoundSetDownloader.DownloaderCallback() {
            @Override
            void run(SoundSet soundSet) {
                onSoundSetFilesDownloaded(soundSet);
            }
        }).installSoundSet(soundfontJSON);

    }

    private void onSoundSetFilesDownloaded(SoundSet soundSet) {
        if (soundSet == null){
            Log.e("MGH", "Not a valid soundset");
            return;
        }

        mChannel.prepareSoundSet(soundSet);
        if (mCallback != null)
            mCallback.onChoice(mChannel.getSoundSet());

        Activity activity = getActivity();
        if (activity != null)
            activity.getFragmentManager().popBackStack();

        new Thread(new Runnable() {
            @Override
            public void run() {
                mPool.loadSounds();
                mChannel.loadSoundSetIds();
            }
        }).start();

    }

    private void setupOptionsButton() {
        mView.findViewById(R.id.more_channel_options_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChannelOptionsFragment f = new ChannelOptionsFragment();
                f.setJam(mJam, mChannel);
                showFragmentRight(f);
            }
        });
    }
}
