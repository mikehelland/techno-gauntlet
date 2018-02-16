package com.mikehelland.omgtechnogauntlet;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class LoadJamFromURLFragment extends OMGFragment {

    private View mView;
    private Channel mChannel;
    private ChoiceCallback mCallback = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.load_jam_from_url,
                container, false);


        getActivityMembers();
        setup();

        return mView;
    }

    private void setup() {

        mView.findViewById(R.id.download_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadCustomUrl();
            }
        });
    }
    void downloadCustomUrl() {

        EditText editText = (EditText)mView.findViewById(R.id.custom_url_edittext);
        String customUrl = editText.getText().toString();

        InputMethodManager imm = (InputMethodManager)editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        if (customUrl.length() == 0) {
            return;
        }

        new FileDownloader(getActivity(), customUrl, new FileDownloader.DownloaderCallback() {
            @Override
            void run(String result) {
                if (result == null) {
                    Log.e("MGH fileDownlaoder", "error downloading");
                    return;
                }

                Main activity = (Main)getActivity();

                if (activity != null) {
                    activity.loadJam(result);
                    activity.getFragmentManager().popBackStack();
                }
            }
        });

    }
    static abstract class ChoiceCallback {
        abstract void onChoice(String result);
    }

    void setCallback(ChoiceCallback callback) {
        mCallback = callback;
    }

}
