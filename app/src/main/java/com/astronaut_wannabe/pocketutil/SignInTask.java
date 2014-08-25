package com.astronaut_wannabe.pocketutil;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
public class SignInTask extends AsyncTask<Context, Void, String> {
    public static final String TAG = SignInTask.class.getSimpleName().toString();

    final private String mConsumerKey = "31435-0abb54732de387258fdc3ca5";
    final private String mRequestTokenUrl = "https://getpocket.com/v3/oauth/request";
    final private String mAuthorizeUrl = "https://getpocket.com/auth/authorize";
    final private String mRedirectUrl = "pocketapp31435:authdone";

    @Override
    protected String doInBackground(Context... params) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;
        Log.d(TAG, "attempting pocket auth...");

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(mRequestTokenUrl);

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("consumer_key", mConsumerKey));
            nameValuePairs.add(new BasicNameValuePair("redirect_uri", mRedirectUrl));
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
            final int start = mess.indexOf('=');
            final String code = mess.substring(start+1);
            Log.d(TAG, "content:" + code);

            AccessKey.code = code;

            Uri builtUri = Uri.parse(mAuthorizeUrl).buildUpon()
                    .appendQueryParameter("request_token", code)
                    .appendQueryParameter("mobile", "1")
                    .appendQueryParameter("redirect_uri", mRedirectUrl)
                    .build();
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(builtUri);
            params[0].startActivity(intent);

            return code;
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }

        return null;
    }

}
