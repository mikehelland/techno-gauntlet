package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
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
    private EditText editText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.choosesoundset,
                container, false);

        editText = (EditText)mView.findViewById(R.id.custom_url_edittext);
        editText.setOnEditorActionListener(new DoneOnEditorActionListener());

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
        if (mChannel.getType().equals("DRUMBEAT")) {
            cursor = openHelper.getSavedCursor(db);
        }
        else {
            cursor = openHelper.getSavedCursor(db);
        }

        final SoundSetAdapter curA = new SoundSetAdapter(context,
                R.layout.saved_row,
                cursor, new String[]{"name"},
                new int[]{R.id.saved_data_tags});


        ListView soundsetList = (ListView)mView.findViewById(R.id.soundset_list);
        soundsetList.setAdapter(curA);

        //db.close();


        soundsetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                cursor.moveToPosition(i);

                String json = cursor.getString(cursor.getColumnIndex("data"));

                if (mChannel.loadSoundSet(json, l)) {
                    Log.d("MGH", "sound set loaded");
                }
                else {
                    Log.d("MGH", "sound set NOT loaded");
                }

                //mainFragment.updateUI();
                //getActivity().getSupportFragmentManager().popBackStack();
            }


        });

        mView.findViewById(R.id.custom_url_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadCustomUrl();
            }
        });

    }

    void downloadCustomUrl() {

        String customUrl = editText.getText().toString();

        InputMethodManager imm = (InputMethodManager)editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        new SoundSetDownloader(getActivity(), customUrl, new Runnable() {
            public void run() {
                setup();
            }
        });
    }


    private class DoneOnEditorActionListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                downloadCustomUrl();
                return true;
            }
            return false;
        }
    }

}
