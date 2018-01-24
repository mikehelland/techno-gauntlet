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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
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
        spec3.setIndicator("Open SoundFonts");
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

                final Runnable oldPoolCallback = mPool.onAllLoadsFinishedCallback;
                mPool.onAllLoadsFinishedCallback = new Runnable() {
                    @Override
                    public void run() {

                        Activity activity = getActivity();
                        if (activity != null)
                            activity.getFragmentManager().popBackStack();

                        mPool.onAllLoadsFinishedCallback = oldPoolCallback;
                    }
                };

                mChannel.prepareSoundSet(new SoundSet(mCursor));
                if (mCallback != null)
                    mCallback.onChoice(mChannel.getSoundSet());

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

                        new SoundSetDownloader(getActivity(), url, new SoundSetDownloader.DownloaderCallback() {

                            public void run(SoundSet soundSet) {
                                onSoundSetFilesDownloaded(soundSet);
                            }
                        }).download();
                    }
                });

            }
        });
    }

    void downloadCustomUrl() {

        EditText editText = (EditText)mView.findViewById(R.id.custom_url_edittext);
        String customUrl = editText.getText().toString();

        InputMethodManager imm = (InputMethodManager)editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        new SoundSetDownloader(getActivity(), customUrl, new SoundSetDownloader.DownloaderCallback() {
            @Override
            void run(SoundSet soundSet) {

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
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Remove this SoundSet?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }

    private void delete(int i) {
        mCursor.moveToPosition(i);
        long id = mCursor.getLong(mCursor.getColumnIndex("_id"));
        ((Main)getActivity()).getDatabase().getSoundSetData().delete(id);
    }

    private void setupSoundfontTab() {

        final String[] soundfonts = getActivity().getResources().getStringArray(R.array.soundfonts);

        ListView list = (ListView)mView.findViewById(R.id.soundfont_list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                loadSoundfont(soundfonts[i]);
            }
        });
    }

    private void loadSoundfont(String soundfontName) {
        final String[] soundfontURLs = getActivity().getResources().getStringArray(R.array.soundfont_urls);

        Spinner librarySpinner = (Spinner)mView.findViewById(R.id.soundfont_library_spinner);
        String url = soundfontURLs[librarySpinner.getSelectedItemPosition()];

        url = url + soundfontName + "-mp3/";

        String soundfontJSON = "{\"name\": \"" + soundfontName + "\", \"prefix\": \"" + url +"\"," +
                getActivity().getResources().getString(R.string.soundfont_json);


        Toast.makeText(getActivity(), url, Toast.LENGTH_LONG).show();
        new SoundSetDownloader(getActivity(), url, new SoundSetDownloader.DownloaderCallback() {
            @Override
            void run(SoundSet soundSet) {
                onSoundSetFilesDownloaded(soundSet);
            }
        }).installSoundSet(soundfontJSON);

    }

    private void onSoundSetFilesDownloaded(SoundSet soundSet) {
        if (soundSet == null){
            Log.d("MGH", "Not a valid soundset");
            return;
        }

        final Runnable oldPoolCallback = mPool.onAllLoadsFinishedCallback;
        mPool.onAllLoadsFinishedCallback = new Runnable() {
            @Override
            public void run() {

                Activity activity = getActivity();
                if (activity != null)
                    activity.getFragmentManager().popBackStack();

                mPool.onAllLoadsFinishedCallback = oldPoolCallback;
            }
        };

        mChannel.prepareSoundSet(soundSet);
        if (mCallback != null)
            mCallback.onChoice(mChannel.getSoundSet());

        new Thread(new Runnable() {
            @Override
            public void run() {
                mPool.loadSounds();
                mChannel.loadSoundSetIds();
            }
        }).start();

    }
}
