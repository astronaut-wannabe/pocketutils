package com.astronaut_wannabe.pocketutil;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.astronaut_wannabe.PocketClient;
import com.astronaut_wannabe.model.PocketItem;
import com.astronaut_wannabe.model.PocketSendAction;
import com.astronaut_wannabe.model.PocketSendResponse;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;

import static com.astronaut_wannabe.PocketClient.CONSUMER_KEY;

public class SwipeFlingListener implements SwipeFlingAdapterView.onFlingListener {
    private final ArrayList<PocketItem> mData;
    private final ArrayAdapter mAdapter;
    private final Activity mContext;
    private final PocketClient.Pocket mClient;
    private final String mToken;


    public SwipeFlingListener(ArrayList<PocketItem> data, ArrayAdapter adapter, Activity context, PocketClient.Pocket c) {
        mData = data;
        mAdapter = adapter;
        mContext = context;
        mClient = c;
        mToken = getAccessToken();
    }

    private String getAccessToken() {
        final String prefKey = mContext.getString(R.string.pocket_access_key);
        final SharedPreferences prefs = mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        return prefs.getString(prefKey, null);
    }

    @Override
    public void removeFirstObjectInAdapter() {
        // this is the simplest way to delete an object from the Adapter (/AdapterView)
        Log.d("LIST", "removed object!");
        mData.remove(0);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLeftCardExit(Object o) {
        final PocketItem item = (PocketItem) o;
        //send retrofit call
        mClient.send(deleteRequest(item.item_id)).enqueue(stubCallback);
    }

    @Override
    public void onRightCardExit(Object o) {
        final PocketItem item = (PocketItem) o;
        // send retrofit call
        mClient.send(addRequest(item.item_id)).enqueue(stubCallback);
    }

    @Override
    public void onAdapterAboutToEmpty(int i) {
        // Ask for more data here
        Log.d("LIST", "notified");
    }

    @Override
    public void onScroll(float v) {

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

    private final static Callback<PocketSendResponse> stubCallback = new Callback<PocketSendResponse>() {
        @Override
        public void onResponse(Response<PocketSendResponse> response) {
            //ignore for now, should probably remove the item from the adapter
        }

        @Override
        public void onFailure(Throwable t) {
            //ignore
        }
    };
}
