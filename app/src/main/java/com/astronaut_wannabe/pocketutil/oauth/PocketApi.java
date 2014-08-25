package com.astronaut_wannabe.pocketutil.oauth;

import android.content.Intent;
import android.net.Uri;

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
    private final static String sObtainRequestTokenUrl = "https://getpocket.com/v3/oauth/request";
    private final static String sRetrieveContentUrl = "https://getpocket.com/v3/get";
    private final static String sRedirectToPocketForAuthorizeUrl =
            "https://getpocket.com/auth/authorize";

    public static Intent getUserRedirectIntent(String token, String redirectUri){
        final Uri builtUri = Uri.parse(sRedirectToPocketForAuthorizeUrl).buildUpon()
                    .appendQueryParameter("request_token", token)
                    .appendQueryParameter("redirect_uri", redirectUri)
                    .build();
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(builtUri);
        return intent;
    }
}
