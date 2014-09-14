package com.astronaut_wannabe.pocketutil.service;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

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
        final ViewSwitcher rootView = (ViewSwitcher) inflater.inflate(
                R.layout.fragment_signin, container, false);
        rootView.setInAnimation(getActivity(), R.anim.fade_in);
        rootView.setOutAnimation(getActivity(), R.anim.fade_out);

        //check for a token to auth
        final String key = getActivity().getString(R.string.pocket_access_key);
        final String token = getActivity()
                    .getSharedPreferences("prefs", Context.MODE_PRIVATE).getString(key,null);
        if (null != token){
            signin();
            return null;
        } else {
            rootView.showNext();
            View btn = rootView.findViewById(R.id.sign_in_button);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signin();
                    rootView.showNext();
                }
            });
            return rootView;
        }
    }
}
