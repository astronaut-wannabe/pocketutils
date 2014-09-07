package com.astronaut_wannabe.pocketutil;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.astronaut_wannabe.pocketutil.data.PocketDataContract;

/**
 * Created by ***REMOVED*** on 9/6/14.
 */
public class HomeScreenFragment extends Fragment implements View.OnClickListener {

    public static final String LOG_TAG = HomeScreenFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.home_screen_fragment, container, false);
        final int childCount = ((ViewGroup) rootView).getChildCount();
        final HomeScreenClickListener clickListener = new HomeScreenClickListener();
        for (int i = 0; i < childCount; ++i) {
            ((ViewGroup) rootView).getChildAt(i).setOnClickListener(clickListener);
        }

        final Cursor cursor = getActivity().getContentResolver().query(
                PocketDataContract.PocketItemEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
        final int listCount = cursor.getCount();
        final String labelFormat = getString(R.string.home_screen_label_format, listCount);
        cursor.close();

        final TextView label = (TextView) rootView.findViewById(R.id.home_screen_label);
        label.setText(Html.fromHtml(labelFormat));

        return rootView;
    }

    @Override
    public void onClick(View v) {

    }
}
