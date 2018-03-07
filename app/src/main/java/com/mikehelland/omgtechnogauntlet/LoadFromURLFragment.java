package com.mikehelland.omgtechnogauntlet;

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
import org.json.JSONObject;

/**
 * User: m
 * Date: 5/6/14
 * Time: 7:11 PM
 */
public class LoadFromURLFragment extends OMGFragment {

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.load_from_url,
                container, false);

        getActivityMembers();
        setup();

        return mView;
    }

    private void setup() {

        mView.findViewById(R.id.download_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText)mView.findViewById(R.id.custom_url_edittext);
                String customUrl = editText.getText().toString();

                InputMethodManager imm = (InputMethodManager)editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }

                if (customUrl.startsWith("omg/")) {
                    customUrl = customUrl.replace("omg/", "http://openmusic.gallery/data/");
                }

                downloadCustomUrl(customUrl);
            }
        });
    }

    void downloadCustomUrl(String customUrl) {

        if (customUrl.length() == 0) {
            return;
        }

        new FileDownloader(getActivity(), customUrl, new FileDownloader.DownloaderCallback() {
            @Override
            void run(String result) {
                if (result == null || result.length() == 0) {
                    Log.e("MGH fileDownlaoder", "error downloading");
                    return;
                }

                String type = getTypeFromJSON(result);
                if (type == null || type.length() == 0) {
                    Log.e("MGH fileDownlaoder", "unknown file type");
                    return;
                }

                handleDownloadResult(result, type);
            }
        });

    }

    private void handleDownloadResult(String result, String type) {

        Main activity = (Main)getActivity();
        if (activity == null) {
            return;
        }

        switch (type) {
            case "ARRAY":
                //this should be a list of soundset urls
                processSoundSetList(activity, result);
                break;
            case "SOUNDSET":
                new SoundSetDownloader(activity, "", null).installSoundSet(result);
                break;
            case "SECTION":
                Jam jam = activity.loadJam(result);
                if (jam != null) {
                    activity.getDatabase().getSavedData().insert(0, jam.getTags(), result);
                }
                break;
        }

        activity.getFragmentManager().popBackStack();
    }

    private String getTypeFromJSON(String json) {
        if (json.startsWith("[")) {
            return "ARRAY";
        }
        String type = "";
        try {
            JSONObject jsonO = new JSONObject(json);
            if (jsonO.has("type")) {
                type = jsonO.getString("type");
            }
        }
        catch (JSONException e) {
            Log.e("MGH FileDownload jsone", json);
        }
        return type;
    }

    private void processSoundSetList(Context context, String json) {
        String[]  urls = new String[0];
        try {
            JSONArray list = new JSONArray(json);
            urls = new String[list.length()];
            for (int i = 0; i < list.length(); i++) {
                urls[i] = list.getString(i);
            }
        }
        catch (JSONException je) {
            Log.e("MGH FileDownload jsone", json);
        }

        if (urls.length > 0) {
            downloadSoundSetFromList(context, urls, 0);
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

}
