package com.astronaut_wannabe.pocketutil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import com.astronaut_wannabe.PocketClient;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.astronaut_wannabe.PocketClient.CONSUMER_KEY;
import static com.astronaut_wannabe.PocketClient.Pocket;

public class AuthActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final boolean isApproved = "***REMOVED***".equalsIgnoreCase(getIntent().getDataString());
        if(isApproved) {
            final String prefKey = getString (R.string.pocket_access_key);
            final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
            prefs.getString(prefKey, null);
            authorizeToken(CONSUMER_KEY, prefs.getString(prefKey, null));
        } else {
            setContentView(R.layout.activity_auth);
            final String prefKey = getString (R.string.pocket_access_key);
            final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
            final String token = prefs.getString(prefKey, null);
        }
    }

    public void doAuth(View view) {
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(PocketClient.API_URL)
                .setRequestInterceptor(PocketClient.sRequestInterceptor)
                .build();

        final Pocket pocket = restAdapter.create(Pocket.class);

        // Get initial token
        final String redirectUrl = getString(R.string.oauth_redirect_url) + ":authdone";
        final PocketClient.TokenRequest req = new PocketClient.TokenRequest();
        req.consumer_key = PocketClient.CONSUMER_KEY;
        req.redirect_uri = redirectUrl;

        pocket.obtainRequestToken(req, new Callback<PocketClient.TokenResponse>() {
            @Override
            public void success(PocketClient.TokenResponse s, Response response) {
                makeToast(s.code, response);
                final String prefKey = getString(R.string.pocket_access_key);
                final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putString(prefKey, s.code);
                editor.commit();
                launchBrowser(s.code, redirectUrl);
            }

            @Override
            public void failure(RetrofitError error) {
                makeToast(error);
            }
        });
    }

    public void launchBrowser(String code, String url) {
        final Uri builtUri = Uri.parse("https://getpocket.com/auth/authorize").buildUpon()
                .appendQueryParameter("request_token", code)
                .appendQueryParameter("mobile", "1")
                .appendQueryParameter("redirect_uri", url)
                .build();
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(builtUri);
        startActivity(intent);
    }

    public void authorizeToken(String key, String code) {
        final PocketClient.TokenRequest req = new PocketClient.TokenRequest();
        req.code = code;
        req.consumer_key = key;

        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setRequestInterceptor(PocketClient.sRequestInterceptor)
                .setEndpoint(PocketClient.API_URL)
                .build();

        final Pocket pocket = restAdapter.create(Pocket.class);
        pocket.authorizeToken(req, new Callback<PocketClient.TokenResponse>() {
            @Override
            public void success(PocketClient.TokenResponse tokenResponse, Response response) {
                final String prefKey = getString(R.string.pocket_access_key);
                final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putString(prefKey, tokenResponse.access_token);
                editor.commit();

                makeToast(tokenResponse.username, response);
            }

            @Override
            public void failure(RetrofitError error) {
                makeToast(error);
            }
        });

    }

    public void makeToast(String s, Response r){
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
    public void makeToast(RetrofitError error){
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
    }
}
