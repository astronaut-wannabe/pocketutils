package com.astronaut_wannabe.pocketutil.service;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astronaut_wannabe.pocketutil.R;
import com.astronaut_wannabe.pocketutil.SignInTask;

/**
 * Created by ***REMOVED*** on 9/6/14.
 */
public class SignInFragment  extends Fragment {

    private static final String LOG_TAG = SignInFragment.class.getSimpleName();

    public void signin() {
        final SignInTask task = new SignInTask();
        task.execute(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        Log.d(LOG_TAG, "saveStateInstance="+savedInstanceState);
        //check for a token to auth
        final String key = getActivity().getString(R.string.pocket_access_key);
        final String token = getActivity()
                    .getSharedPreferences("prefs", Context.MODE_PRIVATE).getString(key,null);
        Log.d(LOG_TAG, "token="+token);
        if (null != token){
            signin();
            return null;
        } else {
            final View rootView = inflater.inflate(R.layout.fragment_signin, container, false);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signin();
                }
            });
            return rootView;
        }
    }
}
