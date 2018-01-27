package com.mikehelland.omgtechnogauntlet;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class SoundSetDownloader {

    private ProgressDialog mProgressDialog;
    private DownloaderCallback mCallback;

    private String mURL;

    private SoundSet mSoundSet = null;
    final private Context context;
    final private SoundSetDataOpenHelper mDataHelper;

    SoundSetDownloader(Context context, String url, DownloaderCallback callback) {
        this.context = context;
        mDataHelper = ((Main)context).getDatabase().getSoundSetData();

        mCallback = callback;
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Downloading...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        mURL = url;
    }

    void download() {
        final DownloadSoundSetJSON downloadTask = new DownloadSoundSetJSON();
        downloadTask.execute(mURL);
    }

    private class DownloadSoundSetJSON extends AsyncTask<String, Integer, String> {

        //private PowerManager.WakeLock mWakeLock;

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
                    Log.e("MGH", "Server returned HTTP " + connection.getResponseCode()
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
                e.printStackTrace();
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

            installSoundSet(result);
        }
    }



    private class DownloadSoundSetFiles extends AsyncTask<String, Integer, String> {

        private int idownload = 0;
        private int itotaldownloads = 0;
        private int isuccessfuldownloads = 0;
        private Context context;
        private PowerManager.WakeLock mWakeLock;

        private String mJSON;

        private String mDownloadPath;

        DownloadSoundSetFiles(Context context, String json, String downloadpath) {
            mJSON = json;
            mDownloadPath = downloadpath;
            this.context = context;
        }

        @Override
        protected String doInBackground(String... surl) {

            String resultList = "";
            try {
                JSONObject jsonO = new JSONObject(mJSON);
                JSONArray data = jsonO.getJSONArray("data");

                String prefix = jsonO.has("prefix") ? jsonO.getString("prefix") : "";
                String postfix = jsonO.has("postfix") ? jsonO.getString("postfix") : "";
                itotaldownloads = data.length();
                String results;

                for (int i = 0; i < data.length(); i++) {

                    publishProgress(-100);

                    try {
                        URL url = new URL(prefix +
                                data.getJSONObject(i).getString("url") + postfix);
                        results = downloadFile(url, i);
                    }
                    catch (MalformedURLException murle) {
                        results = null;
                    }
                    if (results != null) {
                        resultList += results;
                    }
                }
            }
            catch (JSONException jsonexp) {
                jsonexp.printStackTrace();
            }

            if (resultList.length() > 0)
                return resultList;

            return null;
        }

        String downloadFile(URL url, int position) {

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {

                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();

                String fileName = url.toString();
                String extension = fileName.substring(fileName.lastIndexOf("."), fileName.length());
                if (extension.lastIndexOf("?") > -1)
                    extension = extension.substring(0, extension.lastIndexOf("?"));

                Log.v("MGH", "downlading url: " + url);
                Log.v("MGH", mDownloadPath + Integer.toString(position) + extension);

                output = new FileOutputStream(mDownloadPath + Integer.toString(position)); //+ extension);

                byte data[] = new byte[4096];
                long total = 0;
                int count;

                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
                isuccessfuldownloads++;
            } catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                    return ignored.getMessage();

                }

                if (connection != null)
                    connection.disconnect();

            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false

            if (progress[0] == -100) {
                idownload++;
                mProgressDialog.setMessage("Downloading sound " +
                        Integer.toString(idownload) + " of " + Integer.toString(itotaldownloads));
                mProgressDialog.setIndeterminate(true);
            }
            else {
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setMax(100);
                mProgressDialog.setProgress(progress[0]);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();

            if (isuccessfuldownloads > 0) {
                mDataHelper.saveAsDownlaoded(mSoundSet);
            }

            if (result != null)
                Toast.makeText(context,"Download error: " + result.substring(0, Math.min(100, result.length())), Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context,"Sound Set downloaded", Toast.LENGTH_SHORT).show();

            if (isuccessfuldownloads > 0 && mSoundSet !=null && mCallback != null)
                mCallback.run(mSoundSet);
        }
    }

    static abstract class DownloaderCallback {
        abstract void run(SoundSet soundSet);
    }

    void installSoundSet(String json) {
        ContentValues soundset = processSoundSetJSON(json);

        if (soundset != null) {
            mSoundSet = mDataHelper.addSoundSet(soundset);

            String soundsetDirectoryPath = context.getFilesDir() + "/" +
                    Long.toString(mSoundSet.getID()) + "/";
            new File(soundsetDirectoryPath).mkdirs();

            DownloadSoundSetFiles downloader = new DownloadSoundSetFiles(context, json,
                    soundsetDirectoryPath);
            downloader.execute();

        }

    }

    private ContentValues processSoundSetJSON(String result) {

        ContentValues soundset = new ContentValues();
        soundset.put("url", mURL);

        // now, process the json
        if (result == null) {
            Toast.makeText(context, "Download error", Toast.LENGTH_LONG).show();
            return null;
        }
        try {
            JSONObject jsonSoundSet = new JSONObject(result);
            if (jsonSoundSet.has("name") &&
                    jsonSoundSet.has("type") &&
                    jsonSoundSet.getString("type").equals("SOUNDSET") &&
                    jsonSoundSet.has("data")) {

                soundset.put("name", jsonSoundSet.getString("name"));

            } else {
                Toast.makeText(context, "Not a valid SOUNDSET", Toast.LENGTH_LONG).show();
                return null;
            }

        } catch (JSONException jsonex) {
            Toast.makeText(context, "JSON Error " + jsonex.getMessage(), Toast.LENGTH_LONG).show();
            jsonex.printStackTrace();
            Log.e("MGH err loadsoundfont", result);
            return null;
        }

        soundset.put("data", result);
        return soundset;
   }
}