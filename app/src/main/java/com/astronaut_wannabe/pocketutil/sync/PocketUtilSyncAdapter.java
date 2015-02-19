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
import com.astronaut_wannabe.pocketutil.oauth.PocketApi;
import com.astronaut_wannabe.pocketutil.pocket.PocketImageItem;
import com.astronaut_wannabe.pocketutil.pocket.PocketItem;
import com.astronaut_wannabe.pocketutil.pocket.PocketResponse;
import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PocketUtilSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = PocketUtilSyncAdapter.class.getSimpleName();
    public final static String DELETE_POCKET_ITEMS = "DELETE_ARTICLES";
    public static final int SYNC_INTERVAL = 60 * 10;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    public static final List<String> sArticlesToDelete = new ArrayList<>();
    public static final List<String> sArticlesToAdd = new ArrayList<>();

    public PocketUtilSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        if(extras.containsKey(DELETE_POCKET_ITEMS)) {
            deleteRemovedArticles();
            reAddArticles();
            return;
        }

        final InputStream source = retrieveStream();
        if (source != null) {
            final Gson gson = new Gson();
            final Reader reader = new InputStreamReader(source);
            final PocketResponse pocketResponse = gson.fromJson(reader, PocketResponse.class);
            addPocketItemsToDb(pocketResponse);
        }
    }

    private void deleteRemovedArticles() {
        if(sArticlesToDelete.isEmpty())
            return;

        final List<Map<String, String>> requestList = new ArrayList<>(sArticlesToDelete.size());

	    // create JSON request to tag every item as "pocket-util-delete"
        for(String id : sArticlesToDelete) {
            final Map<String, String> pocketRequestMap = new HashMap<>(1);
            pocketRequestMap.put("action","delete");
            pocketRequestMap.put("item_id",id);
            requestList.add(pocketRequestMap);
        }
	    // actually send request to the server
        if(postToPocket(requestList))
            sArticlesToDelete.clear();
    }

    private void reAddArticles() {
        if(sArticlesToAdd.isEmpty())
            return;

        final List<Map<String, String>> requestList = new ArrayList<>(sArticlesToAdd.size());

        // create JSON request to tag every item as "pocket-util-delete"
        for(String id : sArticlesToAdd) {
            final Map<String, String> pocketRequestMap = new HashMap<>(1);
            pocketRequestMap.put("action","add");
            pocketRequestMap.put("item_id",id);
            pocketRequestMap.put("tags","pocketutil-add");
            requestList.add(pocketRequestMap);
        }
        // actually send request to the server
        if(postToPocket(requestList))
            sArticlesToAdd.clear();
    }

    private boolean postToPocket(List<Map<String, String>> actions) {
        final DefaultHttpClient client = new DefaultHttpClient();
        HttpPost httpPost = null;
        try {
            httpPost = PocketApi.getModifyPocketItemsHttpRequest(getContext(), actions);
            final HttpResponse getResponse = client.execute(httpPost);
            final int statusCode = getResponse.getStatusLine().getStatusCode();

            if(statusCode == HttpStatus.SC_OK) {
                return true;
            } else {
                Log.w(getClass().getSimpleName(),
                        "Error " + statusCode + " for URL " + PocketApi.MODIFY_CONTENT_URL);
                return false;
            }
        }
        catch (IOException e) {
            httpPost.abort();
            Log.w(getClass().getSimpleName(), "Error for URL " + PocketApi.MODIFY_CONTENT_URL, e);
        }
        return true;
    }

    private InputStream retrieveStream() {
        final DefaultHttpClient client = new DefaultHttpClient();
        HttpPost httpPost = null;
        try {
            httpPost = PocketApi.getRetrieveNewPocketItemsHttpRequest(getContext());
            final HttpResponse getResponse = client.execute(httpPost);
            final int statusCode = getResponse.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                Log.w(getClass().getSimpleName(),
                        "Error " + statusCode + " for URL " + PocketApi.RETRIEVE_CONTENT_URL);
                return null;
            }

            HttpEntity getResponseEntity = getResponse.getEntity();
            return getResponseEntity.getContent();

        }
        catch (IOException e) {
            httpPost.abort();
            Log.w(getClass().getSimpleName(), "Error for URL " + PocketApi.RETRIEVE_CONTENT_URL, e);
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
            if(item.has_image > 0){
                for (PocketImageItem image : item.images.values()) {
                    value.put(PocketDataContract.PocketItemEntry.COLUMN_IMAGE_URL, image.src);
                }
                break;
            } else {
                value.put(PocketDataContract.PocketItemEntry.COLUMN_IMAGE_URL, "");
            }

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

    /**
     * Helper method to have the sync adapter delete a given list
     * of Pocket articles immediately.
     *
     * @param context The context used to access the account service
     * @param removedArticles The article Ids to delete
     * @param addedArticles the articles to move to the top of the list
     */
    public static void deleteImmediately(Context context, List<String> removedArticles, List<String> addedArticles) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(DELETE_POCKET_ITEMS, true);
        sArticlesToDelete.addAll(removedArticles);
        sArticlesToAdd.addAll(addedArticles);
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
        final AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        // Create the account type and default account
        final Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
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
        final String contentAuthority = context.getString(R.string.content_authority);
        PocketUtilSyncAdapter.configurePeriodicSync(context,SYNC_INTERVAL,SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, contentAuthority, true);
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context){
        getSyncAccount(context);
    }
}
