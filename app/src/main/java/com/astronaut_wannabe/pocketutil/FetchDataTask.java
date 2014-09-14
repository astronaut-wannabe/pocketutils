package com.astronaut_wannabe.pocketutil;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
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

import java.io.BufferedReader;
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
        // dumpResponseToLog(retrieveStream(url));
        return null;
    }

    private InputStream retrieveStream(String url) {

        final SharedPreferences prefs = mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        final String prefKey = mContext.getString(R.string.pocket_access_key);
        final String accessKey = prefs.getString(prefKey, null);

        Log.d(LOG_TAG, "access_key is: " + accessKey);

        if(accessKey == null){
            return null;
        }

        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        // Add your data
        final String sincePref = prefs.getString(mContext.getString(R.string.pocket_since_date), null);
        final String since = sincePref != null ? sincePref : "0";
        Log.d(LOG_TAG, "Current Since date is: " + since);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("consumer_key", mConsumerKey));
        nameValuePairs.add(new BasicNameValuePair("access_token", accessKey));
        nameValuePairs.add(new BasicNameValuePair("count", "10"));
        nameValuePairs.add(new BasicNameValuePair("detailType", "complete"));
        nameValuePairs.add(new BasicNameValuePair("since", since));

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
        // update the sinceDate value for future calls
        final SharedPreferences.Editor editor = mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit();
        editor.putString(mContext.getString(R.string.pocket_since_date), Integer.toString(response.since));
        editor.commit();
        Log.d(LOG_TAG, "New Since date is: " + response.since);

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
            value.put(PocketItemEntry.COLUMN_POCKET_ITEM_ID, item.item_id);
            value.put(PocketItemEntry.COLUMN_POCKET_RESOLVED_ID, item.resolved_id);
            value.put(PocketItemEntry.COLUMN_DATETEXT, item.time_added);
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

    private void dumpResponseToLog(InputStream is){
        BufferedReader reader = null;
        final StringBuffer buffer = new StringBuffer();

        try {
            if (is == null) {
                // Nothing to do.
                return;
            }

            reader = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error dumping the JSON from the response:", e);
        }
        final String mess = buffer.toString();
        Log.d(LOG_TAG, mess);
    }
}
