package com.astronaut_wannabe.pocketutil;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;
import android.widget.Toast;

import com.astronaut_wannabe.pocketutil.data.PocketDataContract.PocketItemEntry;

/**
 * Created by ***REMOVED*** on 9/6/14.
 */
public class PocketListFragment extends Fragment {

    public static final String LOG_TAG = PocketListFragment.class.getSimpleName();

    private final static int POCKET_LOADER = 0;

    private ViewFlipperCursorAdapter mAdapter;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(POCKET_LOADER, null, mAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.pocket_list_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
         if (id == R.id.action_list_count){
            final Activity activity = getActivity();
            final Cursor cursor = activity.getContentResolver()
                    .query(PocketItemEntry.CONTENT_URI,null,null,null,null);
            if (cursor.moveToFirst())
                Toast.makeText(activity,"Items in list: " +cursor.getCount(),Toast.LENGTH_LONG).show();
            else
                Toast.makeText(activity,"Items in list: " + 0,Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        final AdapterViewFlipper rootView = (AdapterViewFlipper) inflater.inflate(
                R.layout.fragment_pocket_list, container, false);
        rootView.setInAnimation(getActivity(), R.anim.slide_in_from_top);
        mAdapter = new ViewFlipperCursorAdapter(
                getActivity(),
                null,0);
        rootView.setAdapter(mAdapter);
        return rootView;
    }

    private void openPdf(String url) {
        final String googleDocsUrl = "http://docs.google.com/viewer?url=";
        final   Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(googleDocsUrl + url), "text/html");

        final PackageManager packageManager = getActivity().getPackageManager();
        if (!packageManager.queryIntentActivities(intent, 0).isEmpty()) {
            startActivity(intent);
        }
    }

    private void openMp3(String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        final Uri uri = Uri.parse(url);
        intent.setData(uri);

        final PackageManager packageManager = getActivity().getPackageManager();
        if (!packageManager.queryIntentActivities(intent, 0).isEmpty()) {
            startActivity(intent);
        }
    }

    private void openWebPage(String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        final Uri uri = Uri.parse(url);
        intent.setData(uri);

        final PackageManager packageManager = getActivity().getPackageManager();
        if (!packageManager.queryIntentActivities(intent, 0).isEmpty()) {
            startActivity(intent);
        }
    }
}
