package com.mikehelland.omgtechnogauntlet;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by m on 7/7/17.
 */

public class SoundSetListDownloader {

    private ProgressDialog mProgressDialog;
    private DownloaderCallback mCallback;

    private String mOMGDataURL = "http://openmusic.gallery/data/";

    public SoundSetListDownloader(Context context, DownloaderCallback callback) {

        mCallback = callback;
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Downloading...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        final DownloadSoundSetListJSON downloadTask = new DownloadSoundSetListJSON(context);
        downloadTask.execute(mOMGDataURL + "?type=SOUNDSET");
    }


    private class DownloadSoundSetListJSON extends AsyncTask<String, Integer, String> {

        private Context context;
        //private PowerManager.WakeLock mWakeLock;

        public DownloadSoundSetListJSON(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... surl) {

            String results;

            try {
                URL url = new URL(surl[0]);
                results = downloadFile(url);
            }
            catch (MalformedURLException murle) {
                results = null;
            }
            if (results != null) {
                return results;
            }

            return null;
        }


        String downloadFile(URL url) {

            StringBuilder json = new StringBuilder();
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                //Thread.currentThread().sleep(2000);

                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d("MGH", "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage());
                    return null;
                }

                input = connection.getInputStream();

                BufferedReader r = new BufferedReader(new InputStreamReader(input));

                String line;
                while ((line = r.readLine()) != null) {
                    json.append(line).append('\n');

                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                }

            } catch (Exception e) {
                Log.e("MGH", Log.getStackTraceString(e));
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();

            }

            return json.toString();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            //mWakeLock.release();
            mProgressDialog.dismiss();

            // now, process the json
            if (result == null) {
                Toast.makeText(context, "Download error", Toast.LENGTH_LONG).show();
            }
            else {
                MatrixCursor soundsets = processSoundSetListJSON(result);
                if (soundsets != null && mCallback != null) {
                    mCallback.run(soundsets);
                }
            }
        }

        protected MatrixCursor processSoundSetListJSON(String result) {

            String[] columns = {"_id", "name", "url", "data"};
            Object[] values = new Object[4];
            MatrixCursor soundsets = new MatrixCursor(columns);

            try {
                JSONArray jsonList = new JSONArray(result);
                JSONObject jsonSoundSet;
                for (int i = 0; i < jsonList.length(); i++) {
                    jsonSoundSet = jsonList.getJSONObject(i);

                    if (jsonSoundSet.has("name") &&
                            jsonSoundSet.has("type") &&
                            jsonSoundSet.getString("type").equals("SOUNDSET") &&
                            jsonSoundSet.has("data") &&
                            jsonSoundSet.has("id")) {

                        values[0] = i;
                        values[1] = jsonSoundSet.getString("name");
                        values[2] = mOMGDataURL + jsonSoundSet.getLong("id");
                        values[3] = jsonSoundSet.toString(); //jsonSoundSet.getString("data");

                        soundsets.addRow(values);
                    }
                }

            } catch (JSONException jsonex) {
                Toast.makeText(context, "JSON Error " + jsonex.getMessage(), Toast.LENGTH_LONG).show();
                return null;
            }

            return soundsets;
        }
    }

    static abstract class DownloaderCallback {
        abstract void run(MatrixCursor matrixCursor);
    }
}