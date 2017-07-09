package com.mikehelland.omgtechnogauntlet;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Created by m on 7/7/17.
 */

public class SoundSetDownloadFragment extends OMGFragment {

    View mView;
    ProgressDialog mProgressDialog;

    String soundsetUrl;
    boolean isSetup = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivityMembers();

        mView = inflater.inflate(R.layout.soundset_download_fragment,
                container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("Downloading...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        isSetup = true;

        if (soundsetUrl != null) {
            downloadSoundSet(soundsetUrl);
        }

        return mView;
    }


    public void downloadSoundSet(String url) {

        if (isSetup) {
            final DownloadSoundSetJSON downloadTask = new DownloadSoundSetJSON(getActivity());
            downloadTask.execute(url);
        }
        else {
            soundsetUrl = url;
        }
    }


    private class DownloadSoundSetJSON extends AsyncTask<String, Integer, String> {

        private Context context;
        //private PowerManager.WakeLock mWakeLock;

        public DownloadSoundSetJSON(Context context) {
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

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file

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
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            //PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
            //        getClass().getName());
            //mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false

            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            //mWakeLock.release();
            mProgressDialog.dismiss();

            ContentValues soundset = processSoundSetJSON(result);
            if (soundset != null) {
                SoundSetDataOpenHelper soundsetDataHelper = new SoundSetDataOpenHelper(context);
                long id = soundsetDataHelper.newSoundSet(soundset);

                String soundsetDirectoryPath = getActivity().getFilesDir() + "/" +
                        Long.toString(id) + "/";
                //String soundsetDirectoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
                new File(soundsetDirectoryPath).mkdirs();

                DownloadSoundSetFiles downloader = new DownloadSoundSetFiles(context, mJSON,
                        soundsetDirectoryPath);
                downloader.execute();

            }

        }

        private JSONObject mJSON;
        protected ContentValues processSoundSetJSON(String result) {

            ContentValues soundset = new ContentValues();

            // now, process the json
            if (result == null) {
                Toast.makeText(context, "Download error", Toast.LENGTH_LONG).show();
                return null;
            } else
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();

            try {
                JSONObject jsonSoundSet = new JSONObject(result);
                if (jsonSoundSet.has("name") &&
                        jsonSoundSet.has("type") &&
                        jsonSoundSet.getString("type").equals("SOUNDSET") &&
                        jsonSoundSet.has("data")) {

                    mJSON = jsonSoundSet;
                    soundset.put("name", jsonSoundSet.getString("name"));

                } else {
                    Toast.makeText(context, "Not a valid SOUNDSET", Toast.LENGTH_LONG).show();
                    return null;
                }

            } catch (JSONException jsonex) {
                Toast.makeText(context, "JSON Error " + jsonex.getMessage(), Toast.LENGTH_LONG).show();
                return null;
            }

            soundset.put("data", result);
            return soundset;


        }
    }



    private class DownloadSoundSetFiles extends AsyncTask<String, Integer, String> {

        private int idownload = 0;
        private int itotaldownloads = 0;
        private Context context;
        private PowerManager.WakeLock mWakeLock;

        private JSONObject mJSON;

        private String mDownloadPath;

        public DownloadSoundSetFiles(Context context, JSONObject jsonObject, String downloadpath) {
            mJSON = jsonObject;
            mDownloadPath = downloadpath;
            this.context = context;
        }

        @Override
        protected String doInBackground(String... surl) {

            try {
                JSONArray data = mJSON.getJSONArray("data");

                itotaldownloads = data.length();
                String results;
                Log.d("MGH", "do in background start");

                for (int i = 0; i < data.length(); i++) {

                    Log.d("MGH", "do download "  + Integer.toString(i));

                    publishProgress(-100);

                    try {
                        URL url = new URL(data.getJSONObject(i).getString("url"));
                        results = downloadFile(url, i);
                    }
                    catch (MalformedURLException murle) {
                        results = null;
                    }
                    if (results != null) {
                        return results;
                    }
                }


            }
            catch (JSONException jsonexp) {
                Log.d("MGH", "JSON parsing problem");
                Log.e("MGH", Log.getStackTraceString(jsonexp));
            }


            return null;
        }


        String downloadFile(URL url, int position) {

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                //Thread.currentThread().sleep(2000);

                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                Log.d("MGH", "do in background connected");

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

                Log.d("MGH", "downlading url: " + url);
                Log.d("MGH", mDownloadPath + Integer.toString(position) + extension);

                output = new FileOutputStream(mDownloadPath + Integer.toString(position)); //+ extension);

                byte data[] = new byte[4096];
                long total = 0;
                int count;

                Log.d("MGH", "starting read");


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

                Log.d("MGH", "done with loop");
            } catch (IOException e) {
                Log.e("MGH", Log.getStackTraceString(e));
                return e.getMessage();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                    Log.e("MGH", Log.getStackTraceString(ignored));
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
            if (result != null)
                Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
        }
    }


}