package com.mikehelland.omgtechnogauntlet;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class SaveToOMG {

    SaveToOMG() {
    }

    private static long doHttp(String saveUrl, String data) {
        long id = -1;

        try
        {

            URL url = new URL(saveUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");

            OutputStream request = connection.getOutputStream();

            byte[] bytes = data.getBytes("UTF-8");
            request.write(bytes);
            request.flush();

            String line;
            InputStreamReader isr = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
                sb.append("\n");
            }

            request.close();

            String response = sb.toString();

            id = getIdFromResponse(response);

            isr.close();
            reader.close();

        }
        catch(IOException e)
        {
            // Error
            Log.e("MGH SaveToOMG IO", e.getMessage());
            e.printStackTrace();
        }

        return id;
    }

    void execute(String saveUrl, String type, String data, OMGCallback callback) {
        new SendJam(callback).execute(saveUrl, type, data);
    }

    private static class SendJam extends AsyncTask<String, Void, String> {

        private OMGCallback mCallback;

        private long id = -1;

        private SendJam(OMGCallback callback) {
            mCallback = callback;
        }

        protected String doInBackground(String... args) {

            id = doHttp(args[0], args[2]);
            return null;
        }

        protected void onPreExecute(){
        }

        protected void onPostExecute(String result) {

            mCallback.onSuccess(id);

        }
    }

    private static long getIdFromResponse(String responseString) {
        long ret = -1;
        try {
            JSONObject response = new JSONObject(responseString);

            if (response.has("id")) {
                ret = response.getLong("id");
            }
        }
        catch (JSONException ex) {
            Log.e("MGH getIdFromResponse", ex.getMessage());
        }

        return ret;
    }
}
