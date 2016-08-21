package com.astronaut_wannabe.pocketutil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import com.astronaut_wannabe.PocketClient;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

import static com.astronaut_wannabe.PocketClient.Pocket;

public class AuthActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final boolean isApproved = "pocketapp41140:authdone".equalsIgnoreCase(getIntent().getDataString());
        if(isApproved) {
            final String prefKey = getString (R.string.pocket_access_key);
            final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
            prefs.getString(prefKey, null);
            authorizeToken(prefs.getString(prefKey, null));
        } else {
            setContentView(R.layout.activity_auth);
        }
    }

    public void doAuth(View view) {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(PocketClient.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofit.client().networkInterceptors().add(PocketClient.sRequestInterceptor);

        final Pocket pocket = retrofit.create(Pocket.class);

        // Get initial token
        final String redirectUrl = getString(R.string.oauth_redirect_url) + ":authdone";
        final PocketClient.TokenRequest req = new PocketClient.TokenRequest();
        req.consumer_key = PocketClient.CONSUMER_KEY;
        req.redirect_uri = redirectUrl;

        Call<PocketClient.TokenResponse> call = pocket.obtainRequestToken(req);

        final Callback<PocketClient.TokenResponse> cb = new Callback<PocketClient.TokenResponse>() {

            @Override
            public void onResponse(Response<PocketClient.TokenResponse> response) {
                makeToast(response.body().code, response);
                final String prefKey = getString(R.string.pocket_access_key);
                final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putString(prefKey, response.body().code);
                editor.commit();
                launchBrowser(response.body().code, redirectUrl);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        };
        call.enqueue(cb);
    }

    private void launchBrowser(String code, String url) {
        final Uri builtUri = Uri.parse("https://getpocket.com/auth/authorize").buildUpon()
                .appendQueryParameter("request_token", code)
                .appendQueryParameter("mobile", "1")
                .appendQueryParameter("redirect_uri", url)
                .build();
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(builtUri);
        startActivity(intent);
    }

    private void authorizeToken(String code) {
        final PocketClient.TokenRequest req = new PocketClient.TokenRequest();
        req.code = code;
        req.consumer_key = PocketClient.CONSUMER_KEY;

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(PocketClient.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofit.client().networkInterceptors().add(PocketClient.sRequestInterceptor);

        final Pocket pocket = retrofit.create(Pocket.class);

        Call<PocketClient.TokenResponse> call = pocket.authorizeToken(req);
        final Activity activity = this;
        final Callback<PocketClient.TokenResponse> cb = new Callback<PocketClient.TokenResponse>() {

            @Override
            public void onResponse(Response<PocketClient.TokenResponse> response) {
                final String token = response.body().access_token;
                final String prefKey = getString(R.string.pocket_access_key);
                final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putString(prefKey, token);
                editor.commit();
                startActivity(new Intent(activity, SwipeActivity.class));

                makeToast(response.body().username, response);
            }

            @Override
            public void onFailure(Throwable t) {

            }
        };
        call.enqueue(cb);
    }

    private void makeToast(String s, Response r){
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}
