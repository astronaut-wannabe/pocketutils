package com.astronaut_wannabe.pocketutil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterViewFlipper;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.astronaut_wannabe.PocketClient;
import com.astronaut_wannabe.model.PocketResponse;
import com.astronaut_wannabe.model.PocketSendAction;
import com.astronaut_wannabe.model.PocketSendResponse;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.astronaut_wannabe.PocketClient.CONSUMER_KEY;


public class SwipeActivity extends ActionBarActivity implements PocketSwipeItem.PocketSwipeCallbacks {
    private static final String LOG_TAG = SwipeActivity.class.getSimpleName();

    private ViewFlipperArrayAdapter mAdapter;
    private AdapterViewFlipper mFlipper;

    private PocketClient.Pocket mPocketClient;
    private String mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final boolean isAuthenticated = isAuthorized();
        if(isAuthenticated) {
            setContentView(R.layout.activity_signin);
            final ViewSwitcher vs = (ViewSwitcher) findViewById(R.id.viewSwitcher);
            Animation out = AnimationUtils.loadAnimation(this,android.R.anim.fade_out);
            Animation in = AnimationUtils.loadAnimation(this,android.R.anim.fade_in);
            vs.setOutAnimation(out);
            vs.setInAnimation(in);

            mPocketClient = getPocketClient();
            mToken = getAccessToken();
            mFlipper = (AdapterViewFlipper) findViewById(R.id.flipper);
            mFlipper.setInAnimation(this, R.anim.slide_in_from_top);
            mAdapter = new ViewFlipperArrayAdapter(this);
            mAdapter.setSwipeCallbacks(this);

            mPocketClient.get(createFetchRequest(1000), new Callback<PocketResponse>() {
                @Override
                public void success(PocketResponse pocketResponse, Response response) {
                    mAdapter.addAll(pocketResponse.list.values());
                    mFlipper.setAdapter(mAdapter);
                    vs.showNext();
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(LOG_TAG, error.toString());
                }
            });
        } else {
            startActivity(new Intent(this, AuthActivity.class));
        }
    }

    private boolean isAuthorized() {
        final String prefKey = getString(R.string.pocket_access_key);
        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        return prefs.getString(prefKey, null) != null;
    }

    private String getAccessToken() {
        final String prefKey = getString (R.string.pocket_access_key);
        final SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        return prefs.getString(prefKey, null);
    }

    private PocketClient.Pocket getPocketClient() {
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(PocketClient.API_URL)
                .setRequestInterceptor(PocketClient.sRequestInterceptor)
                .build();
        return restAdapter.create(PocketClient.Pocket.class);
    }

    private PocketClient.GetRequest createFetchRequest(int numItems) {
        final PocketClient.GetRequest req = new PocketClient.GetRequest();
        req.access_token = mToken;
        req.consumer_key = CONSUMER_KEY;
        req.count = Integer.toString(numItems);
        return req;
    }

    private PocketClient.PostRequest deleteRequest(int item_id) {
        return createSendRequest("delete",item_id);
    }

    private PocketClient.PostRequest addRequest(int item_id) {
        return createSendRequest("add",item_id);
    }

    private PocketClient.PostRequest createSendRequest(String action, int item_id) {
        final PocketClient.PostRequest req = new PocketClient.PostRequest();
        req.access_token = mToken;
        req.consumer_key = CONSUMER_KEY;

        PocketSendAction act = new PocketSendAction();
        act.action = action;
        act.item_id = item_id;

        req.actions = new PocketSendAction[] {act};

        return req;
    }

    @Override
    public void onLeftSwipe() {

    }


    private int getRandomArticle(){
        final int size = mFlipper.getCount();
        Log.d(LOG_TAG, String.format("current item count = %d", size));
        int randomArticle;
        randomArticle = (int) ((Math.random() * size) + 1);
        return randomArticle;
    }

    @Override
    public void onRightSwipe() {
        final TextView currentArticle = (TextView) mFlipper.getCurrentView().findViewById(R.id.article_id);
        final String id = currentArticle.getText().toString();
        //send retrofit call
        mPocketClient.send(addRequest(Integer.parseInt(id)), new Callback<PocketSendResponse>() {
            @Override
            public void success(PocketSendResponse pocketSendResponse, Response response) {
                Toast.makeText(getBaseContext(), "added " + id, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(RetrofitError error) {
                // no op
            }
        });
        final int nextArticle = getRandomArticle();
        mFlipper.setOutAnimation(this, R.anim.slide_right);
        mFlipper.setDisplayedChild(nextArticle);
    }

    @Override
    public void onTap() {

    }
}
