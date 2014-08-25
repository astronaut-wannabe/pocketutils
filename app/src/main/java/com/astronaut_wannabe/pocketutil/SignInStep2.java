package com.astronaut_wannabe.pocketutil;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by ***REMOVED*** on 8/24/14.
 */
public class SignInStep2 extends AsyncTask<String, Void, String> {

    public static final String TAG = SignInTask.class.getSimpleName().toString();

    final private String mConsumerKey = "31435-0abb54732de387258fdc3ca5";
    final private String mAuthorizeUrl = "https://getpocket.com/v3/oauth/authorize";
    final private String mRedirectUrl = "pocketapp31435:authdone";
    final TextView mText;

    public SignInStep2(TextView tv) {
        mText = tv;
    }

    @Override
    protected String doInBackground(String... params) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;
        Log.d(TAG, "attempting pocket auth...");

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(mAuthorizeUrl);

        try {
            // Add your data
            List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("consumer_key", mConsumerKey));
            nameValuePairs.add(new BasicNameValuePair("code", params[0]));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            final Header[] headers = response.getAllHeaders();
            for(Header h : headers)
                Log.d(TAG, h.toString());

            InputStream inputStream = response.getEntity().getContent();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line);
            }
            final String mess = buffer.toString();
            Log.d(TAG, "content:" + mess);
            return mess;

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        mText.setText(s);
    }
}
