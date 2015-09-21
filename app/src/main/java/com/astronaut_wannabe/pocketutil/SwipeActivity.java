package com.astronaut_wannabe.pocketutil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ViewSwitcher;

import com.astronaut_wannabe.PocketClient;
import com.astronaut_wannabe.model.PocketItem;
import com.astronaut_wannabe.model.PocketResponse;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.Collections;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

import static com.astronaut_wannabe.PocketClient.CONSUMER_KEY;


public class SwipeActivity extends AppCompatActivity {
    private static final String LOG_TAG = SwipeActivity.class.getSimpleName();

    private SwipeFlingAdapterView mFlingContainer;
    private ViewFlipperArrayAdapter mAdapter;
    private ViewSwitcher mSwitcher;
    private PocketClient.Pocket mPocketClient;
    private String mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToken = getAccessToken();

        if (mToken == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_signin);
        mFlingContainer = (SwipeFlingAdapterView) findViewById(R.id.swipe_container);

        mSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);

        mPocketClient = getPocketClient();

        final Call<PocketResponse> call = mPocketClient.get(createFetchRequest(1000));
        final Activity activity = this;
        final Callback<PocketResponse> cb = new Callback<PocketResponse>() {
                @Override
                public void onResponse(Response<PocketResponse> response) {
                    final ArrayList<PocketItem> data = new ArrayList<>();
                    data.addAll(response.body().list.values());
                    Collections.shuffle(data);

                    mAdapter = new ViewFlipperArrayAdapter(activity, data);
                    mFlingContainer.setFlingListener(new SwipeFlingListener(data, mAdapter, activity,mPocketClient));
                    mAdapter.addAll(data);
                    mAdapter.notifyDataSetChanged();
                    mFlingContainer.setAdapter(mAdapter);
                    mSwitcher.showNext();
                }
                
                @Override
                public void onFailure(Throwable t) {
                    Log.e(LOG_TAG, t.getMessage());
                }
            };
        call.enqueue(cb);
    }

    private String getAccessToken() {
        final String prefKey = getString (R.string.pocket_access_key);
        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        return prefs.getString(prefKey, null);
    }

    private PocketClient.Pocket getPocketClient() {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(PocketClient.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofit.client().networkInterceptors().add(PocketClient.sRequestInterceptor);

        return retrofit.create(PocketClient.Pocket.class);
    }

    private PocketClient.GetRequest createFetchRequest(int numItems) {
        final PocketClient.GetRequest req = new PocketClient.GetRequest();
        req.access_token = mToken;
        req.consumer_key = CONSUMER_KEY;
        req.count = Integer.toString(numItems);
        return req;
    }
}
