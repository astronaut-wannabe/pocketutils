package com.astronaut_wannabe.pocketutil;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.astronaut_wannabe.pocketutil.data.PocketDataContract.PocketItemEntry;
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
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ***REMOVED*** on 8/24/14.
 */
public class FetchDataTask extends AsyncTask<Void, Void, Void> {

    public static final String LOG_TAG = FetchDataTask.class.getSimpleName().toString();

    final private String mConsumerKey = "31435-0abb54732de387258fdc3ca5";
    final private Activity mContext;

    public FetchDataTask(Activity context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        final String url = "https://getpocket.com/v3/get";

        InputStream source = retrieveStream(url);

        if ( source != null) {
            Gson gson = new Gson();

            Reader reader = new InputStreamReader(source);

            final PocketResponse pocketResponse = gson.fromJson(reader, PocketResponse.class);
            addPocketItemsToDb(pocketResponse);
        }
        return null;
    }

    private InputStream retrieveStream(String url) {

        final SharedPreferences prefs = mContext.getPreferences(mContext.MODE_PRIVATE);
        final String prefKey = mContext.getString(R.string.pocket_access_key);
        final String accessKey = prefs.getString(prefKey, null);

        Log.d(LOG_TAG, "access_key is: " + accessKey);

        if(accessKey == null){
            return null;
        }

        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        // Add your data
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("consumer_key", mConsumerKey));
        nameValuePairs.add(new BasicNameValuePair("access_token", accessKey));
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

    private void addPocketItemsToDb(PocketResponse response){
        Collection<PocketItem> items = response.list.values();
        PocketItem[] arr = new PocketItem[items.size()];
        items.toArray(arr);
        ContentValues[] values =
                convertResponseToContentValues(arr);
        final int count = mContext.getContentResolver()
                .bulkInsert(PocketItemEntry.CONTENT_URI, values);
        Log.d(LOG_TAG, "Added " + count + " rows to the DB");

    }

    private ContentValues [] convertResponseToContentValues(PocketItem[] items){
        final int size = items.length;
        if (0 == size){
            return new ContentValues[0];
        }

        final ContentValues [] ret = new ContentValues[size];

        for(int i = 0; i < size; ++i){
            final PocketItem item = items[i];
            final ContentValues value = new ContentValues();
            value.put(PocketItemEntry.COLUMN_POCKET_ITEM_ID, item.resolved_id);
            value.put(PocketItemEntry.COLUMN_DATETEXT, "1000000");
            value.put(PocketItemEntry.COLUMN_TITLE, item.resolved_title);
            value.put(PocketItemEntry.COLUMN_RESOLVED_URL, item.resolved_url);
            value.put(PocketItemEntry.COLUMN_EXCERPT, item.excerpt);
            ret[i] = value;
        }
        return ret;
    }

    private Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
