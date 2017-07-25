package com.mikehelland.omgtechnogauntlet;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/*import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
*/

/**
 * User: m
 * Date: 7/4/13
 * Time: 11:35 PM
 */
public class SaveToOMG {

    public String desc = "";
    public String responseString = "";


    public SaveToOMG() {
    }

    private long doHttp(String saveUrl, String type, String data) {
        long id = -1;

        ContentValues values = new ContentValues();
        values.put("data", data);
        values.put("type", type);

        try
        {
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("data", new JSONObject(data));
            jsonParam.put("type", type);

            URL url = new URL(saveUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");

            OutputStreamWriter request = new OutputStreamWriter(connection.getOutputStream());

            request.write(URLEncoder.encode(jsonParam.toString(),"UTF-8"));
            request.flush();
            request.close();
            String line = "";
            InputStreamReader isr = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            // Response from server after login process will be stored in response variable.
            String response = sb.toString();
            responseString = response;

            id = getIdFromResponse(response);

            // You can perform UI operations here
            Log.d("MGH server response", response);
            isr.close();
            reader.close();

        }
        catch(IOException e)
        {
            // Error
            Log.e("MGH SaveToOMG IO", e.getMessage());
        }
        catch (JSONException e) {
            Log.e("MGH SaveToOMG JSON", e.getMessage());
        }

        Log.d("MGH doHttp", responseString);
        desc = responseString;
            //}


        Log.d("MGH doHttp saved?", desc);
        return id;

        //return 0;
    }

    public void execute(String saveUrl, String type, String data, OMGCallback callback) {
        new SendJam(callback).execute(saveUrl, type, data);
    }

    private class SendJam extends AsyncTask<String, Void, String> {

        private OMGCallback mCallback;

        private long id = -1;

        private SendJam(OMGCallback callback) {
            mCallback = callback;
        }

        protected String doInBackground(String... args) {

            id = doHttp(args[0], args[1], args[2]);
            return null;
        }

        protected void onPreExecute(){
        }

        protected void onPostExecute(String result) {

            mCallback.onSuccess(id);

        }
    }

    private long getIdFromResponse(String responseString) {
        long ret = -1;
        try {
            JSONObject response = new JSONObject(responseString);


            if (response.has("result") && response.getString("result").equals("good")) {
                if (response.has("id")) {
                    ret = response.getLong("id");
                }
            }
        }
        catch (JSONException ex) {

        }

        return ret;
    }
}
