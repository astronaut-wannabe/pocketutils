package com.astronaut_wannabe.pocketutil.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.astronaut_wannabe.pocketutil.R;
import com.astronaut_wannabe.pocketutil.data.PocketDataContract;
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
 * Created by ***REMOVED*** on 9/14/14.
 */
public class PocketUtilSyncAdapter extends AbstractThreadedSyncAdapter {
    /**
     * Interval to sync with the user's Pocket list, in seconds.
     * 60 secs * 180 = 3 hours
     */
    public static final int SYNC_INTERVAL = 60 * 10;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private static final String LOG_TAG = PocketUtilSyncAdapter.class.getSimpleName();
    private final String mConsumerKey = "31435-0abb54732de387258fdc3ca5";

    public PocketUtilSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        final String url = "https://getpocket.com/v3/get";
        final InputStream source = retrieveStream(url);


        if ( source != null) {
            Gson gson = new Gson();

            Reader reader = new InputStreamReader(source);

            final PocketResponse pocketResponse = gson.fromJson(reader, PocketResponse.class);
            addPocketItemsToDb(pocketResponse);
        }
        // dumpResponseToLog(retrieveStream(url));
    }

    private InputStream retrieveStream(String url) {

        final SharedPreferences prefs = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        final String prefKey = getContext().getString(R.string.pocket_access_key);
        final String accessKey = prefs.getString(prefKey, null);

        Log.d(LOG_TAG, "access_key is: " + accessKey);

        if(accessKey == null){
            return null;
        }

        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        // Add your data
        final String sincePref = prefs.getString(getContext().getString(R.string.pocket_since_date), null);
        final String since = sincePref != null ? sincePref : "0";
        Log.d(LOG_TAG, "Current Since date is: " + since);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("consumer_key", mConsumerKey));
        nameValuePairs.add(new BasicNameValuePair("access_token", accessKey));
        nameValuePairs.add(new BasicNameValuePair("count", "5000"));
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
        final SharedPreferences.Editor editor = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE).edit();
        editor.putString(getContext().getString(R.string.pocket_since_date), Integer.toString(response.since));
        editor.commit();
        Log.d(LOG_TAG, "New Since date is: " + response.since);

        Collection<PocketItem> items = response.list.values();
        PocketItem[] arr = new PocketItem[items.size()];
        items.toArray(arr);
        ContentValues[] values =
                convertResponseToContentValues(arr);
        final int count = getContext().getContentResolver()
                .bulkInsert(PocketDataContract.PocketItemEntry.CONTENT_URI, values);
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
            value.put(PocketDataContract.PocketItemEntry.COLUMN_POCKET_ITEM_ID, item.item_id);
            value.put(PocketDataContract.PocketItemEntry.COLUMN_POCKET_RESOLVED_ID, item.resolved_id);
            value.put(PocketDataContract.PocketItemEntry.COLUMN_DATETEXT, item.time_added);
            value.put(PocketDataContract.PocketItemEntry.COLUMN_TITLE, item.resolved_title);
            value.put(PocketDataContract.PocketItemEntry.COLUMN_RESOLVED_URL, item.resolved_url);
            value.put(PocketDataContract.PocketItemEntry.COLUMN_EXCERPT, item.excerpt);
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

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static void configurePeriodicSync(Context context, int interval, int flexTime){
        final Account account = getSyncAccount(context);
        final String authority = context.getString(R.string.content_authority);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            final SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(interval,flexTime)
                    .setSyncAdapter(account,authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), interval);
        }

    }
    
    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet. If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
// Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

// Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

// If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

/*
* Add the account and account type, no password or user data
* If successful, return the Account object, otherwise report an error.
*/
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
/*
* If you don't set android:syncable="true" in
* in your <provider> element in the manifest,
* then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
* here.
*/
            onAccountCreated(newAccount, context);

        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context){
        PocketUtilSyncAdapter.configurePeriodicSync(context,SYNC_INTERVAL,SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount,
                context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context){
        getSyncAccount(context);
    }
}