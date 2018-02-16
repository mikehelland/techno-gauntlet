package com.mikehelland.omgtechnogauntlet;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class CustomSoundSetFragment extends OMGFragment {

    private View mView;
    private Channel mChannel;
    private ChoiceCallback mCallback = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.choose_customsoundset,
                container, false);


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

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                mPool.loadSounds();
                mChannel.loadSoundSetIds();
            }
        }).start();*/

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
                if (result.startsWith("[")) {

                    Activity activity = getActivity();
                    if (activity != null)
                        activity.getFragmentManager().popBackStack();

                    //this should be a list of soundset urls
                    processSoundSetList(result);
                }
                else if (result.startsWith("{")) {
                    //this should be a soundset
                    new SoundSetDownloader(getActivity(), "", new SoundSetDownloader.DownloaderCallback() {
                        @Override
                        void run(SoundSet soundSet) {
                            onSoundSetFilesDownloaded(soundSet);
                        }
                    }).installSoundSet(result);
                }
            }
        });

    }

    private void processSoundSetList(String json) {
        String[]  urls = new String[0];
        try {
            JSONArray list = new JSONArray(json);
            urls = new String[list.length()];
            for (int i = 0; i < list.length(); i++) {
                urls[i] = list.getString(i);
            }
        }
        catch (JSONException je) {

        }

        if (urls != null && urls.length > 0) {
            downloadSoundSetFromList(getActivity(), urls, 0);
        }
    }

    private void downloadSoundSetFromList(final Context context, final String[] list, final int index) {
        if (context == null) {
            return;
        }

        if (index < list.length) {
            new SoundSetDownloader(context, list[index], new SoundSetDownloader.DownloaderCallback() {
                @Override
                void run(SoundSet soundSet) {
                    downloadSoundSetFromList(context, list, index + 1);
                }
            }).download();
        }
    }

    static abstract class ChoiceCallback {
        abstract void onChoice(SoundSet soundSet);
    }

    void setCallback(ChoiceCallback callback) {
        mCallback = callback;
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
}
