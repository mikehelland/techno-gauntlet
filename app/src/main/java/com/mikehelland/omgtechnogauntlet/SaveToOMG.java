package com.mikehelland.omgtechnogauntlet;

import android.os.AsyncTask;
import android.util.Log;

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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
/*        long id = -1;
        HttpClient httpclientup = new DefaultHttpClient();
        Log.d("MGH doHttp", "1");
        try {
            HttpPost hPost = new HttpPost(saveUrl);
            List<NameValuePair> postParams = new ArrayList<NameValuePair>();
            postParams.add(new BasicNameValuePair("data", data));
            postParams.add(new BasicNameValuePair("type", type));
            postParams.add(new BasicNameValuePair("tags", ""));
            hPost.setEntity(new UrlEncodedFormEntity(postParams));

            HttpResponse response = httpclientup.execute(hPost);
            StatusLine statusLine = response.getStatusLine();
            //if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();

                id = getIdFromResponse(responseString);

                Log.d("MGH doHttp", responseString);
                desc = responseString;
            //}

        } catch (ClientProtocolException ee) {
            desc = ee.getMessage();

        } catch (IOException ee) {
            desc = ee.getMessage();
        }

        Log.d("MGH doHttp saved?", desc);
        return id;
*/
        return 0;
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
