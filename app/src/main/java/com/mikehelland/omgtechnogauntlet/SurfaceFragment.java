package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

public class SurfaceFragment extends OMGFragment {

    private Jam mJam;
    private View mView;
    private Channel mChannel;
    private EditText editText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.choose_surface,
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

        getActivityMembers();

        final Context context = getActivity();
        if (context == null) {
            return;
        }

        final SufacesDataHelper openHelper = new SufacesDataHelper(context);
        final SQLiteDatabase db = openHelper.getWritableDatabase();
        final Cursor cursor;
        if (mChannel.getSoundSet().isChromatic()) {
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

                String surface = cursor.getString(cursor.getColumnIndex("data"));

                //todo yeah this is way whacky, needs an surface object
                mChannel.setSurface(surface);

                mBtf.sendCommandToDevices(CommandProcessor.getChannelsInfoCommand(mJam), null);

                getActivity().getFragmentManager().popBackStack();

                if (surface.equals("PRESET_SEQUENCER")) {
                    DrumFragment f = new DrumFragment();
                    f.setJam(mJam, mChannel);
                    showFragmentRight(f);
                }
                else {

                    GuitarFragment f = new GuitarFragment();
                    f.setJam(mJam, mChannel);
                    showFragmentRight(f);
                }
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

        //todo download surfaces
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
