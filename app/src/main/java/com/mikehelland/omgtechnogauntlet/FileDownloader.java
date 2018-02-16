package com.mikehelland.omgtechnogauntlet;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class FileDownloader {

    private ProgressDialog mProgressDialog;
    private DownloaderCallback mCallback;

    FileDownloader(Context context, String url, DownloaderCallback callback) {

        mCallback = callback;
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Downloading...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        final DownloadSoundSetJSON downloadTask = new DownloadSoundSetJSON();
        downloadTask.execute(url);
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
            return results;
        }


        String downloadFile(URL url) {

            StringBuilder json = new StringBuilder();
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {

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

            if (mCallback != null) {
                mCallback.run(result);
            }
        }
    }

    static abstract class DownloaderCallback {
        abstract void run(String result);
    }
}