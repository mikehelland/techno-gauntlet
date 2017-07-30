package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.TabHost;
import android.widget.TextView;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class SoundSetFragment extends OMGFragment {

    private Jam mJam;
    private View mView;
    private Channel mChannel;

    private boolean mDownloadedFromOMG = false;

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
        spec3.setIndicator("Custom ...");
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

        if (mJam != null)
            setup();

        return mView;
    }

    public void setJam(Jam jam, Channel channel) {
        mJam = jam;
        mChannel = channel;

        if (mView != null)
            setup();
    }

    public void setup() {

        final Context context = getActivity();

        final SoundSetDataOpenHelper openHelper = new SoundSetDataOpenHelper(context);
        final SQLiteDatabase db = openHelper.getWritableDatabase();
        final Cursor cursor;
        //if (mChannel.getSoundSet().isChromatic()) {
            cursor = openHelper.getSavedCursor(db);
        //}
        //else {
        //    cursor = openHelper.getSavedCursor(db);
       // }

        final SoundSetAdapter curA = new SoundSetAdapter(context,
                R.layout.saved_row,
                cursor, new String[]{"name"},
                new int[]{R.id.saved_data_tags});


        ListView soundsetList = (ListView)mView.findViewById(R.id.installed_soundset_list);
        soundsetList.setAdapter(curA);

        //db.close();


        soundsetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                cursor.moveToPosition(i);

                mChannel.prepareSoundSet(new SoundSet(cursor));
                if (mChannel.loadSoundSet()) {
                    Log.d("MGH", "sound set loaded");
                }
                else {
                    Log.d("MGH", "sound set NOT loaded");
                }

                //mainFragment.updateUI();
                getActivity().getFragmentManager().popBackStack();
            }


        });

        mView.findViewById(R.id.custom_url_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadCustomUrl();
            }
        });

    }

    public void setupOMGTab() {


        new SoundSetListDownloader(getActivity(), new SoundSetListDownloader.DownloaderCallback() {
            @Override
            void run(final MatrixCursor cursor) {

                final Context context = getActivity();
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

                                if (soundSet == null){
                                    Log.d("MGH", "Not a valid soundset");
                                    return;
                                }

                                mChannel.prepareSoundSet(soundSet);
                                mChannel.loadSoundSet();
                                Log.d("MGH", "sound set loaded");

                                getActivity().getFragmentManager().popBackStack();
                            }
                        });
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
}
