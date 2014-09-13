package com.astronaut_wannabe.pocketutil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterViewFlipper;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.astronaut_wannabe.pocketutil.data.PocketDataContract.PocketItemEntry;

/**
 * Created by ***REMOVED*** on 9/6/14.
 */
public class PocketListFragment extends Fragment implements LoaderManager.LoaderCallbacks <Cursor> {

    public static final String LOG_TAG = PocketListFragment.class.getSimpleName();

    private final static int POCKET_LOADER = 0;

    private final static String[] POCKET_COLUMNS = {
            PocketItemEntry.TABLE_NAME + "." + PocketItemEntry._ID,
            PocketItemEntry.COLUMN_TITLE,
            PocketItemEntry.COLUMN_EXCERPT,
            PocketItemEntry.COLUMN_POCKET_ITEM_ID,
            PocketItemEntry.COLUMN_RESOLVED_URL
    };

    // Indices tied to the COLUMNS, if the column order changes in the table, these must be updated
    public final static int COL_ID = 0;
    public final static int COL_TITLE = 1;
    public final static int COL_EXCERPT  = 2;
    public final static int COL_POCKET_ITEM_ID = 3;
    public final static int COL_POCKET_URL = 4;

    private SimpleCursorAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(POCKET_LOADER, null, this);
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
        if (id == R.id.action_refresh) {
            new FetchDataTask(getActivity()).execute();
            return true;
        } else if (id == R.id.action_clear_data){
            // delete everything
            final Activity activity = getActivity();
            final SharedPreferences.Editor editor = activity.getPreferences(activity.MODE_PRIVATE).edit();
            editor.putString(activity.getString(R.string.pocket_since_date), null);
            editor.commit();
            activity.getContentResolver().delete(PocketItemEntry.CONTENT_URI, null, null);
            return true;
        }else if (id == R.id.action_list_count){
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
        //final ListView rootView = (ListView) inflater.inflate(R.layout.fragment_pocket_list,
          //      container, false);
final AdapterViewFlipper rootView = (AdapterViewFlipper) inflater.inflate(R.layout.fragment_pocket_list,
             container, false);

        mAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_pocket,
                null,
                new String[]{
                        PocketItemEntry.COLUMN_TITLE,
                        PocketItemEntry.COLUMN_POCKET_ITEM_ID

                },
                new int[]{
                        R.id.article_title_and_excerpt,
                        R.id.article_id
                },
                0
        );

        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                final TextView tv = (TextView) view;
                switch (columnIndex){

                    case COL_POCKET_ITEM_ID:
                        tv.setText(cursor.getString(columnIndex));
                        return true;
                    case COL_TITLE:
                        final String title = cursor.getString(COL_TITLE);
                        final String excerpt = cursor.getString(COL_EXCERPT);
                        final String htmlString = getString(R.string.title_and_synopsis_format, title, excerpt);
                        tv.setText(Html.fromHtml(htmlString));
                        return true;
                    default:
                        return false;
                }
            }
        });

        rootView.setAdapter(mAdapter);
        rootView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final SimpleCursorAdapter adapter = (SimpleCursorAdapter) parent.getAdapter();
                final Cursor cursor = adapter.getCursor();
                if(null != cursor && cursor.moveToPosition(position)) {
                    final String url = cursor.getString(COL_POCKET_URL);
                    if(url.contains(".pdf")){
                        Log.d(LOG_TAG, "Opening as a pdf: " + url);
                        openPdf(url);
                    } else if(url.contains(".mp3")){
                        Log.d(LOG_TAG, "Opening as a mp3: " + url);
                        openMp3(url);
                    } else {
                        Log.d(LOG_TAG, "Opening as a webpage: " + url);
                        openWebPage(url);
                    }
                }
            }
        });
        return rootView;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        final Uri uri = PocketItemEntry.CONTENT_URI;

        //sort ascending alphabetically by the article title
        final String sortOrder = PocketItemEntry.COLUMN_TITLE + " ASC";

        return new CursorLoader(
                getActivity(),
                uri,
                POCKET_COLUMNS,
                null,
                null,
                sortOrder
        );
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

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.swapCursor(null);
    }
}
