package com.astronaut_wannabe.pocketutil.service;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astronaut_wannabe.pocketutil.R;
import com.astronaut_wannabe.pocketutil.SignInTask;

/**
 * Created by ***REMOVED*** on 9/6/14.
 */
public class SignInFragment  extends Fragment {

    public void signin(View view) {
        final SignInTask task = new SignInTask();
        task.execute(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_signin, container, false);
        final View button = rootView.findViewById(R.id.sign_in_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signin(v);
            }
        });
        return rootView;
    }
}
