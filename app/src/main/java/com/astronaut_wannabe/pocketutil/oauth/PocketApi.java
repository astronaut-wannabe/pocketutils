package com.astronaut_wannabe.pocketutil.oauth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.astronaut_wannabe.pocketutil.R;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * The pocket Auth flow isn't actually OAuth, which is annoying. The flow is:
 * <dl>
 *     <dt><b>Obtain a request token</b></dt>
 *     <dd>This is done by sending an http request to the /request url</dd>
 *     <dt><b>Redirect user to Pocket to continue authorization</b></dt>
 *     <dd>Pocket does not want you opening up a WebView, so you must use the token you obtained to
 *     launch the default browser. The redirect URI is whatever you have set in the Manifest for
 *     your callback Activity</dd>
 *     <dt><b>Convert a request token into a Pocket access token</b></dt>
 *     <dd>Once you have received a callback from pocket, you can make a call to the oauth/authorize
 *     endpoint to obtain an actual access token</dd>
 * </dl>
 *
 * Created by ***REMOVED*** on 8/24/14.
 */
public class PocketApi {
    private final static String sConsumerKey = "31435-0abb54732de387258fdc3ca5";

    private final static String sObtainRequestTokenUrl = "https://getpocket.com/v3/oauth/request";
    public final static String RETRIEVE_CONTENT_URL = "https://getpocket.com/v3/get";
    public final static String MODIFY_CONTENT_URL = "https://getpocket.com/v3/send";
    private final static String sRedirectToPocketForAuthorizeUrl =
            "https://getpocket.com/auth/authorize";
    private static final String LOG_TAG = PocketApi.class.getSimpleName();

    public static Intent getUserRedirectIntent(String token, String redirectUri){
        final Uri builtUri = Uri.parse(sRedirectToPocketForAuthorizeUrl).buildUpon()
                    .appendQueryParameter("request_token", token)
                    .appendQueryParameter("redirect_uri", redirectUri)
                    .build();
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(builtUri);
        return intent;
    }
    @NonNull public static String getConsumerKey(){
        return sConsumerKey;
    }

    @Nullable public static String getAccessKey(final Context context){
        final SharedPreferences prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        final String prefKey = context.getString(R.string.pocket_access_key);
        final String accessKey = prefs.getString(prefKey, null);
        Log.d(LOG_TAG, "access_key is being accessed: " + accessKey);
        return accessKey;
    }

    @Nullable public static String getLatestSinceDate(final Context context){
        final SharedPreferences prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        final String sincePref = prefs.getString(context.getString(R.string.pocket_since_date), null);
        final String since = sincePref != null ? sincePref : "0";
        Log.d(LOG_TAG, "Current Since date is: " + since);
        return since;
    }

    @NonNull public static HttpPost getRetrieveNewPocketItemsHttpRequest(final Context context)
            throws UnsupportedEncodingException {
        final HttpPost httppost = new HttpPost(RETRIEVE_CONTENT_URL);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("consumer_key", PocketApi.getConsumerKey()));
        nameValuePairs.add(new BasicNameValuePair("access_token", PocketApi.getAccessKey(context)));
        nameValuePairs.add(new BasicNameValuePair("count", "5000"));
        nameValuePairs.add(new BasicNameValuePair("detailType", "complete"));
        nameValuePairs.add(new BasicNameValuePair("since", PocketApi.getLatestSinceDate(context)));
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        return httppost;
    }
}
