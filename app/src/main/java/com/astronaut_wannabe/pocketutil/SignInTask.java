package com.astronaut_wannabe.pocketutil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
public class SignInTask extends AsyncTask<Context, Void, Void> {
    public static final String LOG_TAG = SignInTask.class.getSimpleName().toString();

    final private String mConsumerKey = "31435-0abb54732de387258fdc3ca5";
    final private String mRequestTokenUrl = "https://getpocket.com/v3/oauth/request";
    final private String mAuthorizeUrl = "https://getpocket.com/auth/authorize";
    final private String mAuthorizeAccessKeyUrl = "https://getpocket.com/v3/oauth/authorize";
    final private String mRedirectUrl = "pocketapp31435:authdone";

    @Override
    protected Void doInBackground(Context... params) {
        final Activity activity = (Activity) params[0];
        final SharedPreferences prefs = activity.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        final String prefKey = activity.getString(R.string.pocket_access_key);
        final String accessKey = prefs.getString(prefKey, null);

        if(accessKey == null){
            Log.d(LOG_TAG, "No Access key, obtaining initial token.");
            getAccessKey(prefs, activity);
        }else{
            Log.d(LOG_TAG, String.format("token=%s - Updating token to access key.",accessKey));
            authorizeAccessKey(prefs, activity);
        }
        return null;
    }

    private void getAccessKey(SharedPreferences prefs, Activity activity){
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;
        Log.d(LOG_TAG, "attempting pocket auth...");

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
                Log.d(LOG_TAG, h.toString());

            InputStream inputStream = response.getEntity().getContent();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
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
            Log.d(LOG_TAG, "content:" + code);

            final String prefKey = activity.getString(R.string.pocket_access_key);
            final SharedPreferences.Editor editor = prefs.edit();
            editor.putString(prefKey, code.trim()
            );
            editor.commit();
            Uri builtUri = Uri.parse(mAuthorizeUrl).buildUpon()
                    .appendQueryParameter("request_token", code)
                    .appendQueryParameter("mobile", "1")
                    .appendQueryParameter("redirect_uri", mRedirectUrl)
                    .build();
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(builtUri);
            activity.startActivity(intent);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
    }

    private void authorizeAccessKey(SharedPreferences prefs, Activity activity){
        final String prefKey = activity.getString(R.string.pocket_access_key);
        final String accessKey = prefs.getString(prefKey, null);

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;
        Log.d(LOG_TAG, "attempting to authorize access key...");

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(mAuthorizeAccessKeyUrl);

        try {
            // Add your data
            List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("consumer_key", mConsumerKey));
            nameValuePairs.add(new BasicNameValuePair("code", accessKey));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            final Header[] headers = response.getAllHeaders();
            for(Header h : headers)
                Log.d(LOG_TAG, h.toString());

            InputStream inputStream = response.getEntity().getContent();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
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
            final int start = mess.indexOf('=') + 1;
            final int end = mess.indexOf('&');
            final String authorizedAccessToken = mess.substring(start, end);
            Log.d(LOG_TAG, "message=" + mess);
            Log.d(LOG_TAG, "Authorized access token=" + authorizedAccessToken);

            final String isPocketAuthorizedPrefKey = activity.getString(R.string.pocket_authorized);
            final SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(isPocketAuthorizedPrefKey, true);
            editor.putString(prefKey, authorizedAccessToken);
            editor.commit();

            //restart the main activity
            final Intent intent = new Intent(activity, HomeScreenActivity.class);
            activity.startActivity(intent);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
    }
}
