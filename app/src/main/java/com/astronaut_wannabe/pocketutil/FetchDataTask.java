package com.astronaut_wannabe.pocketutil;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.astronaut_wannabe.pocketutil.pocket.PocketItem;
import com.astronaut_wannabe.pocketutil.pocket.PocketResponse;
import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ***REMOVED*** on 8/24/14.
 */
public class FetchDataTask extends AsyncTask<Context, Void, PocketResponse> {

    public static final String TAG = SignInTask.class.getSimpleName().toString();

    final private String mConsumerKey = "31435-0abb54732de387258fdc3ca5";
    final private String mRequestTokenUrl = "https://getpocket.com/v3/oauth/request";
    final private String mAuthorizeUrl = "https://getpocket.com/auth/authorize";
    final private String mRedirectUrl = "pocketapp31435:authdone";
    final private TextView tv;

    public FetchDataTask(TextView textView) {
        tv = textView;
    }

    @Override
    protected PocketResponse doInBackground(Context... params) {
        final String url = "https://getpocket.com/v3/get";

            InputStream source = retrieveStream(url);

        Gson gson = new Gson();

            Reader reader = new InputStreamReader(source);

            return gson.fromJson(reader, PocketResponse.class);

    }

    private InputStream retrieveStream(String url) {

        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        // Add your data
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("consumer_key", mConsumerKey));
        nameValuePairs.add(new BasicNameValuePair("access_token", AccessKey.accessToken));
        nameValuePairs.add(new BasicNameValuePair("count", "10"));
        nameValuePairs.add(new BasicNameValuePair("detailType", "complete"));

        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse getResponse = client.execute(httppost);
            final int statusCode = getResponse.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                Log.w(getClass().getSimpleName(),
                        "Error " + statusCode + " for URL " + url);
                return null;
            }

            HttpEntity getResponseEntity = getResponse.getEntity();
            return getResponseEntity.getContent();

        }
        catch (IOException e) {
            httppost.abort();
            Log.w(getClass().getSimpleName(), "Error for URL " + url, e);
        }

        return null;

    }

    @Override
    protected void onPostExecute(PocketResponse pocketResponse) {
        StringBuilder builder = new StringBuilder();
        for (PocketItem item : pocketResponse.list.values()){
            builder.append(item.given_title).append(":\n").append(item.excerpt).append("\n\n");
        }
        tv.setText(builder);
    }
}
